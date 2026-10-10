package com.realestate.controller;

import com.realestate.entity.User;
import com.realestate.service.ApplicationService;
import com.realestate.service.ListingService;
import com.realestate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final ListingService listingService;
    private final ApplicationService applicationService;

    @GetMapping
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("myListings", listingService.getMyListings(user.getId()));
        model.addAttribute("incomingApplications", applicationService.getIncomingApplications(user.getId()));
        return "profile/profile";
    }
}
