package com.bizflow.modules.auth.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.auth.dto.RoleRequest;
import com.bizflow.modules.auth.dto.RoleResponse;
import com.bizflow.modules.auth.entity.Role;
import com.bizflow.modules.auth.repository.RoleRepository;
import com.bizflow.modules.auth.repository.UserRoleRepository;
import com.bizflow.modules.auth.service.RoleService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public ApiResponse<List<RoleResponse>> getAllRoles() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        List<RoleResponse> roles = roleRepository.findAllByTenantId(tenantId).stream().map(this::toResponse).toList();
        return ApiResponse.success(roles);
    }

    @Override
    public ApiResponse<RoleResponse> getRoleById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Role role = roleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.ROLE_NOT_FOUND));
        return ApiResponse.success(toResponse(role));
    }

    @Override
    @Transactional
    public ApiResponse<RoleResponse> createRole(RoleRequest request) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        String normalizedName = normalizeName(request.getName());

        if (roleRepository.existsByNameAndTenantId(normalizedName, tenantId)) {
            throw new BusinessException(MessageConstant.ROLE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        Role role = Role.builder().tenantId(tenantId).name(normalizedName).description(request.getDescription())
                .createdAt(LocalDateTime.now()).build();

        return ApiResponse.success(MessageConstant.ROLE_CREATED, toResponse(roleRepository.save(role)));
    }

    @Override
    @Transactional
    public ApiResponse<RoleResponse> updateRole(Long id, RoleRequest request) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Role role = roleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.ROLE_NOT_FOUND));

        String normalizedName = normalizeName(request.getName());
        if (!role.getName().equals(normalizedName)
                && roleRepository.existsByNameAndTenantId(normalizedName, tenantId)) {
            throw new BusinessException(MessageConstant.ROLE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        role.setName(normalizedName);
        role.setDescription(request.getDescription());

        return ApiResponse.success(MessageConstant.ROLE_UPDATED, toResponse(roleRepository.save(role)));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteRole(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Role role = roleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.ROLE_NOT_FOUND));

        if (userRoleRepository.existsByRoleId(id)) {
            throw new BusinessException(MessageConstant.ROLE_ASSIGNED_TO_USER);
        }

        roleRepository.delete(role);
        return ApiResponse.success(MessageConstant.ROLE_DELETED, null);
    }

    private String normalizeName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new BusinessException(MessageConstant.ROLE_NAME_REQUIRED);
        }
        return roleName.trim().toUpperCase(Locale.ROOT);
    }

    private RoleResponse toResponse(Role role) {
        return RoleResponse.builder().id(role.getId()).tenantId(role.getTenantId()).name(role.getName())
                .description(role.getDescription()).createdAt(role.getCreatedAt()).build();
    }
}
