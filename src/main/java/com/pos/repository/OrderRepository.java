// src/main/java/com/pos/repository/OrderRepository.java
package com.pos.repository;

import com.pos.entity.Order;
import com.pos.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Find by status
    List<Order> findByStatus(OrderStatus status);

    // Find by cashier ID
    List<Order> findByCashierId(Long cashierId);

    // Find orders after specific date (for today's orders)
    List<Order> findByCreatedAtAfter(LocalDateTime date);

    // Find orders between dates
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find orders by status and date range
    List<Order> findByStatusAndCreatedAtAfter(OrderStatus status, LocalDateTime date);

    // Count orders today
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdAt) = CURRENT_DATE")
    long countTodayOrders();

    // Get total sales for today
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE DATE(o.createdAt) = CURRENT_DATE AND o.status = 'COMPLETED'")
    Double getTodaySalesTotal();
}