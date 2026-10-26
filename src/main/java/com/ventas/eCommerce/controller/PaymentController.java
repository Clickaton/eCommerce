package com.ventas.eCommerce.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ventas.eCommerce.exceptions.ResourceNotFoundException;
import com.ventas.eCommerce.payment.MercadoPagoPaymentGateway;
import com.ventas.eCommerce.payment.PaymentGateway.PaymentResult;
import com.ventas.eCommerce.payment.PaymentGateway.Status;
import com.ventas.eCommerce.payment.SimulatedPaymentGateway;
import com.ventas.eCommerce.security.AuthUser;
import com.ventas.eCommerce.services.OrderService;
import com.ventas.eCommerce.services.PaymentService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ObjectProvider<SimulatedPaymentGateway> simulatedGateway;
    private final ObjectProvider<MercadoPagoPaymentGateway> mercadoPagoGateway;

    /**
     * URL de retorno del checkout. MercadoPago agrega {@code payment_id} y {@code external_reference};
     * el pago se verifica contra la API (no se confía en el parámetro {@code status}).
     */
    @GetMapping("/return")
    public String paymentReturn(@RequestParam(name = "payment_id", required = false) String paymentId,
                                @RequestParam(name = "external_reference", required = false) String orderRef,
                                RedirectAttributes redirect) {
        Optional<PaymentResult> result = StringUtils.hasText(paymentId) && !"null".equals(paymentId)
                ? paymentService.processPayment(paymentId)
                : Optional.empty();

        Integer orderId = result.map(PaymentResult::orderId).orElseGet(() -> parseId(orderRef));
        if (orderId == null) {
            return "redirect:/orders";
        }

        Status status = result.map(PaymentResult::status).orElse(null);
        if (status == Status.APPROVED) {
            redirect.addFlashAttribute("success", "¡Pago aprobado! Gracias por tu compra.");
        } else if (status == Status.PENDING) {
            redirect.addFlashAttribute("info", "Tu pago está siendo procesado. Te avisaremos cuando se acredite.");
        } else {
            redirect.addFlashAttribute("error", "El pago no se completó. Podés reintentarlo desde el detalle del pedido.");
        }
        return "redirect:/orders/" + orderId;
    }

    /** Webhook de MercadoPago (formato nuevo con body JSON y formato IPN con query params). */
    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<Void> mercadoPagoWebhook(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "topic", required = false) String topic,
            @RequestParam(name = "data.id", required = false) String dataIdParam,
            @RequestParam(name = "id", required = false) String idParam,
            @RequestHeader(name = "x-signature", required = false) String signature,
            @RequestHeader(name = "x-request-id", required = false) String requestId,
            @RequestBody(required = false) JsonNode body) {

        MercadoPagoPaymentGateway gateway = mercadoPagoGateway.getIfAvailable();
        if (gateway == null) {
            return ResponseEntity.notFound().build();
        }

        String eventType = firstNonBlank(type, topic, body != null ? body.path("type").asText(null) : null);
        String paymentId = firstNonBlank(dataIdParam, idParam,
                body != null ? body.path("data").path("id").asText(null) : null);

        if (!"payment".equals(eventType) || paymentId == null) {
            return ResponseEntity.ok().build(); // otros eventos (merchant_order, etc.) se ignoran
        }
        if (!gateway.isValidWebhookSignature(signature, requestId, paymentId)) {
            log.warn("Webhook de MercadoPago con firma inválida para el pago {}", paymentId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        paymentService.processPayment(paymentId);
        return ResponseEntity.ok().build();
    }

    // --- Pasarela simulada (solo con payment.provider=simulated) ---

    @GetMapping("/simulated/{orderId}")
    public String simulatedCheckout(@AuthenticationPrincipal AuthUser user, @PathVariable Integer orderId,
                                    Model model) {
        requireSimulated();
        model.addAttribute("order", orderService.getForUser(orderId, user.id()));
        return "payment/simulated";
    }

    @PostMapping("/simulated/{orderId}")
    public String simulatedPay(@AuthenticationPrincipal AuthUser user, @PathVariable Integer orderId,
                               @RequestParam boolean approved) {
        SimulatedPaymentGateway gateway = requireSimulated();
        var order = orderService.getForUser(orderId, user.id());
        String paymentId = gateway.simulate(order.id(), order.total(), approved);
        return "redirect:/payments/return?payment_id=" + paymentId + "&external_reference=" + order.id();
    }

    private SimulatedPaymentGateway requireSimulated() {
        SimulatedPaymentGateway gateway = simulatedGateway.getIfAvailable();
        if (gateway == null) {
            throw new ResourceNotFoundException("La pasarela simulada no está habilitada");
        }
        return gateway;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static Integer parseId(String value) {
        try {
            return value != null ? Integer.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
