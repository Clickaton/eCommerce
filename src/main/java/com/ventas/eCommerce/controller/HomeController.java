package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("featured", productService.featured());
        return "index";
    }
}
