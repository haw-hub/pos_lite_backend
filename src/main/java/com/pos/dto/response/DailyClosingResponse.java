package com.pos.dto.response;

import com.pos.entity.DailyClosing;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DailyClosingResponse {
    private Long id;
    private LocalDate businessDate;
    private LocalDateTime closedAt;
    private Long closedById;
    private String closedByName;
    private BigDecimal totalSales;
    private BigDecimal totalCost;
    private BigDecimal totalProfit;
    private BigDecimal profitMargin;
    private Long totalOrders;
    private Long itemsSold;
    private ReportSummaryResponse summary;

    public static DailyClosingResponse from(DailyClosing closing, ReportSummaryResponse summary) {
        DailyClosingResponse response = new DailyClosingResponse();
        response.setId(closing.getId());
        response.setBusinessDate(closing.getBusinessDate());
        response.setClosedAt(closing.getClosedAt());
        response.setClosedById(closing.getClosedBy().getId());
        response.setClosedByName(closing.getClosedBy().getFullName());
        response.setTotalSales(closing.getTotalSales());
        response.setTotalCost(closing.getTotalCost());
        response.setTotalProfit(closing.getTotalProfit());
        response.setProfitMargin(closing.getProfitMargin());
        response.setTotalOrders(closing.getTotalOrders());
        response.setItemsSold(closing.getItemsSold());
        response.setSummary(summary);
        return response;
    }
}
