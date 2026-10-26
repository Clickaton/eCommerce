package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.security.AuthUser;
import com.ventas.eCommerce.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String cart(@AuthenticationPrincipal AuthUser user, Model model) {
        model.addAttribute("cart", cartService.getCart(user.id()));
        return "cart";
    }

    @PostMapping("/items")
    public String add(@AuthenticationPrincipal AuthUser user, @RequestParam Integer productId,
                      @RequestParam(defaultValue = "1") int quantity, RedirectAttributes redirect) {
        cartService.addItem(user.id(), productId, quantity);
        redirect.addFlashAttribute("success", "Producto agregado al carrito");
        return "redirect:/cart";
    }

    @PostMapping("/items/{productId}")
    public String update(@AuthenticationPrincipal AuthUser user, @PathVariable Integer productId,
                         @RequestParam int quantity) {
        cartService.updateQuantity(user.id(), productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/items/{productId}/remove")
    public String remove(@AuthenticationPrincipal AuthUser user, @PathVariable Integer productId,
                         RedirectAttributes redirect) {
        cartService.removeItem(user.id(), productId);
        redirect.addFlashAttribute("success", "Producto eliminado del carrito");
        return "redirect:/cart";
    }
}
