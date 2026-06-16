package com.pos.service;

import com.pos.dto.request.LoginRequest;
import com.pos.dto.response.SuperAdminAuthResponse;
import com.pos.dto.response.SuperAdminOverviewResponse;
import com.pos.dto.response.SuperAdminShopResponse;
import com.pos.entity.Shop;
import com.pos.enums.SubscriptionStatus;
import com.pos.repository.OrderRepository;
import com.pos.repository.ProductRepository;
import com.pos.repository.ShopRepository;
import com.pos.repository.UserRepository;
import com.pos.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SuperAdminService {
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final SubscriptionService subscriptionService;
    private final JwtUtil jwtUtil;
    private final String username;
    private final String password;

    public SuperAdminService(
            ShopRepository shopRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            SubscriptionService subscriptionService,
            JwtUtil jwtUtil,
            @Value("${super-admin.username}") String username,
            @Value("${super-admin.password}") String password
    ) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.subscriptionService = subscriptionService;
        this.jwtUtil = jwtUtil;
        this.username = username;
        this.password = password;
    }

    public SuperAdminAuthResponse login(LoginRequest request) {
        if (!username.equals(request.getUsername()) || !password.equals(request.getPassword())) {
            throw new IllegalArgumentException("Invalid super admin credentials");
        }
        return new SuperAdminAuthResponse(jwtUtil.generateSuperAdminToken(username), username, "SUPER_ADMIN");
    }

    public List<SuperAdminShopResponse> shops() {
        return shopRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SuperAdminOverviewResponse overview() {
        List<SuperAdminShopResponse> shops = shops();
        return new SuperAdminOverviewResponse(
                shops.size(),
                countStatus(shops, SubscriptionStatus.TRIAL),
                countStatus(shops, SubscriptionStatus.ACTIVE),
                countStatus(shops, SubscriptionStatus.EXPIRED),
                countStatus(shops, SubscriptionStatus.SUSPENDED),
                userRepository.count(),
                productRepository.count(),
                orderRepository.count()
        );
    }

    @Transactional
    public SuperAdminShopResponse extend(Long shopId, int months) {
        return toResponse(subscriptionService.extendMonths(requireShop(shopId), months));
    }

    @Transactional
    public SuperAdminShopResponse setTrial(Long shopId, int days) {
        return toResponse(subscriptionService.setTrialDays(requireShop(shopId), days));
    }

    @Transactional
    public SuperAdminShopResponse setSuspended(Long shopId, boolean suspended) {
        return toResponse(subscriptionService.setSuspended(requireShop(shopId), suspended));
    }

    private Shop requireShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));
    }

    private SuperAdminShopResponse toResponse(Shop shop) {
        return SuperAdminShopResponse.from(
                shop,
                subscriptionService.effectiveStatus(shop),
                userRepository.countByShopId(shop.getId()),
                productRepository.countByShopIdAndDeletedFalse(shop.getId()),
                orderRepository.countByShopId(shop.getId())
        );
    }

    private long countStatus(List<SuperAdminShopResponse> shops, SubscriptionStatus status) {
        return shops.stream().filter(shop -> shop.getStatus() == status).count();
    }
}
