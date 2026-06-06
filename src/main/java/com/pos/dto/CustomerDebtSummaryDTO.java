package com.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CustomerDebtSummaryDTO {

    private Long customerId;

    private String customerName;

    private String phone;

    private BigDecimal totalDebt;

    private Long orderCount;
}