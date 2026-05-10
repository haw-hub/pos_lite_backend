// src/main/java/com/pos/repository/OrderRepository.java
package com.pos.repository;

import com.pos.entity.Order;
import com.pos.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by status
    List<Order> findByStatus(OrderStatus status);

    // Find orders created after specific date (for today's orders)
    List<Order> findByCreatedAtAfter(LocalDateTime date);

    // Find orders by order number
    Order findByOrderNumber(String orderNumber);

    // Find orders between dates
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Count orders by status
    long countByStatus(OrderStatus status);

    // Get total sales amount for today
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE DATE(o.createdAt) = CURRENT_DATE AND o.status = 'COMPLETED'")
    Double getTodaySalesTotal();

    // Get total sales amount for date range
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'COMPLETED'")
    Double getSalesTotalBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Get today's order count
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdAt) = CURRENT_DATE")
    Long getTodayOrderCount();
}