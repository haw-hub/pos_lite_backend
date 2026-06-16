package com.pos.service;

import com.pos.dto.response.SubscriptionActivityResponse;
import com.pos.entity.Shop;
import com.pos.entity.SubscriptionActivity;
import com.pos.repository.SubscriptionActivityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionActivityService {
    private final SubscriptionActivityRepository repository;

    public SubscriptionActivityService(SubscriptionActivityRepository repository) {
        this.repository = repository;
    }

    public void record(
            Shop shop,
            String action,
            Integer value,
            String detail,
            LocalDateTime previousEndsAt,
            LocalDateTime newEndsAt
    ) {
        SubscriptionActivity activity = new SubscriptionActivity();
        activity.setShop(shop);
        activity.setAction(action);
        activity.setValue(value);
        activity.setDetail(detail);
        activity.setPreviousEndsAt(previousEndsAt);
        activity.setNewEndsAt(newEndsAt);
        repository.save(activity);
    }

    public List<SubscriptionActivityResponse> recent() {
        return repository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(SubscriptionActivityResponse::from)
                .toList();
    }

    public List<SubscriptionActivityResponse> forShop(Long shopId) {
        return repository.findByShopIdOrderByCreatedAtDesc(shopId).stream()
                .map(SubscriptionActivityResponse::from)
                .toList();
    }
}
