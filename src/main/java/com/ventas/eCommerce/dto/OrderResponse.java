package com.ventas.eCommerce.dto;

import com.ventas.eCommerce.entities.Order;
import com.ventas.eCommerce.entities.OrderItem;
import com.ventas.eCommerce.entities.ShippingAddress;
import com.ventas.eCommerce.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Integer id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        OrderStatus status,
        String statusLabel,
        BigDecimal total,
        int totalQuantity,
        Customer customer,
        Address shippingAddress,
        String paymentProvider,
        String paymentId,
        List<Item> items) {

    public record Customer(Integer id, String fullName, String email) {
    }

    public record Address(String recipientName, String street, String city, String province,
                          String postalCode, String phone) {

        static Address from(ShippingAddress address) {
            if (address == null) {
                return null;
            }
            return new Address(address.getRecipientName(), address.getStreet(), address.getCity(),
                    address.getProvince(), address.getPostalCode(), address.getPhone());
        }

        @Override
        public String toString() {
            return street + ", " + city + " (CP " + postalCode + "), " + province;
        }
    }

    public record Item(Integer productId, String productName, BigDecimal unitPrice, int quantity,
                       BigDecimal subtotal) {

        static Item from(OrderItem item) {
            return new Item(
                    item.getProduct() != null ? item.getProduct().getId() : null,
                    item.getProductName(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getSubtotal());
        }
    }

    /** Resumen sin items, para listados. */
    public static OrderResponse summary(Order order) {
        return build(order, List.of());
    }

    public static OrderResponse from(Order order) {
        return build(order, order.getItems().stream().map(Item::from).toList());
    }

    private static OrderResponse build(Order order, List<Item> items) {
        var user = order.getUser();
        return new OrderResponse(
                order.getId(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getStatus(),
                order.getStatus().getLabel(),
                order.getTotal(),
                items.stream().mapToInt(Item::quantity).sum(),
                new Customer(user.getId(), user.getFullName(), user.getEmail()),
                Address.from(order.getShippingAddress()),
                order.getPaymentProvider(),
                order.getPaymentId(),
                items);
    }

    public boolean isPayable() {
        return status == OrderStatus.PENDING;
    }
}
