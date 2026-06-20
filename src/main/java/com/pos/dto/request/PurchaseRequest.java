package com.pos.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseRequest {
    private Long productId;
    private String supplierName;
    private String supplierPhone;
    private Integer quantity;
    private BigDecimal unitCost;
    private String purchaseDate;
    private String note;
}
