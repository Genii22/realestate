package com.realestate.controller;

import com.realestate.entity.DealType;
import com.realestate.entity.Role;
import com.realestate.entity.User;
import com.realestate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
public class RealtorController {

    private final UserRepository userRepository;

    // Публичный список риелторов агентства — две секции: аренда и продажа.
    @GetMapping("/realtors")
    public String realtors(Model model) {
        List<User> all = Stream.concat(
                userRepository.findByRole(Role.SELLER).stream(),
                userRepository.findByRole(Role.AGENT).stream()
        ).toList();

        model.addAttribute("rentRealtors",
                all.stream().filter(u -> u.getSpecialization() == DealType.RENT).toList());
        model.addAttribute("saleRealtors",
                all.stream().filter(u -> u.getSpecialization() == DealType.SALE).toList());
        return "realtors";
    }
}
