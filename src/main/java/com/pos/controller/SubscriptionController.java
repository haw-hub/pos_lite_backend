package com.pos.controller;

import com.pos.dto.response.SubscriptionStatusResponse;
import com.pos.entity.User;
import com.pos.service.CurrentUserService;
import com.pos.service.SubscriptionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {
    private final CurrentUserService currentUserService;
    private final SubscriptionService subscriptionService;

    public SubscriptionController(CurrentUserService currentUserService, SubscriptionService subscriptionService) {
        this.currentUserService = currentUserService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/status")
    public SubscriptionStatusResponse status() {
        User user = currentUserService.requireAccountOnly(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        return SubscriptionStatusResponse.from(
                user.getShop(),
                subscriptionService.effectiveStatus(user.getShop()),
                subscriptionService.canUseApp(user.getShop())
        );
    }
}
