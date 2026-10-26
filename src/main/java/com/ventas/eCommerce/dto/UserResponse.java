package com.ventas.eCommerce.dto;

import com.ventas.eCommerce.entities.User;
import com.ventas.eCommerce.enums.Rol;
import java.time.LocalDateTime;

public record UserResponse(
        Integer id,
        String name,
        String lastName,
        String fullName,
        String email,
        String phone,
        Rol rol,
        boolean enabled,
        String imageUrl,
        LocalDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastName(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRol(),
                user.isEnabled(),
                user.hasImage() ? "/image/users/" + user.getId() : null,
                user.getCreatedAt());
    }
}
