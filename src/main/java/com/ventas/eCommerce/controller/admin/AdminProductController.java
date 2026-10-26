package com.ventas.eCommerce.controller.admin;

import com.ventas.eCommerce.dto.ProductForm;
import com.ventas.eCommerce.enums.Category;
import com.ventas.eCommerce.exceptions.BusinessException;
import com.ventas.eCommerce.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @ModelAttribute("section")
    public String section() {
        return "products";
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Category category,
                       @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        model.addAttribute("page", productService.adminSearch(q, category, pageable));
        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", category);
        return "admin/products";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new ProductForm());
        return "admin/product-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ProductForm form, BindingResult result, Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "admin/product-form";
        }
        try {
            var product = productService.create(form);
            redirect.addFlashAttribute("success", "Producto \"" + product.name() + "\" creado");
            return "redirect:/admin/products";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/product-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("form", productService.getForm(id));
        return "admin/product-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @Valid @ModelAttribute("form") ProductForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        form.setId(id);
        if (result.hasErrors()) {
            return "admin/product-form";
        }
        try {
            var product = productService.update(id, form);
            redirect.addFlashAttribute("success", "Producto \"" + product.name() + "\" actualizado");
            return "redirect:/admin/products";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/product-form";
        }
    }

    @PostMapping("/{id}/active")
    public String setActive(@PathVariable Integer id, @RequestParam boolean active, RedirectAttributes redirect) {
        productService.setActive(id, active);
        redirect.addFlashAttribute("success", active ? "Producto publicado" : "Producto dado de baja");
        return "redirect:/admin/products";
    }
}
