package com.pos.service;

import com.pos.dto.DebtPaymentRequest;
import com.pos.entity.Debt;
import com.pos.entity.DebtPayment;
import com.pos.enums.OrderStatus;
import com.pos.repository.DebtPaymentRepository;
import com.pos.repository.DebtRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DebtService {

    private final DebtRepository debtRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final CurrentUserService currentUserService;

    public DebtService(
            DebtRepository debtRepository,
            DebtPaymentRepository debtPaymentRepository,
            CurrentUserService currentUserService
    ) {
        this.debtRepository = debtRepository;
        this.debtPaymentRepository = debtPaymentRepository;
        this.currentUserService = currentUserService;
    }
    @Transactional
    public Debt markPaid(Long debtId, String username) {

        Debt debt = debtRepository
                .findByIdAndOrderShopId(debtId, shopId(username))
                .orElseThrow();

        debt.setPaidAmount(
                debt.getTotalAmount()
        );

        debt.setRemainingAmount(
                BigDecimal.ZERO
        );

        if (debt.getOrder() != null) {
            debt.getOrder().setStatus(
                    OrderStatus.COMPLETED
            );
        }

        return debt;
    }

    @Transactional
    public Debt makePayment(
            Long debtId,
            DebtPaymentRequest request,
            String username
    ) {

        Debt debt = debtRepository
                .findByIdAndOrderShopId(debtId, shopId(username))
                .orElseThrow();

        if (request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        if (request.getPaymentMethod() == null
                || request.getPaymentMethod() == com.pos.enums.PaymentMethod.CREDIT) {
            throw new IllegalArgumentException("A valid payment method is required");
        }

        if (request.getAmount().compareTo(debt.getRemainingAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount cannot exceed remaining debt");
        }

        DebtPayment payment =
                new DebtPayment();

        payment.setDebt(debt);

        payment.setAmount(
                request.getAmount()
        );

        payment.setPaymentMethod(
                request.getPaymentMethod()
        );
        payment.setReceivedBy(currentUserService.require(username));

        debtPaymentRepository.save(payment);

        debt.setPaidAmount(
                debt.getPaidAmount()
                        .add(request.getAmount())
        );

        debt.setRemainingAmount(
                debt.getRemainingAmount()
                        .subtract(request.getAmount())
        );

        if (
                debt.getRemainingAmount()
                        .compareTo(BigDecimal.ZERO) <= 0
        ) {

            debt.setRemainingAmount(
                    BigDecimal.ZERO
            );

            if (debt.getOrder() != null) {
                debt.getOrder().setStatus(
                        OrderStatus.COMPLETED
                );
            }
        }

        return debtRepository.save(debt);
    }

    private Long shopId(String username) {
        return currentUserService.require(username).getShop().getId();
    }
}
