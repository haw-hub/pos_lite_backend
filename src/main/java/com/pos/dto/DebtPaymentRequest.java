package com.pos.dto;

import com.pos.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DebtPaymentRequest {

    private BigDecimal amount;

    private PaymentMethod paymentMethod;
}