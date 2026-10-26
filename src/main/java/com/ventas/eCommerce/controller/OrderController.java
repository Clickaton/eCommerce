package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.security.AuthUser;
import com.ventas.eCommerce.services.OrderService;
import com.ventas.eCommerce.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @GetMapping
    public String myOrders(@AuthenticationPrincipal AuthUser user, @PageableDefault(size = 10) Pageable pageable,
                           Model model) {
        model.addAttribute("page", orderService.findByUser(user.id(), pageable));
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal AuthUser user, @PathVariable Integer id, Model model) {
        model.addAttribute("order", orderService.getForUser(id, user.id()));
        return "orders/detail";
    }

    @PostMapping("/{id}/pay")
    public String pay(@AuthenticationPrincipal AuthUser user, @PathVariable Integer id) {
        return "redirect:" + paymentService.startPayment(id, user.id());
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@AuthenticationPrincipal AuthUser user, @PathVariable Integer id,
                         RedirectAttributes redirect) {
        orderService.cancelByUser(id, user.id());
        redirect.addFlashAttribute("success", "Pedido #" + id + " cancelado");
        return "redirect:/orders/" + id;
    }
}
