package com.ventas.eCommerce.controller.admin;

import com.ventas.eCommerce.enums.Rol;
import com.ventas.eCommerce.security.AuthUser;
import com.ventas.eCommerce.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @ModelAttribute("section")
    public String section() {
        return "users";
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        model.addAttribute("page", userService.search(q, pageable));
        model.addAttribute("q", q);
        model.addAttribute("roles", Rol.values());
        return "admin/users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@AuthenticationPrincipal AuthUser admin, @PathVariable Integer id,
                             @RequestParam Rol rol, RedirectAttributes redirect) {
        userService.changeRole(id, rol, admin.id());
        redirect.addFlashAttribute("success", "Rol actualizado");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/enabled")
    public String setEnabled(@AuthenticationPrincipal AuthUser admin, @PathVariable Integer id,
                             @RequestParam boolean enabled, RedirectAttributes redirect) {
        userService.setEnabled(id, enabled, admin.id());
        redirect.addFlashAttribute("success", enabled ? "Usuario habilitado" : "Usuario deshabilitado");
        return "redirect:/admin/users";
    }
}
