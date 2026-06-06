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

    public DebtService(
            DebtRepository debtRepository,
            DebtPaymentRepository debtPaymentRepository
    ) {
        this.debtRepository = debtRepository;
        this.debtPaymentRepository = debtPaymentRepository;
    }
    @Transactional
    public Debt markPaid(Long debtId) {

        Debt debt = debtRepository
                .findById(debtId)
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
            DebtPaymentRequest request
    ) {

        Debt debt = debtRepository
                .findById(debtId)
                .orElseThrow();

        DebtPayment payment =
                new DebtPayment();

        payment.setDebt(debt);

        payment.setAmount(
                request.getAmount()
        );

        payment.setPaymentMethod(
                request.getPaymentMethod()
        );

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
}