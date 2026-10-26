package com.ventas.eCommerce.controller.admin;

import com.ventas.eCommerce.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final OrderService orderService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", orderService.dashboardStats());
        model.addAttribute("section", "dashboard");
        return "admin/dashboard";
    }
}
