package com.bizflow.modules.expense.repository;

import com.bizflow.modules.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByTenantIdOrderByExpenseDateDesc(Long tenantId);

    Optional<Expense> findByIdAndTenantId(Long id, Long tenantId);

    List<Expense> findAllByTenantIdAndExpenseDateBetween(Long tenantId, LocalDate from, LocalDate to);
}