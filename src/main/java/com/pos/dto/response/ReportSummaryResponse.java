package com.pos.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReportSummaryResponse {
    private LocalDateTime start;
    private LocalDateTime end;
    private BigDecimal totalSales = BigDecimal.ZERO;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private BigDecimal totalProfit = BigDecimal.ZERO;
    private BigDecimal refundAmount = BigDecimal.ZERO;
    private BigDecimal refundProfitAdjustment = BigDecimal.ZERO;
    private BigDecimal purchaseCost = BigDecimal.ZERO;
    private BigDecimal profitMargin = BigDecimal.ZERO;
    private long totalOrders;
    private long refundCount;
    private long purchaseCount;
    private long itemsSold;
    private List<PaymentBreakdown> payments = new ArrayList<>();
    private List<ProductPerformance> topProducts = new ArrayList<>();
    private List<CashierPerformance> cashiers = new ArrayList<>();

    @Data
    public static class PaymentBreakdown {
        private String paymentMethod;
        private long orderCount;
        private BigDecimal totalAmount = BigDecimal.ZERO;
    }

    @Data
    public static class ProductPerformance {
        private Long productId;
        private String productName;
        private long quantity;
        private BigDecimal sales = BigDecimal.ZERO;
        private BigDecimal profit = BigDecimal.ZERO;
    }

    @Data
    public static class CashierPerformance {
        private Long userId;
        private String fullName;
        private String username;
        private long orderCount;
        private BigDecimal sales = BigDecimal.ZERO;
        private BigDecimal profit = BigDecimal.ZERO;
    }
}
