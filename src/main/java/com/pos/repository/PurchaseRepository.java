package com.pos.repository;

import com.pos.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByShopIdOrderByCreatedAtDesc(Long shopId);
    List<Purchase> findByShopIdAndCreatedAtBetween(Long shopId, LocalDateTime start, LocalDateTime end);
}
