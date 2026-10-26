package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.enums.Category;
import com.ventas.eCommerce.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String catalogue(@RequestParam(required = false) String q,
                            @RequestParam(required = false) Category category,
                            @PageableDefault(size = 12, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
                            Model model) {
        model.addAttribute("page", productService.catalogue(q, category, pageable));
        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", category);
        return "products/catalogue";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("product", productService.getActive(id));
        return "products/detail";
    }
}
