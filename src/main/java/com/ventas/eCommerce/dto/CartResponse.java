package com.ventas.eCommerce.dto;

import com.ventas.eCommerce.entities.Cart;
import com.ventas.eCommerce.entities.CartItem;
import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<Item> items, BigDecimal total, int totalQuantity) {

    public record Item(
            Integer productId,
            String productName,
            String imageUrl,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal,
            int stock,
            boolean available) {

        static Item from(CartItem item) {
            var product = item.getProduct();
            return new Item(
                    product.getId(),
                    product.getName(),
                    product.hasImage() ? "/image/products/" + product.getId() : null,
                    product.getPrice(),
                    item.getQuantity(),
                    item.getSubtotal(),
                    product.getStock(),
                    product.isActive() && product.getStock() >= item.getQuantity());
        }
    }

    public static CartResponse from(Cart cart) {
        return new CartResponse(
                cart.getItems().stream().map(Item::from).toList(),
                cart.getTotal(),
                cart.getTotalQuantity());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Indica si algún item quedó sin stock suficiente o fue dado de baja. */
    public boolean hasUnavailableItems() {
        return items.stream().anyMatch(item -> !item.available());
    }
}
