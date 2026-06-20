package com.pos.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyClosingRequest {
    private BigDecimal cashInHand = BigDecimal.ZERO;
    private String note;
}
