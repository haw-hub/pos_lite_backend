package com.pos.repository;

import com.pos.entity.SubscriptionPayment;
import com.pos.enums.SubscriptionPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {
    List<SubscriptionPayment> findTop50ByOrderByCreatedAtDesc();
    List<SubscriptionPayment> findByShopIdOrderByCreatedAtDesc(Long shopId);
    long countByStatus(SubscriptionPaymentStatus status);
    long countByShopIdAndStatus(Long shopId, SubscriptionPaymentStatus status);
}
