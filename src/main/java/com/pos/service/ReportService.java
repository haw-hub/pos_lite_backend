package com.pos.service;

import com.pos.dto.response.ReportSummaryResponse;
import com.pos.entity.Order;
import com.pos.entity.OrderItem;
import com.pos.enums.OrderStatus;
import com.pos.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReportService {
    private final OrderRepository orderRepository;
    private final CurrentUserService currentUserService;

    public ReportService(OrderRepository orderRepository, CurrentUserService currentUserService) {
        this.orderRepository = orderRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse summary(String username, LocalDate startDate, LocalDate endDate) {
        Long shopId = currentUserService.require(username).getShop().getId();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        var orders = orderRepository.findByShopIdAndCreatedAtBetweenOrderByCreatedAtDesc(shopId, start, end)
                .stream().filter(order -> order.getStatus() != OrderStatus.CANCELLED).toList();

        ReportSummaryResponse response = new ReportSummaryResponse();
        response.setStart(start);
        response.setEnd(end);
        Map<String, ReportSummaryResponse.PaymentBreakdown> payments = new LinkedHashMap<>();
        Map<Long, ReportSummaryResponse.ProductPerformance> products = new LinkedHashMap<>();
        Map<Long, ReportSummaryResponse.CashierPerformance> cashiers = new LinkedHashMap<>();

        for (Order order : orders) {
            BigDecimal orderSales = amount(order.getTotalAmount());
            BigDecimal orderProfit = amount(order.getTotalProfit());
            response.setTotalSales(response.getTotalSales().add(orderSales));
            response.setTotalProfit(response.getTotalProfit().add(orderProfit));

            String paymentMethod = order.getPaymentMethod().name();
            var payment = payments.computeIfAbsent(paymentMethod, key -> {
                var item = new ReportSummaryResponse.PaymentBreakdown();
                item.setPaymentMethod(key);
                return item;
            });
            payment.setOrderCount(payment.getOrderCount() + 1);
            payment.setTotalAmount(payment.getTotalAmount().add(orderSales));

            if (order.getCashier() != null) {
                var cashier = cashiers.computeIfAbsent(order.getCashier().getId(), key -> {
                    var item = new ReportSummaryResponse.CashierPerformance();
                    item.setUserId(key);
                    item.setFullName(order.getCashier().getFullName());
                    item.setUsername(order.getCashier().getUsername());
                    return item;
                });
                cashier.setOrderCount(cashier.getOrderCount() + 1);
                cashier.setSales(cashier.getSales().add(orderSales));
                cashier.setProfit(cashier.getProfit().add(orderProfit));
            }

            for (OrderItem orderItem : order.getItems()) {
                response.setItemsSold(response.getItemsSold() + orderItem.getQuantity());
                if (orderItem.getProduct() == null) continue;
                var product = products.computeIfAbsent(orderItem.getProduct().getId(), key -> {
                    var item = new ReportSummaryResponse.ProductPerformance();
                    item.setProductId(key);
                    item.setProductName(orderItem.getProduct().getName());
                    return item;
                });
                product.setQuantity(product.getQuantity() + orderItem.getQuantity());
                product.setSales(product.getSales().add(amount(orderItem.getTotalPrice())));
                product.setProfit(product.getProfit().add(amount(orderItem.getProfit())));
            }
        }

        response.setTotalOrders(orders.size());
        response.setTotalCost(response.getTotalSales().subtract(response.getTotalProfit()));
        if (response.getTotalSales().compareTo(BigDecimal.ZERO) > 0) {
            response.setProfitMargin(response.getTotalProfit()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(response.getTotalSales(), 2, RoundingMode.HALF_UP));
        }
        response.setPayments(payments.values().stream()
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount())).toList());
        response.setTopProducts(products.values().stream()
                .sorted((a, b) -> Long.compare(b.getQuantity(), a.getQuantity())).limit(5).toList());
        response.setCashiers(cashiers.values().stream()
                .sorted((a, b) -> b.getSales().compareTo(a.getSales())).toList());
        return response;
    }

    private BigDecimal amount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
