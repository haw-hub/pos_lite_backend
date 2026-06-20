package com.pos.dto.request;

import lombok.Data;

@Data
public class RefundRequest {
    private Long orderItemId;
    private Integer quantity;
    private String reason;
}
