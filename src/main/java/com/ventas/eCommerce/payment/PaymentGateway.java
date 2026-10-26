package com.ventas.eCommerce.payment;

import com.ventas.eCommerce.entities.Order;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Abstracción de la pasarela de pagos. La aplicación nunca recibe ni guarda datos de tarjeta:
 * redirige al usuario al checkout del proveedor y luego consulta el resultado del pago por su id.
 */
public interface PaymentGateway {

    /** Nombre corto del proveedor, guardado en el pedido (ej. "mercadopago"). */
    String name();

    /** Crea la sesión de pago para el pedido y devuelve a dónde redirigir al usuario. */
    PaymentSession createPayment(Order order);

    /**
     * Consulta un pago directamente a la pasarela. Nunca se confía en los datos que llegan en la
     * notificación o en la URL de retorno: siempre se verifica contra el proveedor.
     */
    Optional<PaymentResult> findPayment(String paymentId);

    record PaymentSession(String reference, String redirectUrl) {
    }

    record PaymentResult(String paymentId, Integer orderId, Status status, BigDecimal amount) {
    }

    enum Status {
        APPROVED,
        PENDING,
        REJECTED
    }
}
