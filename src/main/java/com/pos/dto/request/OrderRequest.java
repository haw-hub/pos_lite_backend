package com.pos.dto.request;

import lombok.Data;
import com.pos.enums.PaymentMethod;
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