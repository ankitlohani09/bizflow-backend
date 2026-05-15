package com.bizflow.modules.user.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.common.utility.FileStorageService;
import com.bizflow.modules.auth.entity.PasswordResetToken;
import com.bizflow.modules.role.entity.Role;
import com.bizflow.modules.tenant.entity.Tenant;
import com.bizflow.modules.user.entity.User;
import com.bizflow.modules.user.entity.UserRole;
import com.bizflow.modules.role.repository.RoleRepository;
import com.bizflow.modules.user.repository.UserRepository;
import com.bizflow.modules.user.repository.UserRoleRepository;
import com.bizflow.modules.user.dto.UserRequest;
import com.bizflow.modules.user.dto.UserResponse;
import com.bizflow.modules.auth.repository.PasswordResetTokenRepository;
import com.bizflow.modules.email.service.EmailService;
import com.bizflow.modules.tenant.repository.TenantRepository;
import com.bizflow.modules.user.service.UserService;
import com.bizflow.modules.logs.service.ActivityLogService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    private final ActivityLogService activityLogService;

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

        // If password is not provided, generate a random one as a placeholder
        String password = (request.getPassword() != null && !request.getPassword().isBlank()) ? request.getPassword()
                : UUID.randomUUID().toString();

        User user = User.builder().tenantId(tenantId).name(request.getName()).email(request.getEmail())
                .password(passwordEncoder.encode(password)).phone(request.getPhone()).isActive(request.getIsActive())
                .build();

        user = userRepository.save(user);

        assignRoles(tenantId, user.getId(), request.getRoleIds());

        // Generate onboarding/reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder().token(token).user(user)
                .expiryDate(LocalDateTime.now().plusDays(7)) // Onboarding
                                                             // link valid
                                                             // for 7 days
                .build();
        tokenRepository.save(resetToken);

        // Fetch Tenant Name for the email
        String companyName = "BizFlow";
        try {
            // We'll need TenantRepository to get the actual company name
            // For now, using "BizFlow" or fetching it if repository is available
            if (tenantId != null) {
                companyName = tenantRepository.findById(tenantId).map(Tenant::getName).orElse("BizFlow");
            }
        } catch (Exception e) {
            // Fallback to BizFlow
        }

        // Send Onboarding Email with "Set Password" link
        emailService.sendOnboardingEmail(user.getEmail(), companyName, token);

        return ApiResponse.success("User created and onboarding email sent", toResponse(user));
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

        // Security check for password update
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (id.equals(currentUserId)) {
            // User updating themselves -> Require current password if updating password
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                    throw new BusinessException("Current password is required to change password",
                            HttpStatus.BAD_REQUEST);
                }
                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                    throw new BusinessException("Incorrect current password", HttpStatus.BAD_REQUEST);
                }
            }
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            activityLogService.log("UPDATE_PASSWORD", "USER", user.getId(), "User updated password", null);
        }

        user = userRepository.save(user);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            userRoleRepository.deleteByUserId(user.getId());
            assignRoles(tenantId, user.getId(), request.getRoleIds());
        }

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

    @Override
    @Transactional
    public ApiResponse<UserResponse> updateProfilePicture(Long id, String imageUrl) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        User user = userRepository.findById(id).filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Delete old picture if exists
        String oldImageUrl = user.getProfilePictureUrl();
        if (oldImageUrl != null && !oldImageUrl.isBlank()) {
            fileStorageService.deleteFile(oldImageUrl);
        }

        user.setProfilePictureUrl(imageUrl);
        user = userRepository.save(user);

        return ApiResponse.success("Profile picture updated", toResponse(user));
    }

    // --- Helpers ---
    private void assignRoles(Long tenantId, Long userId, List<Long> roleIds) {
        List<Role> roles = roleRepository.findAllById(roleIds).stream()
                .filter(role -> tenantId.equals(role.getTenantId())).toList();

        if (roles.size() != roleIds.size()) {
            throw new ResourceNotFoundException("One or more roles not found");
        }

        List<UserRole> userRoles = roles.stream().map(
                role -> UserRole.builder().userId(userId).roleId(role.getId()).assignedAt(LocalDateTime.now()).build())
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
                .profilePictureUrl(user.getProfilePictureUrl()).isActive(user.getIsActive())
                .createdAt(user.getCreatedAt()).updatedAt(user.getUpdatedAt()).build();
    }
}
