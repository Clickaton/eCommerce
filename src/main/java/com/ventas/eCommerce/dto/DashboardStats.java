package com.ventas.eCommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStats(
        long activeProducts,
        long lowStockProducts,
        long customers,
        long pendingOrders,
        long paidOrders,
        long shippedOrders,
        BigDecimal revenue,
        List<OrderResponse> latestOrders,
        List<ProductResponse> lowStock) {
}
