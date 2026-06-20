// src/main/java/com/pos/service/OrderService.java
package com.pos.service;

import com.pos.dto.request.OrderRequest;
import com.pos.entity.*;
import com.pos.enums.OrderStatus;
import com.pos.enums.PaymentMethod;
import com.pos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final DebtRepository debtRepository;
    private final CurrentUserService currentUserService;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            DebtRepository debtRepository,
            CurrentUserService currentUserService
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.debtRepository = debtRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Order createOrder(OrderRequest request, String username) {
        User cashier = currentUserService.require(username);
        Long shopId = cashier.getShop().getId();
        if (request.getClientReference() != null && !request.getClientReference().isBlank()) {
            Order existing = orderRepository.findByShopIdAndClientReference(shopId, request.getClientReference()).orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Order order = new Order();
        order.setClientReference(request.getClientReference());
        order.setCashier(cashier);
        order.setShop(cashier.getShop());
        order.setPaymentMethod(request.getPaymentMethod());
        if (request.getPaymentMethod() == PaymentMethod.CREDIT) {
            order.setStatus(OrderStatus.PENDING);
        } else {
            order.setStatus(OrderStatus.COMPLETED);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null
                    || itemRequest.getQuantity() == null
                    || itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("Each order item must have a product and positive quantity");
            }

            Product product = productRepository.findByIdAndShopIdAndDeletedFalse(itemRequest.getProductId(), shopId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));

            if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(LocalDate.now())) {
                throw new RuntimeException("Product has expired: " + product.getName());
            }

            if (product.getCostPrice() == null
                    || product.getCostPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Cost price is required for product: " + product.getName());
            }

            // Check stock
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Update stock
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);

            BigDecimal unitPrice = itemRequest.getUnitPrice() != null
                    && itemRequest.getUnitPrice().compareTo(BigDecimal.ZERO) > 0
                    ? itemRequest.getUnitPrice()
                    : product.getPrice();

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setUnitCost(product.getCostPrice() == null ? BigDecimal.ZERO : product.getCostPrice());
            item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            item.setProfit(
                    item.getUnitPrice()
                            .subtract(item.getUnitCost())
                            .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
            );
            item.setOrder(order);

            order.getItems().add(item);
            subtotal = subtotal.add(item.getTotalPrice());
            totalProfit = totalProfit.add(item.getProfit());
        }

        order.setSubtotal(subtotal);
        order.setTax(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);
        order.setTotalProfit(totalProfit);

        if (request.getPaymentMethod() == PaymentMethod.CREDIT) {

            if (request.getCustomerPhone() == null
                    || request.getCustomerPhone().isBlank()) {

                throw new RuntimeException(
                        "Customer phone is required"
                );
            }

            if (request.getCustomerName() == null
                    || request.getCustomerName().isBlank()) {
                throw new RuntimeException("Customer name is required");
            }

            if (request.getDueDate() == null) {
                throw new RuntimeException("Debt due date is required");
            }

            Customer customer =
                    customerRepository.findByShopIdAndPhone(
                            shopId, request.getCustomerPhone()
                    ).orElse(null);

            if (customer == null) {

                customer = new Customer();
                customer.setOwner(cashier);
                customer.setShop(cashier.getShop());

                customer.setName(
                        request.getCustomerName()
                );

                customer.setPhone(
                        request.getCustomerPhone()
                );

                customer =
                        customerRepository.save(customer);
            }

            order.setCustomer(customer);
        }

        Order savedOrder = orderRepository.save(order);

        if (savedOrder.getPaymentMethod() == PaymentMethod.CREDIT) {

            Debt debt = new Debt();

            debt.setCustomer(savedOrder.getCustomer());

            debt.setOrder(savedOrder);

            debt.setTotalAmount(savedOrder.getTotalAmount());

            debt.setPaidAmount(BigDecimal.ZERO);

            debt.setRemainingAmount(savedOrder.getTotalAmount());

            debt.setDueDate(request.getDueDate());

            debt.setNote(request.getCreditNote());

            debtRepository.save(debt);
        }

        return savedOrder;
    }

    public List<Order> getTodayOrders(String username) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return orderRepository.findByShopIdAndCreatedAtAfter(shopId(username), startOfDay);
    }

    public List<Order> getAllOrders(String username) {
        return orderRepository.findByShopId(shopId(username));
    }

    public Order getOrderById(Long id, String username) {
        return orderRepository.findByIdAndShopId(id, shopId(username))
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public Order getOrderByNumber(String orderNumber, String username) {
        return orderRepository.findByOrderNumberAndShopId(orderNumber, shopId(username))
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
    }

    private Long shopId(String username) {
        return currentUserService.require(username).getShop().getId();
    }
}
