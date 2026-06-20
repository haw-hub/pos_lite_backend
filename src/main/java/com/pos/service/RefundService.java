package com.pos.service;

import com.pos.dto.request.RefundRequest;
import com.pos.entity.*;
import com.pos.enums.OrderStatus;
import com.pos.enums.UserRole;
import com.pos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RefundService {
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public RefundService(
            RefundRepository refundRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository,
            CurrentUserService currentUserService
    ) {
        this.refundRepository = refundRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    public List<Refund> listForOrder(Long orderId, String username) {
        User user = currentUserService.require(username);
        return refundRepository.findByOrderIdAndShopId(orderId, user.getShop().getId());
    }

    @Transactional
    public Refund refund(Long orderId, RefundRequest request, String username) {
        User user = currentUserService.require(username);
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.MANAGER) {
            throw new RuntimeException("Refund requires admin or manager approval");
        }
        if (request.getOrderItemId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Order item and positive quantity are required");
        }

        Order order = orderRepository.findByIdAndShopId(orderId, user.getShop().getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already fully refunded");
        }

        OrderItem item = order.getItems().stream()
                .filter(orderItem -> request.getOrderItemId().equals(orderItem.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        int refundedQuantity = refundRepository.findByOrderIdAndShopId(orderId, user.getShop().getId())
                .stream()
                .filter(refund -> refund.getOrderItem().getId().equals(item.getId()))
                .mapToInt(Refund::getQuantity)
                .sum();
        int refundable = item.getQuantity() - refundedQuantity;
        if (request.getQuantity() > refundable) {
            throw new RuntimeException("Refund quantity exceeds remaining refundable quantity");
        }

        BigDecimal quantity = BigDecimal.valueOf(request.getQuantity());
        BigDecimal amount = item.getUnitPrice().multiply(quantity);
        BigDecimal profitAdjustment = item.getUnitPrice().subtract(item.getUnitCost()).multiply(quantity);

        Product product = item.getProduct();
        product.setStock(product.getStock() + request.getQuantity());
        productRepository.save(product);

        order.setTotalAmount(order.getTotalAmount().subtract(amount));
        order.setSubtotal(order.getSubtotal().subtract(amount));
        order.setTotalProfit(order.getTotalProfit().subtract(profitAdjustment));

        boolean fullyRefunded = order.getItems().stream().allMatch(orderItem -> {
            int totalRefunded = refundRepository.findByOrderIdAndShopId(orderId, user.getShop().getId())
                    .stream()
                    .filter(refund -> refund.getOrderItem().getId().equals(orderItem.getId()))
                    .mapToInt(Refund::getQuantity)
                    .sum();
            if (orderItem.getId().equals(item.getId())) totalRefunded += request.getQuantity();
            return totalRefunded >= orderItem.getQuantity();
        });
        if (fullyRefunded) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        orderRepository.save(order);

        Refund refund = new Refund();
        refund.setShop(user.getShop());
        refund.setOrder(order);
        refund.setOrderItem(item);
        refund.setProduct(product);
        refund.setRefundedBy(user);
        refund.setQuantity(request.getQuantity());
        refund.setAmount(amount);
        refund.setProfitAdjustment(profitAdjustment);
        refund.setReason(request.getReason());
        return refundRepository.save(refund);
    }
}
