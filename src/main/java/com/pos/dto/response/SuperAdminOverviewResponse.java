package com.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuperAdminOverviewResponse {
    private long totalShops;
    private long trialShops;
    private long activeShops;
    private long expiredShops;
    private long suspendedShops;
    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
}
