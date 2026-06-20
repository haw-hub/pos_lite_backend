package com.pos.dto.response;

import com.pos.entity.Shop;
import com.pos.enums.ShopFeature;
import com.pos.enums.SubscriptionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class SubscriptionStatusResponse {
    private Long shopId;
    private String shopName;
    private SubscriptionStatus status;
    private LocalDateTime trialEndsAt;
    private LocalDateTime subscriptionEndsAt;
    private LocalDateTime serverTime;
    private boolean canUseApp;
    private Set<ShopFeature> enabledFeatures;

    public static SubscriptionStatusResponse from(
            Shop shop,
            SubscriptionStatus status,
            boolean canUseApp
    ) {
        SubscriptionStatusResponse response = new SubscriptionStatusResponse();
        response.setShopId(shop.getId());
        response.setShopName(shop.getName());
        response.setStatus(status);
        response.setTrialEndsAt(shop.getTrialEndsAt());
        response.setSubscriptionEndsAt(shop.getSubscriptionEndsAt());
        response.setServerTime(LocalDateTime.now());
        response.setCanUseApp(canUseApp);
        response.setEnabledFeatures(shop.getEnabledFeatures());
        return response;
    }
}
