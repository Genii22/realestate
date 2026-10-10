package com.realestate.controller;

import com.realestate.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ListingService listingService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("topListings", listingService.getTopListings(6));
        return "index";
    }
}
