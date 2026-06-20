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
    private BigDecimal cashExpected;
    private BigDecimal cashInHand;
    private BigDecimal cashDifference;
    private BigDecimal digitalPayTotal;
    private BigDecimal creditTotal;
    private BigDecimal refundAmount;
    private Long totalOrders;
    private Long itemsSold;
    private String note;
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
        response.setCashExpected(closing.getCashExpected());
        response.setCashInHand(closing.getCashInHand());
        response.setCashDifference(closing.getCashDifference());
        response.setDigitalPayTotal(closing.getDigitalPayTotal());
        response.setCreditTotal(closing.getCreditTotal());
        response.setRefundAmount(closing.getRefundAmount());
        response.setTotalOrders(closing.getTotalOrders());
        response.setItemsSold(closing.getItemsSold());
        response.setNote(closing.getNote());
        response.setSummary(summary);
        return response;
    }
}
