package com.ventas.eCommerce.payment;

import com.ventas.eCommerce.entities.Order;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Pasarela falsa para desarrollo y pruebas: muestra una pantalla propia donde se elige aprobar o
 * rechazar el pago. Se activa con {@code payment.provider=simulated} (valor por defecto).
 * No usar en producción.
 */
@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "simulated", matchIfMissing = true)
public class SimulatedPaymentGateway implements PaymentGateway {

    public static final String NAME = "simulated";

    private final Map<String, PaymentResult> payments = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public PaymentSession createPayment(Order order) {
        return new PaymentSession("SIM-PREF-" + UUID.randomUUID(), "/payments/simulated/" + order.getId());
    }

    @Override
    public Optional<PaymentResult> findPayment(String paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }

    /** Registra el resultado elegido en la pantalla simulada y devuelve el id de pago generado. */
    public String simulate(Integer orderId, BigDecimal amount, boolean approved) {
        String paymentId = "SIM-" + UUID.randomUUID();
        payments.put(paymentId, new PaymentResult(paymentId, orderId,
                approved ? Status.APPROVED : Status.REJECTED, amount));
        return paymentId;
    }
}
