package com.bizflow.modules.supplier.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.supplier.dto.SupplierDto;

import java.util.List;

public interface SupplierService {
    ApiResponse<List<SupplierDto>> getAll();

    ApiResponse<SupplierDto> getById(Long id);

    ApiResponse<SupplierDto> create(SupplierDto dto);

    ApiResponse<SupplierDto> update(Long id, SupplierDto dto);

    ApiResponse<Void> delete(Long id);
}