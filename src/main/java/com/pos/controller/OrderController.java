// src/main/java/com/pos/controller/OrderController.java
package com.pos.controller;

import com.pos.dto.request.OrderRequest;
import com.pos.entity.Order;
import com.pos.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        // Get current user ID from security context
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = 1L; // In real app, get from database
        return ResponseEntity.ok(orderService.createOrder(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/today")
    public ResponseEntity<List<Order>> getTodayOrders() {
        return ResponseEntity.ok(orderService.getTodayOrders());
    }
}