package com.bizflow.modules.expense.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.expense.dto.ExpenseCategoryDto;
import com.bizflow.modules.expense.entity.ExpenseCategory;
import com.bizflow.modules.expense.repository.ExpenseCategoryRepository;
import com.bizflow.modules.expense.service.ExpenseCategoryService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryServiceImpl implements ExpenseCategoryService {

    private final ExpenseCategoryRepository categoryRepository;

    @Override
    public ApiResponse<List<ExpenseCategoryDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                categoryRepository.findAllByTenantIdAndIsActive(tenantId, true).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<ExpenseCategoryDto> create(ExpenseCategoryDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        ExpenseCategory cat = ExpenseCategory.builder().tenantId(tenantId).name(dto.getName()).isActive(true).build();
        return ApiResponse.success(MessageConstant.CREATED, toDto(categoryRepository.save(cat)));
    }

    @Override
    public ApiResponse<ExpenseCategoryDto> update(Long id, ExpenseCategoryDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        ExpenseCategory cat = categoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));
        cat.setName(dto.getName());
        cat.setIsActive(dto.getIsActive());
        return ApiResponse.success(MessageConstant.UPDATED, toDto(categoryRepository.save(cat)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        ExpenseCategory cat = categoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));
        cat.setIsActive(false);
        categoryRepository.save(cat);
        return ApiResponse.success(MessageConstant.DELETED, null);
    }

    private ExpenseCategoryDto toDto(ExpenseCategory c) {
        ExpenseCategoryDto dto = new ExpenseCategoryDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setIsActive(c.getIsActive());
        return dto;
    }
}