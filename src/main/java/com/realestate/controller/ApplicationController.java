package com.realestate.controller;

import com.realestate.entity.ApplicationStatus;
import com.realestate.service.ApplicationService;
import com.realestate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserService userService;

    // Анонимная заявка на просмотр — посетитель оставляет имя, телефон, сообщение.
    @PostMapping
    public String submit(@RequestParam Long listingId,
                         @RequestParam String name,
                         @RequestParam String phone,
                         @RequestParam(required = false) String message) {
        applicationService.submitAnonymousApplication(listingId, name, phone, message);
        return "redirect:/listings/" + listingId + "?applied";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id) {
        applicationService.updateStatus(id, ApplicationStatus.APPROVED);
        return "redirect:/profile";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id) {
        applicationService.updateStatus(id, ApplicationStatus.REJECTED);
        return "redirect:/profile";
    }

    // Удаление заявки (POST /applications/{id}/delete).
    // Подтверждение делается на фронте через confirm() в шаблоне.
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails) {
        var me = userService.findByEmail(userDetails.getUsername());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        try {
            applicationService.deleteApplication(id, me.getId(), isAdmin);
            return "redirect:/profile?appDeleted";
        } catch (IllegalStateException forbidden) {
            return "redirect:/profile?appForbidden";
        }
    }
}
