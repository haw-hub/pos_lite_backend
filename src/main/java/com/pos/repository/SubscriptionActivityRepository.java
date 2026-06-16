package com.pos.repository;

import com.pos.entity.SubscriptionActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionActivityRepository extends JpaRepository<SubscriptionActivity, Long> {
    List<SubscriptionActivity> findTop50ByOrderByCreatedAtDesc();
    List<SubscriptionActivity> findByShopIdOrderByCreatedAtDesc(Long shopId);
}
