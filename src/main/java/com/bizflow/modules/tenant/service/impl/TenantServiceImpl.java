package com.bizflow.modules.tenant.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.tenant.dto.TenantRequest;
import com.bizflow.modules.tenant.dto.TenantResponse;
import com.bizflow.modules.tenant.entity.Tenant;
import com.bizflow.modules.tenant.repository.TenantRepository;
import com.bizflow.modules.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    public ApiResponse<List<TenantResponse>> getAll() {
        return ApiResponse.success(tenantRepository.findAll().stream().map(this::toResponse).toList());
    }

    @Override
    public ApiResponse<TenantResponse> getById(Long id) {
        Tenant tenant = tenantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tenant", id));
        return ApiResponse.success(toResponse(tenant));
    }

    @Override
    public ApiResponse<TenantResponse> create(TenantRequest request) {
        if (tenantRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new BusinessException("Tenant code already exists");
        }
        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Tenant email already exists");
        }

        Tenant tenant = Tenant.builder().name(request.getName()).code(request.getCode().toUpperCase())
                .email(request.getEmail()).phone(request.getPhone()).address(request.getAddress())
                .businessType(request.getBusinessType()).isActive(request.getIsActive()).build();

        return ApiResponse.success("Tenant created successfully", toResponse(tenantRepository.save(tenant)));
    }

    @Override
    public ApiResponse<TenantResponse> update(Long id, TenantRequest request) {
        Tenant tenant = tenantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tenant", id));

        tenant.setName(request.getName());
        tenant.setEmail(request.getEmail());
        tenant.setPhone(request.getPhone());
        tenant.setAddress(request.getAddress());
        tenant.setBusinessType(request.getBusinessType());
        tenant.setIsActive(request.getIsActive());

        return ApiResponse.success("Tenant updated successfully", toResponse(tenantRepository.save(tenant)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Tenant tenant = tenantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tenant", id));
        tenant.setIsActive(false);
        tenantRepository.save(tenant);
        return ApiResponse.success("Tenant deleted successfully", null);
    }

    private TenantResponse toResponse(Tenant t) {
        return TenantResponse.builder().id(t.getId()).name(t.getName()).code(t.getCode()).email(t.getEmail())
                .phone(t.getPhone()).address(t.getAddress()).businessType(t.getBusinessType()).isActive(t.getIsActive())
                .createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt()).build();
    }
}