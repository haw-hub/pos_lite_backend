package com.pos.dto.response;

import com.pos.entity.SubscriptionActivity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionActivityResponse {
    private Long id;
    private Long shopId;
    private String shopName;
    private String action;
    private Integer value;
    private String detail;
    private LocalDateTime previousEndsAt;
    private LocalDateTime newEndsAt;
    private LocalDateTime createdAt;

    public static SubscriptionActivityResponse from(SubscriptionActivity activity) {
        SubscriptionActivityResponse response = new SubscriptionActivityResponse();
        response.setId(activity.getId());
        response.setShopId(activity.getShop().getId());
        response.setShopName(activity.getShop().getName());
        response.setAction(activity.getAction());
        response.setValue(activity.getValue());
        response.setDetail(activity.getDetail());
        response.setPreviousEndsAt(activity.getPreviousEndsAt());
        response.setNewEndsAt(activity.getNewEndsAt());
        response.setCreatedAt(activity.getCreatedAt());
        return response;
    }
}
