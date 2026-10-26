package com.ventas.eCommerce.controller.admin;

import com.ventas.eCommerce.enums.OrderStatus;
import com.ventas.eCommerce.services.OrderService;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @ModelAttribute("section")
    public String section() {
        return "orders";
    }

    @ModelAttribute("statuses")
    public OrderStatus[] statuses() {
        return OrderStatus.values();
    }

    @GetMapping
    public String list(@RequestParam(required = false) OrderStatus status,
                       @PageableDefault(size = 20) Pageable pageable, Model model) {
        model.addAttribute("page", orderService.search(status, pageable));
        model.addAttribute("selectedStatus", status);
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        var order = orderService.get(id);
        model.addAttribute("order", order);
        model.addAttribute("nextStatuses", Arrays.stream(OrderStatus.values())
                .filter(order.status()::canTransitionTo)
                .toList());
        return "admin/order-detail";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Integer id, @RequestParam OrderStatus status,
                               RedirectAttributes redirect) {
        orderService.changeStatus(id, status);
        redirect.addFlashAttribute("success", "Pedido #" + id + " actualizado a \"" + status.getLabel() + "\"");
        return "redirect:/admin/orders/" + id;
    }
}
