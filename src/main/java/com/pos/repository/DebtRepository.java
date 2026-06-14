package com.pos.repository;

import com.pos.dto.CustomerDebtSummaryDTO;
import com.pos.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DebtRepository
        extends JpaRepository<Debt, Long> {

    List<Debt> findByCustomerIdAndOrderCashierUsername(Long customerId, String username);
    List<Debt> findByOrderCashierUsername(String username);
    java.util.Optional<Debt> findByIdAndOrderCashierUsername(Long id, String username);

    @Query("""
        SELECT new com.pos.dto.CustomerDebtSummaryDTO(
            c.id,
            c.name,
            c.phone,
            SUM(d.remainingAmount),
            COUNT(d.id)
        )
        FROM Debt d
        JOIN d.customer c
        WHERE d.remainingAmount > 0
        AND d.order.cashier.username = :username
        GROUP BY c.id, c.name, c.phone
    """)
    List<CustomerDebtSummaryDTO> getCustomerDebtSummary(String username);
}
