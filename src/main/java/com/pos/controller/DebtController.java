package com.pos.controller;

import com.pos.dto.CustomerDebtSummaryDTO;
import com.pos.entity.Debt;
import com.pos.repository.DebtPaymentRepository;
import com.pos.repository.DebtRepository;
import com.pos.service.DebtService;
import org.springframework.web.bind.annotation.*;
import com.pos.entity.DebtPayment;
import com.pos.entity.Order;
import com.pos.enums.OrderStatus;
import com.pos.dto.DebtPaymentRequest;
import com.pos.repository.DebtPaymentRepository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/debts")
public class DebtController {

    private final DebtRepository debtRepository;
    private final DebtService debtService;
    private final DebtPaymentRepository debtPaymentRepository;

    public DebtController(
            DebtRepository debtRepository,
            DebtService debtService,
            DebtPaymentRepository debtPaymentRepository
    ) {
        this.debtRepository = debtRepository;
        this.debtService = debtService;
        this.debtPaymentRepository = debtPaymentRepository;
    }


    @GetMapping
    public List<Debt> getAll() {
        return debtRepository.findByOrderCashierUsername(username());
    }

    @GetMapping("/summary")
    public List<CustomerDebtSummaryDTO> summary() {
        return debtRepository.getCustomerDebtSummary(username());
    }

    @GetMapping("/customer/{customerId}")
    public List<Debt> getCustomerDebts(
            @PathVariable Long customerId
    ) {
        return debtRepository.findByCustomerIdAndOrderCashierUsername(customerId, username());
    }

    @PutMapping("/{debtId}/paid")
    public Debt markPaid(
            @PathVariable Long debtId
    ) {
        return debtService.markPaid(debtId, username());
    }

    @PostMapping("/{debtId}/payments")
    public Debt makePayment(
            @PathVariable Long debtId,
            @RequestBody DebtPaymentRequest request
    ) {
        return debtService.makePayment(
                debtId,
                request, username()
        );
    }

    private String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
