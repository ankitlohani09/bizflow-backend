package com.bizflow.modules.tenant.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.tenant.dto.TenantRequest;
import com.bizflow.modules.tenant.dto.TenantResponse;

import java.util.List;

public interface TenantService {
    ApiResponse<List<TenantResponse>> getAll();

    ApiResponse<TenantResponse> getById(Long id);

    ApiResponse<TenantResponse> create(TenantRequest request);

    ApiResponse<TenantResponse> update(Long id, TenantRequest request);

    ApiResponse<Void> delete(Long id);
}
