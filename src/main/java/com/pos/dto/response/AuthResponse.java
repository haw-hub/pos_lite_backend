package com.pos.dto.response;

import com.pos.enums.SubscriptionStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private String role;
    private String fullName;
    private Long shopId;
    private String shopName;
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime trialEndsAt;
    private LocalDateTime subscriptionEndsAt;
}
