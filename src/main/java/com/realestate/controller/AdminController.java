package com.realestate.controller;

import com.realestate.entity.DealType;
import com.realestate.entity.Role;
import com.realestate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// Админ-панель: главный администратор управляет аккаунтами риелторов.
// Доступ — только ADMIN (см. SecurityConfig: /admin/** hasRole ADMIN).
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    // Список пользователей + форма создания нового риелтора.
    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        return users(userDetails, model);
    }

    @GetMapping("/users")
    public String users(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", new Role[]{Role.SELLER, Role.AGENT});
        model.addAttribute("specializations", DealType.values());
        if (userDetails != null) {
            model.addAttribute("currentEmail", userDetails.getUsername());
        }
        return "admin/users";
    }

    @PostMapping("/users")
    public String createRealtor(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String phone,
                                @RequestParam Role role,
                                @RequestParam DealType specialization,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        try {
            userService.register(firstName, lastName, email, password, phone, role, specialization);
            return "redirect:/admin/users?created";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return users(userDetails, model);
        }
    }

    // Удаление риелтора. Защищено правилами в UserService.deleteUser.
    @PostMapping("/users/{id}/delete")
    public String deleteRealtor(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes attrs) {
        var me = userService.findByEmail(userDetails.getUsername());
        try {
            userService.deleteUser(id, me.getId());
            attrs.addAttribute("deleted", "");
        } catch (IllegalStateException e) {
            attrs.addAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
