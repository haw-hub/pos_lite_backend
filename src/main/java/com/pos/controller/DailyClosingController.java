package com.pos.controller;

import com.pos.dto.request.DailyClosingRequest;
import com.pos.dto.response.DailyClosingResponse;
import com.pos.enums.ShopFeature;
import com.pos.service.DailyClosingService;
import com.pos.service.ShopFeatureService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports/closings")
public class DailyClosingController {
    private final DailyClosingService dailyClosingService;
    private final ShopFeatureService shopFeatureService;

    public DailyClosingController(DailyClosingService dailyClosingService, ShopFeatureService shopFeatureService) {
        this.dailyClosingService = dailyClosingService;
        this.shopFeatureService = shopFeatureService;
    }

    @PostMapping("/{date}")
    public DailyClosingResponse close(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody(required = false) DailyClosingRequest request
    ) {
        shopFeatureService.require(username(), ShopFeature.DAILY_CLOSING);
        return dailyClosingService.close(date, username(), request);
    }

    @GetMapping("/{date}")
    public ResponseEntity<DailyClosingResponse> get(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        shopFeatureService.require(username(), ShopFeature.DAILY_CLOSING);
        return dailyClosingService.find(date, username())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public List<DailyClosingResponse> recent() {
        shopFeatureService.require(username(), ShopFeature.DAILY_CLOSING);
        return dailyClosingService.recent(username());
    }

    private String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
