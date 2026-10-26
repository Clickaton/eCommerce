package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.dto.ProfileForm;
import com.ventas.eCommerce.dto.RegistrationForm;
import com.ventas.eCommerce.exceptions.BusinessException;
import com.ventas.eCommerce.security.AuthUser;
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
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal AuthUser user) {
        return user != null ? "redirect:/" : "user/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult result,
                           Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "user/register";
        }
        try {
            userService.register(form);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "user/register";
        }
        redirect.addFlashAttribute("success", "¡Cuenta creada! Ya podés iniciar sesión.");
        return "redirect:/user/login";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal AuthUser user, Model model) {
        model.addAttribute("user", userService.get(user.id()));
        model.addAttribute("form", userService.getProfileForm(user.id()));
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal AuthUser user,
                                @Valid @ModelAttribute("form") ProfileForm form, BindingResult result,
                                Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                userService.updateProfile(user.id(), form);
                redirect.addFlashAttribute("success", "Perfil actualizado");
                return "redirect:/user/profile";
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("user", userService.get(user.id()));
        return "user/profile";
    }
}
