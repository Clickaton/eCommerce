package com.ventas.eCommerce.dto;

import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.enums.Category;
import java.math.BigDecimal;

public record ProductResponse(
        Integer id,
        String name,
        String description,
        String brand,
        BigDecimal price,
        Category category,
        String categoryLabel,
        int stock,
        boolean active,
        boolean available,
        String imageUrl) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getPrice(),
                product.getCategory(),
                product.getCategory() != null ? product.getCategory().getLabel() : null,
                product.getStock(),
                product.isActive(),
                product.isAvailable(),
                product.hasImage() ? "/image/products/" + product.getId() : null);
    }
}
