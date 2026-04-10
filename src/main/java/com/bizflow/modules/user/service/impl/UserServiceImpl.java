package com.bizflow.modules.user.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.auth.entity.User;
import com.bizflow.modules.auth.repository.UserRepository;
import com.bizflow.modules.user.dto.UserRequest;
import com.bizflow.modules.user.dto.UserResponse;
import com.bizflow.modules.user.service.UserService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<List<UserResponse>> getAllUsers() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        List<UserResponse> users = userRepository.findAll().stream()
                .filter(u -> u.getTenantId().equals(tenantId))
                .map(this::toResponse)
                .toList();
        return ApiResponse.success(users);
    }

    @Override
    public ApiResponse<UserResponse> getUserById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        User user = userRepository.findById(id)
                .filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return ApiResponse.success(toResponse(user));
    }

    @Override
    public ApiResponse<UserResponse> createUser(UserRequest request) {
        Long tenantId = SecurityUtils.getCurrentTenantId();

        if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new BusinessException(MessageConstant.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(request.getIsActive())
                .build();

        user = userRepository.save(user);
        return ApiResponse.success(MessageConstant.USER_CREATED, toResponse(user));
    }

    @Override
    public ApiResponse<UserResponse> updateUser(Long id, UserRequest request) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        User user = userRepository.findById(id)
                .filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setIsActive(request.getIsActive());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);
        return ApiResponse.success(MessageConstant.USER_UPDATED, toResponse(user));
    }

    @Override
    public ApiResponse<Void> deleteUser(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        User user = userRepository.findById(id)
                .filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.delete(user);
        return ApiResponse.success(MessageConstant.USER_DELETED, null);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
