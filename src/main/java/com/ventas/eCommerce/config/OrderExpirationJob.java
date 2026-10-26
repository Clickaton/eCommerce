package com.ventas.eCommerce.config;

import com.ventas.eCommerce.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Libera periódicamente el stock reservado por pedidos que nunca se pagaron.
 */
@Component
@RequiredArgsConstructor
public class OrderExpirationJob {

    private final OrderService orderService;

    @Scheduled(fixedDelayString = "${app.orders.expiration-check:PT5M}", initialDelayString = "PT1M")
    public void expirePendingOrders() {
        orderService.expirePendingOrders();
    }
}
