// src/main/java/com/pos/service/OrderService.java
package com.pos.service;

import com.pos.dto.request.OrderRequest;
import com.pos.entity.Order;
import com.pos.entity.OrderItem;
import com.pos.entity.Product;
import com.pos.enums.OrderStatus;
import com.pos.enums.PaymentMethod;
import com.pos.repository.OrderRepository;
import com.pos.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder(OrderRequest request, Long userId) {
        Order order = new Order();
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatus.COMPLETED);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Update stock
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            item.setOrder(order);

            order.getItems().add(item);
            subtotal = subtotal.add(item.getTotalPrice());
        }

        order.setSubtotal(subtotal);
        order.setTax(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);

        return orderRepository.save(order);
    }

    public List<Order> getTodayOrders() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return orderRepository.findByCreatedAtAfter(startOfDay);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}