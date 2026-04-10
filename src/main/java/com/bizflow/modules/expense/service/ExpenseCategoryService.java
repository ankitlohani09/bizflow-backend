package com.bizflow.modules.expense.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.expense.dto.ExpenseCategoryDto;

import java.util.List;

public interface ExpenseCategoryService {
    ApiResponse<List<ExpenseCategoryDto>> getAll();

    ApiResponse<ExpenseCategoryDto> create(ExpenseCategoryDto dto);

    ApiResponse<ExpenseCategoryDto> update(Long id, ExpenseCategoryDto dto);

    ApiResponse<Void> delete(Long id);
}