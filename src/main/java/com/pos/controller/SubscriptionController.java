package com.pos.controller;

import com.pos.dto.response.SubscriptionStatusResponse;
import com.pos.dto.response.SubscriptionPaymentResponse;
import com.pos.entity.User;
import com.pos.service.CurrentUserService;
import com.pos.service.SubscriptionPaymentService;
import com.pos.service.SubscriptionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {
    private final CurrentUserService currentUserService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionPaymentService paymentService;

    public SubscriptionController(
            CurrentUserService currentUserService,
            SubscriptionService subscriptionService,
            SubscriptionPaymentService paymentService
    ) {
        this.currentUserService = currentUserService;
        this.subscriptionService = subscriptionService;
        this.paymentService = paymentService;
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

    @PostMapping(value = "/payment-proof", consumes = "multipart/form-data")
    public SubscriptionPaymentResponse submitPaymentProof(
            @RequestParam(required = false) Integer months,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String notes,
            @RequestPart MultipartFile screenshot
    ) {
        return paymentService.submitProof(
                SecurityContextHolder.getContext().getAuthentication().getName(),
                months,
                amount,
                paymentMethod,
                notes,
                screenshot
        );
    }
}
