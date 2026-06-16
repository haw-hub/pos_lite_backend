package com.pos.controller;

import com.pos.dto.response.DailyClosingResponse;
import com.pos.service.DailyClosingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports/closings")
public class DailyClosingController {
    private final DailyClosingService dailyClosingService;

    public DailyClosingController(DailyClosingService dailyClosingService) {
        this.dailyClosingService = dailyClosingService;
    }

    @PostMapping("/{date}")
    public DailyClosingResponse close(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return dailyClosingService.close(date, username());
    }

    @GetMapping("/{date}")
    public DailyClosingResponse get(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return dailyClosingService.get(date, username());
    }

    @GetMapping
    public List<DailyClosingResponse> recent() {
        return dailyClosingService.recent(username());
    }

    private String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
