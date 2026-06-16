package com.pos.repository;

import com.pos.entity.DailyClosing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyClosingRepository extends JpaRepository<DailyClosing, Long> {
    Optional<DailyClosing> findByShopIdAndBusinessDate(Long shopId, LocalDate businessDate);
    List<DailyClosing> findTop31ByShopIdOrderByBusinessDateDesc(Long shopId);
}
