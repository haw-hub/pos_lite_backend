package com.pos.controller;

import com.pos.dto.response.ReportSummaryResponse;
import com.pos.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ReportSummaryResponse summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        if (end.isBefore(start)) throw new IllegalArgumentException("End date must be after start date");
        return reportService.summary(username(), start, end);
    }

    private String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
