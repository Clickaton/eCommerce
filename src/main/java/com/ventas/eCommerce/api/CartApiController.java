package com.ventas.eCommerce.api;

import com.ventas.eCommerce.dto.ApiDtos.CartItemRequest;
import com.ventas.eCommerce.dto.ApiDtos.QuantityRequest;
import com.ventas.eCommerce.dto.CartResponse;
import com.ventas.eCommerce.security.AuthUser;
import com.ventas.eCommerce.services.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    @GetMapping
    public CartResponse get(@AuthenticationPrincipal AuthUser user) {
        return cartService.getCart(user.id());
    }

    @PostMapping("/items")
    public CartResponse add(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(user.id(), request.productId(), request.quantity());
    }

    @PutMapping("/items/{productId}")
    public CartResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Integer productId,
                               @Valid @RequestBody QuantityRequest request) {
        return cartService.updateQuantity(user.id(), productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse remove(@AuthenticationPrincipal AuthUser user, @PathVariable Integer productId) {
        return cartService.removeItem(user.id(), productId);
    }
}
