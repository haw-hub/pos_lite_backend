package com.pos.dto.response;

import com.pos.entity.SubscriptionPayment;
import com.pos.enums.SubscriptionPaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SubscriptionPaymentResponse {
    private Long id;
    private Long shopId;
    private String shopName;
    private SubscriptionPaymentStatus status;
    private Integer months;
    private BigDecimal amount;
    private String paymentMethod;
    private String notes;
    private String screenshotUrl;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public static SubscriptionPaymentResponse from(SubscriptionPayment payment) {
        SubscriptionPaymentResponse response = new SubscriptionPaymentResponse();
        response.setId(payment.getId());
        response.setShopId(payment.getShop().getId());
        response.setShopName(payment.getShop().getName());
        response.setStatus(payment.getStatus());
        response.setMonths(payment.getMonths());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setNotes(payment.getNotes());
        response.setScreenshotUrl(payment.getScreenshotUrl());
        response.setPaidAt(payment.getPaidAt());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}
