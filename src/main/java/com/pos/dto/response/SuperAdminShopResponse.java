package com.pos.dto.response;

import com.pos.entity.Shop;
import com.pos.enums.SubscriptionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SuperAdminShopResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private boolean active;
    private SubscriptionStatus status;
    private LocalDateTime trialEndsAt;
    private LocalDateTime subscriptionEndsAt;
    private LocalDateTime createdAt;
    private long users;
    private long products;
    private long orders;

    public static SuperAdminShopResponse from(
            Shop shop,
            SubscriptionStatus effectiveStatus,
            long users,
            long products,
            long orders
    ) {
        SuperAdminShopResponse response = new SuperAdminShopResponse();
        response.setId(shop.getId());
        response.setName(shop.getName());
        response.setPhone(shop.getPhone());
        response.setAddress(shop.getAddress());
        response.setActive(shop.isActive());
        response.setStatus(effectiveStatus);
        response.setTrialEndsAt(shop.getTrialEndsAt());
        response.setSubscriptionEndsAt(shop.getSubscriptionEndsAt());
        response.setCreatedAt(shop.getCreatedAt());
        response.setUsers(users);
        response.setProducts(products);
        response.setOrders(orders);
        return response;
    }
}
