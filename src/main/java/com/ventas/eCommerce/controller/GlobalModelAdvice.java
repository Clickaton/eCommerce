package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.enums.Category;
import com.ventas.eCommerce.security.AuthUser;
import com.ventas.eCommerce.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Datos comunes a todas las vistas (navbar): usuario actual, cantidad en el carrito y categorías.
 * También aplica al controlador de errores de Spring Boot, que renderiza error.html con el mismo layout.
 */
@ControllerAdvice(basePackages = {"com.ventas.eCommerce.controller",
        "org.springframework.boot.autoconfigure.web.servlet.error"})
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final CartService cartService;

    @ModelAttribute("currentUser")
    public AuthUser currentUser(@AuthenticationPrincipal AuthUser user) {
        return user;
    }

    @ModelAttribute("cartCount")
    public int cartCount(@AuthenticationPrincipal AuthUser user) {
        return user != null ? cartService.countItems(user.id()) : 0;
    }

    @ModelAttribute("categories")
    public Category[] categories() {
        return Category.values();
    }
}
