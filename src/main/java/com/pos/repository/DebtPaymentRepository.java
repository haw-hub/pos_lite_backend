package com.pos.repository;

import com.pos.entity.DebtPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebtPaymentRepository
        extends JpaRepository<DebtPayment, Long> {
}