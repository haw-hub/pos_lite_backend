// src/main/java/com/pos/controller/OrderController.java
package com.pos.controller;

import com.pos.dto.request.OrderRequest;
import com.pos.dto.request.RefundRequest;
import com.pos.entity.Order;
import com.pos.entity.Refund;
import com.pos.service.OrderService;
import com.pos.service.RefundService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final RefundService refundService;

    public OrderController(OrderService orderService, RefundService refundService) {
        this.orderService = orderService;
        this.refundService = refundService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(orderService.createOrder(request, username));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders(username()));
    }

    @GetMapping("/today")
    public ResponseEntity<List<Order>> getTodayOrders() {
        return ResponseEntity.ok(orderService.getTodayOrders(username()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id, username()));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber, username()));
    }

    @PostMapping("/{id}/refunds")
    public ResponseEntity<Refund> refund(@PathVariable Long id, @RequestBody RefundRequest request) {
        return ResponseEntity.ok(refundService.refund(id, request, username()));
    }

    @GetMapping("/{id}/refunds")
    public ResponseEntity<List<Refund>> refunds(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.listForOrder(id, username()));
    }

    private String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
