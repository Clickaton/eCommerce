package com.ventas.eCommerce.dto;

import com.ventas.eCommerce.enums.OrderStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpos de request/response propios de la API REST.
 */
public final class ApiDtos {

    private ApiDtos() {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
    }

    public record CartItemRequest(
            @NotNull Integer productId,
            @NotNull @Min(1) @Max(999) Integer quantity) {
    }

    public record QuantityRequest(@NotNull @Min(0) @Max(999) Integer quantity) {
    }

    public record CheckoutResponse(Integer orderId, String paymentUrl) {
    }

    public record StatusUpdateRequest(@NotNull OrderStatus status) {
    }
}
