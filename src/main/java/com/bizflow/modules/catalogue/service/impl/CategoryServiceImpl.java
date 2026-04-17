package com.bizflow.modules.catalogue.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.catalogue.dto.CategoryDto;
import com.bizflow.modules.catalogue.entity.Category;
import com.bizflow.modules.catalogue.repository.CategoryRepository;
import com.bizflow.modules.catalogue.service.CategoryService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public ApiResponse<List<CategoryDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(categoryRepository.findAllByTenantId(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<CategoryDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Category cat = categoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.CATEGORY_NOT_FOUND));
        return ApiResponse.success(toDto(cat));
    }

    @Override
    public ApiResponse<CategoryDto> create(CategoryDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        String normalizedName = normalizeName(dto.getName());

        if (categoryRepository.existsByTenantIdAndNameIgnoreCase(tenantId, normalizedName)) {
            throw new BusinessException(MessageConstant.CATEGORY_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        Category parent = null;
        if (dto.getParentId() != null) {
            parent = categoryRepository.findByIdAndTenantId(dto.getParentId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.CATEGORY_NOT_FOUND));
        }

        Category cat = Category.builder().tenantId(tenantId).name(normalizedName).parent(parent).build();
        return ApiResponse.success(MessageConstant.CATEGORY_CREATED, toDto(categoryRepository.save(cat)));
    }

    @Override
    public ApiResponse<CategoryDto> update(Long id, CategoryDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Category cat = categoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.CATEGORY_NOT_FOUND));

        String normalizedName = normalizeName(dto.getName());
        if (categoryRepository.existsByTenantIdAndNameIgnoreCaseAndIdNot(tenantId, normalizedName, id)) {
            throw new BusinessException(MessageConstant.CATEGORY_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        Category parent = null;
        if (dto.getParentId() != null) {
            if (id.equals(dto.getParentId())) {
                throw new BusinessException(MessageConstant.CATEGORY_PARENT_INVALID);
            }
            parent = categoryRepository.findByIdAndTenantId(dto.getParentId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.CATEGORY_NOT_FOUND));
        }

        cat.setName(normalizedName);
        cat.setParent(parent);
        return ApiResponse.success(MessageConstant.CATEGORY_UPDATED, toDto(categoryRepository.save(cat)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Category cat = categoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsByTenantIdAndParentId(tenantId, id)) {
            throw new BusinessException(MessageConstant.CATEGORY_HAS_CHILDREN);
        }

        categoryRepository.delete(cat);
        return ApiResponse.success(MessageConstant.CATEGORY_DELETED, null);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(MessageConstant.CATEGORY_NAME_REQUIRED);
        }
        return name.trim();
    }

    private CategoryDto toDto(Category c) {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        if (c.getParent() != null) {
            dto.setParentId(c.getParent().getId());
            dto.setParentName(c.getParent().getName());
        }
        return dto;
    }
}
