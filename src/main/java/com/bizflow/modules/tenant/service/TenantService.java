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

    ApiResponse<com.bizflow.modules.tenant.dto.TenantStatsResponse> getStats(Long id);

    ApiResponse<com.bizflow.modules.tenant.dto.GlobalStatsResponse> getGlobalStats();

    ApiResponse<Void> delete(Long id);
}
