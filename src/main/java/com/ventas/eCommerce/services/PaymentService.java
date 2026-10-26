package com.ventas.eCommerce.services;

import com.ventas.eCommerce.entities.Order;
import com.ventas.eCommerce.payment.PaymentGateway;
import com.ventas.eCommerce.payment.PaymentGateway.PaymentResult;
import com.ventas.eCommerce.payment.PaymentGateway.PaymentSession;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGateway gateway;
    private final OrderService orderService;

    /**
     * Inicia el pago de un pedido pendiente y devuelve la URL a la que hay que redirigir al usuario.
     * La llamada a la pasarela se hace fuera de una transacción de base de datos.
     */
    public String startPayment(Integer orderId, Integer userId) {
        Order order = orderService.getPayableOrder(orderId, userId);
        PaymentSession session = gateway.createPayment(order);
        orderService.registerPaymentAttempt(orderId, gateway.name(), session.reference());
        return session.redirectUrl();
    }

    /**
     * Procesa una notificación de pago (webhook o retorno del checkout). Siempre consulta el pago a
     * la pasarela en lugar de confiar en los parámetros recibidos.
     */
    public Optional<PaymentResult> processPayment(String paymentId) {
        Optional<PaymentResult> result = gateway.findPayment(paymentId);
        if (result.isEmpty()) {
            log.warn("Notificación de pago {} desconocido para {}", paymentId, gateway.name());
            return result;
        }

        PaymentResult payment = result.get();
        switch (payment.status()) {
            case APPROVED -> {
                try {
                    orderService.markPaid(payment.orderId(), payment.paymentId(), payment.amount());
                } catch (ObjectOptimisticLockingFailureException e) {
                    // Webhook y retorno llegaron a la vez: el otro hilo ya actualizó el pedido.
                    log.info("Pago {} procesado concurrentemente", paymentId);
                }
            }
            // Un rechazo no cancela el pedido: el usuario puede reintentar con otro medio mientras dure la reserva.
            case REJECTED -> log.info("Pago {} rechazado para el pedido {}", paymentId, payment.orderId());
            case PENDING -> log.info("Pago {} pendiente para el pedido {}", paymentId, payment.orderId());
        }
        return result;
    }

    public String providerName() {
        return gateway.name();
    }
}
