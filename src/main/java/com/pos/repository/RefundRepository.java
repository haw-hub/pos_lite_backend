package com.pos.repository;

import com.pos.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByShopIdAndCreatedAtBetween(Long shopId, LocalDateTime start, LocalDateTime end);
    List<Refund> findByOrderIdAndShopId(Long orderId, Long shopId);
}
