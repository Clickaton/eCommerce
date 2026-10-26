package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.dto.CartResponse;
import com.ventas.eCommerce.dto.CheckoutForm;
import com.ventas.eCommerce.dto.OrderResponse;
import com.ventas.eCommerce.exceptions.BusinessException;
import com.ventas.eCommerce.exceptions.PaymentException;
import com.ventas.eCommerce.security.AuthUser;
import com.ventas.eCommerce.services.CartService;
import com.ventas.eCommerce.services.OrderService;
import com.ventas.eCommerce.services.PaymentService;
import com.ventas.eCommerce.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserService userService;

    @GetMapping
    public String checkoutForm(@AuthenticationPrincipal AuthUser user, Model model, RedirectAttributes redirect) {
        CartResponse cart = cartService.getCart(user.id());
        if (cart.isEmpty()) {
            redirect.addFlashAttribute("error", "Tu carrito está vacío");
            return "redirect:/cart";
        }
        var profile = userService.get(user.id());
        CheckoutForm form = new CheckoutForm();
        form.setRecipientName(profile.fullName());
        form.setPhone(profile.phone());
        model.addAttribute("form", form);
        model.addAttribute("cart", cart);
        return "checkout";
    }

    @PostMapping
    public String checkout(@AuthenticationPrincipal AuthUser user,
                           @Valid @ModelAttribute("form") CheckoutForm form, BindingResult result,
                           Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("cart", cartService.getCart(user.id()));
            return "checkout";
        }

        OrderResponse order;
        try {
            order = orderService.checkout(user.id(), form);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }

        try {
            return "redirect:" + paymentService.startPayment(order.id(), user.id());
        } catch (PaymentException e) {
            // El pedido queda pendiente con el stock reservado; el usuario puede reintentar el pago.
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/" + order.id();
        }
    }
}
