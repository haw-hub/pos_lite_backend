package com.pos.controller;

import com.pos.dto.request.LoginRequest;
import com.pos.dto.response.SuperAdminAuthResponse;
import com.pos.dto.response.SuperAdminOverviewResponse;
import com.pos.dto.response.SuperAdminShopResponse;
import com.pos.dto.response.SubscriptionActivityResponse;
import com.pos.service.SuperAdminService;
import com.pos.service.SubscriptionActivityService;
import com.pos.service.TrialSettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminController {
    private final SuperAdminService superAdminService;
    private final TrialSettingsService trialSettingsService;
    private final SubscriptionActivityService activityService;

    public SuperAdminController(
            SuperAdminService superAdminService,
            TrialSettingsService trialSettingsService,
            SubscriptionActivityService activityService
    ) {
        this.superAdminService = superAdminService;
        this.trialSettingsService = trialSettingsService;
        this.activityService = activityService;
    }

    @PostMapping("/auth/login")
    public SuperAdminAuthResponse login(@Valid @RequestBody LoginRequest request) {
        return superAdminService.login(request);
    }

    @GetMapping("/overview")
    public SuperAdminOverviewResponse overview() {
        return superAdminService.overview();
    }

    @GetMapping("/settings")
    public Map<String, Integer> settings() {
        return Map.of("defaultTrialDays", trialSettingsService.getDefaultTrialDays());
    }

    @PutMapping("/settings/default-trial-days")
    public Map<String, Integer> setDefaultTrialDays(@RequestParam int days) {
        return Map.of("defaultTrialDays", trialSettingsService.setDefaultTrialDays(days));
    }

    @GetMapping("/shops")
    public List<SuperAdminShopResponse> shops() {
        return superAdminService.shops();
    }

    @GetMapping("/activities")
    public List<SubscriptionActivityResponse> activities() {
        return activityService.recent();
    }

    @GetMapping("/shops/{shopId}/activities")
    public List<SubscriptionActivityResponse> shopActivities(@PathVariable Long shopId) {
        return activityService.forShop(shopId);
    }

    @PostMapping("/shops/{shopId}/extend")
    public SuperAdminShopResponse extend(@PathVariable Long shopId, @RequestParam int months) {
        return superAdminService.extend(shopId, months);
    }

    @PostMapping("/shops/{shopId}/trial")
    public SuperAdminShopResponse trial(@PathVariable Long shopId, @RequestParam int days) {
        return superAdminService.setTrial(shopId, days);
    }

    @PostMapping("/shops/{shopId}/suspend")
    public SuperAdminShopResponse suspend(@PathVariable Long shopId, @RequestParam boolean value) {
        return superAdminService.setSuspended(shopId, value);
    }
}
