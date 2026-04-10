package com.bizflow.modules.expense.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.expense.dto.ExpenseDto;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ApiResponse<List<ExpenseDto>> getAll();

    ApiResponse<List<ExpenseDto>> getByDateRange(LocalDate from, LocalDate to);

    ApiResponse<ExpenseDto> getById(Long id);

    ApiResponse<ExpenseDto> create(ExpenseDto dto);

    ApiResponse<ExpenseDto> update(Long id, ExpenseDto dto);

    ApiResponse<Void> delete(Long id);
}