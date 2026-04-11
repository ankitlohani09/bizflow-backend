package com.bizflow.modules.user.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.auth.entity.Role;
import com.bizflow.modules.auth.entity.User;
import com.bizflow.modules.auth.entity.UserRole;
import com.bizflow.modules.auth.repository.RoleRepository;
import com.bizflow.modules.auth.repository.UserRepository;
import com.bizflow.modules.auth.repository.UserRoleRepository;
import com.bizflow.modules.user.dto.UserRequest;
import com.bizflow.modules.user.dto.UserResponse;
import com.bizflow.modules.user.service.UserService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<List<UserResponse>> getAllUsers() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        List<UserResponse> users = userRepository.findAllByTenantId(tenantId).stream().map(this::toResponse).toList();
        return ApiResponse.success(users);
    }

    @Override
    public ApiResponse<UserResponse> getUserById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        User user = userRepository.findById(id).filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return ApiResponse.success(toResponse(user));
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> createUser(UserRequest request) {
        Long tenantId = SecurityUtils.getCurrentTenantId();

        if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new BusinessException(MessageConstant.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder().tenantId(tenantId).name(request.getName()).email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())).phone(request.getPhone())
                .isActive(request.getIsActive()).build();

        user = userRepository.save(user);

        assignRoles(user.getId(), request.getRoleIds());

        return ApiResponse.success(MessageConstant.USER_CREATED, toResponse(user));
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> updateUser(Long id, UserRequest request) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        User user = userRepository.findById(id).filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setIsActive(request.getIsActive());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);

        // Replace existing roles with new ones
        userRoleRepository.deleteByUserId(user.getId());
        assignRoles(user.getId(), request.getRoleIds());

        return ApiResponse.success(MessageConstant.USER_UPDATED, toResponse(user));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        User user = userRepository.findById(id).filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        userRoleRepository.deleteByUserId(id);
        userRepository.delete(user);

        return ApiResponse.success(MessageConstant.USER_DELETED, null);
    }

    // --- Helpers ---
    private void assignRoles(Long userId, List<Long> roleIds) {
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new ResourceNotFoundException("One or more roles not found");
        }

        List<UserRole> userRoles = roleIds.stream()
                .map(roleId -> UserRole.builder().userId(userId).roleId(roleId).assignedAt(LocalDateTime.now()).build())
                .toList();

        userRoleRepository.saveAll(userRoles);
    }

    private List<String> getRoleNames(Long userId) {
        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream().map(UserRole::getRoleId).toList();

        return roleRepository.findAllById(roleIds).stream().map(Role::getName).toList();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder().id(user.getId()).tenantId(user.getTenantId()).name(user.getName())
                .email(user.getEmail()).phone(user.getPhone()).roles(getRoleNames(user.getId()))
                .isActive(user.getIsActive()).createdAt(user.getCreatedAt()).updatedAt(user.getUpdatedAt()).build();
    }
}
