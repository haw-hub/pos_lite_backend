// src/main/java/com/pos/dto/request/OrderRequest.java
package com.pos.dto.request;

import com.pos.enums.PaymentMethod;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private List<OrderItemRequest> items;
    private PaymentMethod paymentMethod;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}