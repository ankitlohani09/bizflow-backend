package com.bizflow.modules.auth.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.auth.dto.RoleRequest;
import com.bizflow.modules.auth.dto.RoleResponse;

import java.util.List;

public interface RoleService {
    ApiResponse<List<RoleResponse>> getAllRoles();

    ApiResponse<RoleResponse> getRoleById(Long id);

    ApiResponse<RoleResponse> createRole(RoleRequest request);

    ApiResponse<RoleResponse> updateRole(Long id, RoleRequest request);

    ApiResponse<Void> deleteRole(Long id);
}
