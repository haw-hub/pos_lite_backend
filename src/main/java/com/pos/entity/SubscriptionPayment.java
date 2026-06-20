package com.pos.entity;

import com.pos.enums.SubscriptionPaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_payments")
@Data
public class SubscriptionPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPaymentStatus status = SubscriptionPaymentStatus.UNPAID;

    @Column(nullable = false)
    private Integer months = 1;

    @Column(nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    private String paymentMethod;

    @Column(columnDefinition = "TEXT CHARACTER SET utf8mb4")
    private String notes;

    private String screenshotPath;
    private String screenshotUrl;
    private LocalDateTime paidAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
