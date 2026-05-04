package com.bizflow.modules.expense.repository;

import com.bizflow.modules.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category JOIN FETCH e.paymentMode WHERE e.tenantId = ?1 ORDER BY e.expenseDate DESC")
    List<Expense> findAllByTenantIdOrderByExpenseDateDesc(Long tenantId);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category JOIN FETCH e.paymentMode WHERE e.id = ?1 AND e.tenantId = ?2")
    Optional<Expense> findByIdAndTenantId(Long id, Long tenantId);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category JOIN FETCH e.paymentMode WHERE e.tenantId = ?1 AND e.expenseDate BETWEEN ?2 AND ?3")
    List<Expense> findAllByTenantIdAndExpenseDateBetween(Long tenantId, LocalDate from, LocalDate to);
}