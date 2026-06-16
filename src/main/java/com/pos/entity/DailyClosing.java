package com.pos.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_closings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"shop_id", "business_date"})
})
@Data
public class DailyClosing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "closed_by_id", nullable = false)
    private User closedBy;

    @Column(nullable = false)
    private LocalDateTime closedAt;

    private BigDecimal totalSales;
    private BigDecimal totalCost;
    private BigDecimal totalProfit;
    private BigDecimal profitMargin;
    private Long totalOrders;
    private Long itemsSold;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String snapshotJson;

    @PrePersist
    protected void onCreate() {
        closedAt = LocalDateTime.now();
    }
}
