package com.ventas.eCommerce.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ventas.eCommerce.config.AppProperties;
import com.ventas.eCommerce.entities.Order;
import com.ventas.eCommerce.entities.OrderItem;
import com.ventas.eCommerce.exceptions.PaymentException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Integración con MercadoPago Checkout Pro mediante su API REST.
 *
 * <ol>
 *   <li>Se crea una "preferencia" con los items del pedido y {@code external_reference = id del pedido}.</li>
 *   <li>El usuario paga en MercadoPago (la app nunca ve datos de tarjeta).</li>
 *   <li>MercadoPago avisa por webhook y redirige al usuario a {@code /payments/return}; en ambos casos
 *       se consulta el pago a la API para confirmar estado y monto.</li>
 * </ol>
 *
 * Se activa con {@code payment.provider=mercadopago}.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "mercadopago")
public class MercadoPagoPaymentGateway implements PaymentGateway {

    public static final String NAME = "mercadopago";

    private static final DateTimeFormatter MP_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private final RestClient client;
    private final MercadoPagoProperties properties;
    private final AppProperties appProperties;

    public MercadoPagoPaymentGateway(MercadoPagoProperties properties, AppProperties appProperties,
                                     RestClient.Builder builder) {
        if (!StringUtils.hasText(properties.accessToken())) {
            throw new IllegalStateException("payment.provider=mercadopago requiere configurar mercadopago.access-token");
        }
        this.properties = properties;
        this.appProperties = appProperties;
        this.client = builder
                .baseUrl(properties.apiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.accessToken())
                .build();
        if (!StringUtils.hasText(properties.webhookSecret())) {
            log.warn("mercadopago.webhook-secret no está configurado: no se validará la firma de los webhooks");
        }
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public PaymentSession createPayment(Order order) {
        String baseUrl = appProperties.baseUrl();

        List<Map<String, Object>> items = order.getItems().stream().map(this::toItem).toList();

        Map<String, Object> body = new HashMap<>();
        body.put("items", items);
        body.put("payer", Map.of("email", order.getUser().getEmail(), "name", order.getUser().getName()));
        body.put("external_reference", order.getId().toString());
        body.put("back_urls", Map.of(
                "success", baseUrl + "/payments/return",
                "pending", baseUrl + "/payments/return",
                "failure", baseUrl + "/payments/return"));
        body.put("auto_return", "approved");
        body.put("statement_descriptor", properties.statementDescriptor());
        // La preferencia vence junto con la reserva de stock, para que no se pueda pagar un pedido ya cancelado.
        body.put("expires", true);
        body.put("expiration_date_to", OffsetDateTime.now()
                .plus(appProperties.orders().reservationTime()).format(MP_DATE));
        notificationUrl().ifPresent(url -> body.put("notification_url", url));

        try {
            PreferenceResponse response = client.post()
                    .uri("/checkout/preferences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(PreferenceResponse.class);
            if (response == null || response.initPoint() == null) {
                throw new PaymentException("MercadoPago no devolvió un link de pago");
            }
            return new PaymentSession(response.id(), response.initPoint());
        } catch (RestClientException e) {
            log.error("Error creando la preferencia de MercadoPago para el pedido {}", order.getId(), e);
            throw new PaymentException("No se pudo iniciar el pago con MercadoPago. Intentá nuevamente.", e);
        }
    }

    @Override
    public Optional<PaymentResult> findPayment(String paymentId) {
        try {
            PaymentResponse payment = client.get()
                    .uri("/v1/payments/{id}", paymentId)
                    .retrieve()
                    .body(PaymentResponse.class);
            if (payment == null || !StringUtils.hasText(payment.externalReference())) {
                return Optional.empty();
            }
            return Optional.of(new PaymentResult(
                    String.valueOf(payment.id()),
                    Integer.valueOf(payment.externalReference()),
                    mapStatus(payment.status()),
                    payment.transactionAmount()));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw new PaymentException("No se pudo consultar el pago en MercadoPago", e);
        } catch (NumberFormatException e) {
            log.warn("Pago {} de MercadoPago con external_reference inválido", paymentId);
            return Optional.empty();
        } catch (RestClientException e) {
            throw new PaymentException("No se pudo consultar el pago en MercadoPago", e);
        }
    }

    /**
     * Valida la firma {@code x-signature} de un webhook según la documentación de MercadoPago:
     * HMAC-SHA256 de {@code id:<data.id>;request-id:<x-request-id>;ts:<ts>;} con la clave secreta.
     * Si no hay clave configurada, acepta el webhook (igual se verifica el pago contra la API).
     */
    public boolean isValidWebhookSignature(String signatureHeader, String requestId, String dataId) {
        if (!StringUtils.hasText(properties.webhookSecret())) {
            return true;
        }
        if (!StringUtils.hasText(signatureHeader) || !StringUtils.hasText(dataId)) {
            return false;
        }

        String ts = null;
        String v1 = null;
        for (String part : signatureHeader.split(",")) {
            String[] keyValue = part.trim().split("=", 2);
            if (keyValue.length == 2) {
                if ("ts".equals(keyValue[0])) {
                    ts = keyValue[1];
                } else if ("v1".equals(keyValue[0])) {
                    v1 = keyValue[1];
                }
            }
        }
        if (ts == null || v1 == null) {
            return false;
        }

        StringBuilder manifest = new StringBuilder("id:").append(dataId.toLowerCase()).append(';');
        if (StringUtils.hasText(requestId)) {
            manifest.append("request-id:").append(requestId).append(';');
        }
        manifest.append("ts:").append(ts).append(';');

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.webhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(manifest.toString().getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(HexFormat.of().formatHex(expected).getBytes(StandardCharsets.UTF_8),
                    v1.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("No se pudo validar la firma del webhook de MercadoPago", e);
            return false;
        }
    }

    private Map<String, Object> toItem(OrderItem item) {
        Map<String, Object> map = new HashMap<>();
        if (item.getProduct() != null) {
            map.put("id", item.getProduct().getId().toString());
        }
        map.put("title", item.getProductName());
        map.put("quantity", item.getQuantity());
        map.put("unit_price", item.getUnitPrice());
        map.put("currency_id", properties.currency());
        return map;
    }

    private Optional<String> notificationUrl() {
        if (StringUtils.hasText(properties.notificationUrl())) {
            return Optional.of(properties.notificationUrl());
        }
        String baseUrl = appProperties.baseUrl();
        // MercadoPago no puede alcanzar localhost; en desarrollo el pago se confirma al volver a /payments/return.
        if (baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1")) {
            return Optional.empty();
        }
        return Optional.of(baseUrl + "/payments/webhook/mercadopago");
    }

    private static Status mapStatus(String status) {
        if (status == null) {
            return Status.PENDING;
        }
        return switch (status) {
            case "approved" -> Status.APPROVED;
            case "rejected", "cancelled", "refunded", "charged_back" -> Status.REJECTED;
            default -> Status.PENDING; // pending, in_process, authorized, in_mediation
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PreferenceResponse(String id, @JsonProperty("init_point") String initPoint) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PaymentResponse(
            Long id,
            String status,
            @JsonProperty("external_reference") String externalReference,
            @JsonProperty("transaction_amount") BigDecimal transactionAmount) {
    }
}
