package com.realestate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Публичной регистрации нет — учётки риелторам выдаёт главный администратор.
// На сайте доступна только кнопка «Войти».
@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) model.addAttribute("error", "Неверный email или пароль");
        return "auth/login";
    }
}
