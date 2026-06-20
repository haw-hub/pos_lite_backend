package com.pos.controller;

import com.pos.dto.request.PurchaseRequest;
import com.pos.entity.Purchase;
import com.pos.enums.ShopFeature;
import com.pos.service.ShopFeatureService;
import com.pos.service.PurchaseService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {
    private final PurchaseService purchaseService;
    private final ShopFeatureService shopFeatureService;

    public PurchaseController(PurchaseService purchaseService, ShopFeatureService shopFeatureService) {
        this.purchaseService = purchaseService;
        this.shopFeatureService = shopFeatureService;
    }

    @GetMapping
    public List<Purchase> list() {
        shopFeatureService.require(username(), ShopFeature.STOCK_IN);
        return purchaseService.list(username());
    }

    @PostMapping
    public Purchase stockIn(@RequestBody PurchaseRequest request) {
        shopFeatureService.require(username(), ShopFeature.STOCK_IN);
        return purchaseService.stockIn(request, username());
    }

    private String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
