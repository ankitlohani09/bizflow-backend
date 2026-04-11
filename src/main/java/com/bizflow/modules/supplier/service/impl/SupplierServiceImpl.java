package com.bizflow.modules.supplier.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.supplier.dto.SupplierDto;
import com.bizflow.modules.supplier.entity.Supplier;
import com.bizflow.modules.supplier.repository.SupplierRepository;
import com.bizflow.modules.supplier.service.SupplierService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public ApiResponse<List<SupplierDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(supplierRepository.findAllByTenantId(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<SupplierDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.SUPPLIER_NOT_FOUND));
        return ApiResponse.success(toDto(supplier));
    }

    @Override
    public ApiResponse<SupplierDto> create(SupplierDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Supplier supplier = Supplier.builder().tenantId(tenantId).name(dto.getName()).contactName(dto.getContactName())
                .phone(dto.getPhone()).email(dto.getEmail()).address(dto.getAddress()).gstin(dto.getGstin())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true).build();
        return ApiResponse.success(MessageConstant.SUPPLIER_CREATED, toDto(supplierRepository.save(supplier)));
    }

    @Override
    public ApiResponse<SupplierDto> update(Long id, SupplierDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.SUPPLIER_NOT_FOUND));
        supplier.setName(dto.getName());
        supplier.setContactName(dto.getContactName());
        supplier.setPhone(dto.getPhone());
        supplier.setEmail(dto.getEmail());
        supplier.setAddress(dto.getAddress());
        supplier.setGstin(dto.getGstin());
        supplier.setIsActive(dto.getIsActive());
        return ApiResponse.success(MessageConstant.SUPPLIER_UPDATED, toDto(supplierRepository.save(supplier)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.SUPPLIER_NOT_FOUND));
        supplierRepository.delete(supplier);
        return ApiResponse.success(MessageConstant.SUPPLIER_DELETED, null);
    }

    private SupplierDto toDto(Supplier s) {
        SupplierDto dto = new SupplierDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setContactName(s.getContactName());
        dto.setPhone(s.getPhone());
        dto.setEmail(s.getEmail());
        dto.setAddress(s.getAddress());
        dto.setGstin(s.getGstin());
        dto.setIsActive(s.getIsActive());
        return dto;
    }
}