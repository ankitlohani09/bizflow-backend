package com.bizflow.modules.expense.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.modules.billing.entity.PaymentMode;
import com.bizflow.modules.billing.repository.PaymentModeRepository;
import com.bizflow.modules.expense.dto.ExpenseDto;
import com.bizflow.modules.expense.entity.Expense;
import com.bizflow.modules.expense.entity.ExpenseCategory;
import com.bizflow.modules.expense.repository.ExpenseCategoryRepository;
import com.bizflow.modules.expense.repository.ExpenseRepository;
import com.bizflow.modules.expense.service.ExpenseService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final PaymentModeRepository paymentModeRepository;

    @Override
    public ApiResponse<List<ExpenseDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                expenseRepository.findAllByTenantIdOrderByExpenseDateDesc(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<List<ExpenseDto>> getByDateRange(LocalDate from, LocalDate to) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(expenseRepository.findAllByTenantIdAndExpenseDateBetween(tenantId, from, to).stream()
                .map(this::toDto).toList());
    }

    @Override
    public ApiResponse<ExpenseDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Expense expense = expenseRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.NOT_FOUND));
        return ApiResponse.success(toDto(expense));
    }

    @Override
    public ApiResponse<ExpenseDto> create(ExpenseDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();

        ExpenseCategory category = dto.getCategoryId() != null
                ? categoryRepository.findByIdAndTenantId(dto.getCategoryId(), tenantId).orElse(null) : null;

        PaymentMode paymentMode = paymentModeRepository.findByIdAndTenantId(dto.getPaymentModeId(), tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.NOT_FOUND));

        Expense expense = Expense.builder().tenantId(tenantId).category(category).title(dto.getTitle())
                .amount(dto.getAmount()).expenseDate(dto.getExpenseDate()).paymentMode(paymentMode)
                .notes(dto.getNotes()).build();

        return ApiResponse.success(MessageConstant.CREATED, toDto(expenseRepository.save(expense)));
    }

    @Override
    public ApiResponse<ExpenseDto> update(Long id, ExpenseDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Expense expense = expenseRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.NOT_FOUND));

        ExpenseCategory category = dto.getCategoryId() != null
                ? categoryRepository.findByIdAndTenantId(dto.getCategoryId(), tenantId).orElse(null) : null;

        PaymentMode paymentMode = paymentModeRepository.findByIdAndTenantId(dto.getPaymentModeId(), tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.NOT_FOUND));

        expense.setCategory(category);
        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setPaymentMode(paymentMode);
        expense.setNotes(dto.getNotes());

        return ApiResponse.success(MessageConstant.UPDATED, toDto(expenseRepository.save(expense)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Expense expense = expenseRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.NOT_FOUND));
        expenseRepository.delete(expense);
        return ApiResponse.success(MessageConstant.DELETED, null);
    }

    private ExpenseDto toDto(Expense e) {
        ExpenseDto dto = new ExpenseDto();
        dto.setId(e.getId());
        dto.setCategoryId(e.getCategory() != null ? e.getCategory().getId() : null);
        dto.setCategoryName(e.getCategory() != null ? e.getCategory().getName() : null);
        dto.setTitle(e.getTitle());
        dto.setAmount(e.getAmount());
        dto.setExpenseDate(e.getExpenseDate());
        dto.setPaymentModeId(e.getPaymentMode().getId());
        dto.setPaymentModeName(e.getPaymentMode().getName());
        dto.setNotes(e.getNotes());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}