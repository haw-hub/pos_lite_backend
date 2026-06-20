package com.pos.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Data
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Shop shop;

    @ManyToOne(optional = false)
    private Order order;

    @ManyToOne(optional = false)
    private OrderItem orderItem;

    @ManyToOne(optional = false)
    private Product product;

    @ManyToOne(optional = false)
    private User refundedBy;

    private Integer quantity;
    private BigDecimal amount;
    private BigDecimal profitAdjustment;
    private String reason;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
