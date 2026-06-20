package com.pos.controller;

import com.pos.dto.request.LoginRequest;
import com.pos.dto.response.SuperAdminAuthResponse;
import com.pos.dto.response.SuperAdminOverviewResponse;
import com.pos.dto.response.SuperAdminShopResponse;
import com.pos.dto.response.ProductImportResponse;
import com.pos.dto.response.SubscriptionPaymentResponse;
import com.pos.enums.ShopFeature;
import com.pos.enums.SubscriptionPaymentStatus;
import com.pos.dto.response.SubscriptionActivityResponse;
import com.pos.service.ProductImportService;
import com.pos.service.SuperAdminService;
import com.pos.service.SubscriptionActivityService;
import com.pos.service.SubscriptionPaymentService;
import com.pos.service.TrialSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminController {
    private final SuperAdminService superAdminService;
    private final TrialSettingsService trialSettingsService;
    private final SubscriptionActivityService activityService;
    private final SubscriptionPaymentService paymentService;
    private final ProductImportService productImportService;

    public SuperAdminController(
            SuperAdminService superAdminService,
            TrialSettingsService trialSettingsService,
            SubscriptionActivityService activityService,
            SubscriptionPaymentService paymentService,
            ProductImportService productImportService
    ) {
        this.superAdminService = superAdminService;
        this.trialSettingsService = trialSettingsService;
        this.activityService = activityService;
        this.paymentService = paymentService;
        this.productImportService = productImportService;
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

    @GetMapping("/payments")
    public List<SubscriptionPaymentResponse> payments() {
        return paymentService.recent();
    }

    @GetMapping("/shops/{shopId}/payments")
    public List<SubscriptionPaymentResponse> shopPayments(@PathVariable Long shopId) {
        return paymentService.forShop(shopId);
    }

    @PostMapping(value = "/shops/{shopId}/payments", consumes = "multipart/form-data")
    public SubscriptionPaymentResponse createPayment(
            @PathVariable Long shopId,
            @RequestParam SubscriptionPaymentStatus status,
            @RequestParam(required = false) Integer months,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String notes,
            @RequestParam(defaultValue = "false") Boolean extendSubscription,
            @RequestPart(required = false) MultipartFile screenshot
    ) {
        return paymentService.create(shopId, status, months, amount, paymentMethod, notes, extendSubscription, screenshot);
    }

    @PostMapping("/payments/{paymentId}/confirm")
    public SubscriptionPaymentResponse confirmPayment(@PathVariable Long paymentId) {
        return paymentService.confirm(paymentId);
    }

    @PostMapping(value = "/shops/{shopId}/products/import", consumes = "multipart/form-data")
    public ProductImportResponse importProducts(
            @PathVariable Long shopId,
            @RequestPart MultipartFile file
    ) {
        return productImportService.importForShop(shopId, file);
    }

    @GetMapping("/products/import-template")
    public ResponseEntity<byte[]> productImportTemplate() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=product-import-template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(productImportService.createTemplate());
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

    @PutMapping("/shops/{shopId}/features")
    public SuperAdminShopResponse setFeatures(
            @PathVariable Long shopId,
            @RequestBody List<ShopFeature> features
    ) {
        return superAdminService.setFeatures(shopId, Set.copyOf(features));
    }
}
