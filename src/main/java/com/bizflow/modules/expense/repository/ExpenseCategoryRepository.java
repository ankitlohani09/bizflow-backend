package com.bizflow.modules.expense.repository;

import com.bizflow.modules.expense.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    List<ExpenseCategory> findAllByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    Optional<ExpenseCategory> findByIdAndTenantId(Long id, Long tenantId);
}