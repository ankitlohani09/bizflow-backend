package com.bizflow.modules.catalogue.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.catalogue.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    ApiResponse<List<CategoryDto>> getAll();

    ApiResponse<CategoryDto> getById(Long id);

    ApiResponse<CategoryDto> create(CategoryDto dto);

    ApiResponse<CategoryDto> update(Long id, CategoryDto dto);

    ApiResponse<Void> delete(Long id);
}