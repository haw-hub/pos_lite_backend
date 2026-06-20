package com.pos.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchases")
@Data
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Shop shop;

    @ManyToOne(optional = false)
    private Product product;

    @ManyToOne
    private Supplier supplier;

    @ManyToOne(optional = false)
    private User createdBy;

    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private String note;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
