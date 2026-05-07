package com.bizflow.modules.tenant.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.tenant.dto.TenantRequest;
import com.bizflow.modules.tenant.dto.TenantResponse;
import com.bizflow.modules.tenant.entity.Tenant;
import com.bizflow.modules.tenant.repository.TenantRepository;
import com.bizflow.modules.tenant.service.TenantService;
import com.bizflow.modules.auth.entity.PasswordResetToken;
import com.bizflow.modules.auth.repository.PasswordResetTokenRepository;
import com.bizflow.modules.email.service.EmailService;
import com.bizflow.modules.role.entity.Role;
import com.bizflow.modules.role.repository.RoleRepository;
import com.bizflow.modules.user.entity.User;
import com.bizflow.modules.user.entity.UserRole;
import com.bizflow.modules.user.repository.UserRepository;
import com.bizflow.modules.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

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
    @Transactional
    public ApiResponse<TenantResponse> create(TenantRequest request) {
        if (tenantRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new BusinessException("Tenant code already exists");
        }
        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Tenant email already exists");
        }

        // 1. Save Tenant
        Tenant tenant = Tenant.builder().tenantId(1L).name(request.getName()).code(request.getCode().toUpperCase())
                .email(request.getEmail()).phone(request.getPhone()).address(request.getAddress())
                .businessType(request.getBusinessType()).isActive(request.getIsActive())
                .isGpsMandatory(request.getIsGpsMandatory()).isSelfieMandatory(request.getIsSelfieMandatory()).build();

        tenant = tenantRepository.save(tenant);
        Long tenantId = tenant.getId();

        // 2. Create Default Roles for this tenant
        Role ownerRole = roleRepository.save(Role.builder().name("OWNER").tenantId(tenantId)
                .description("Business Owner").createdAt(LocalDateTime.now()).build());
        roleRepository.save(Role.builder().name("ADMIN").tenantId(tenantId).description("Business Admin")
                .createdAt(LocalDateTime.now()).build());
        roleRepository.save(Role.builder().name("MANAGER").tenantId(tenantId).description("Store Manager")
                .createdAt(LocalDateTime.now()).build());
        roleRepository.save(Role.builder().name("USER").tenantId(tenantId).description("Staff/Cashier")
                .createdAt(LocalDateTime.now()).build());

        // 3. Create Owner User
        String tempPassword = UUID.randomUUID().toString();
        User owner = User.builder().tenantId(tenantId).name(request.getOwnerName()).email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword)).isActive(true).build();
        owner = userRepository.save(owner);

        // 4. Assign OWNER role
        userRoleRepository.save(UserRole.builder().userId(owner.getId()).roleId(ownerRole.getId())
                .assignedAt(LocalDateTime.now()).build());

        // 5. Send Onboarding Email (Password setup)
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder().token(token).user(owner)
                .expiryDate(LocalDateTime.now().plusDays(7)).build();
        tokenRepository.save(resetToken);

        try {
            log.info("Sending onboarding emails to: {}. Token: {}", owner.getEmail(), token);
            emailService.sendOnboardingEmail(owner.getEmail(), tenant.getName());
            emailService.sendPasswordResetEmail(owner.getEmail(), token);
        } catch (Exception e) {
            log.error("Failed to trigger onboarding emails for: {}", owner.getEmail(), e);
        }

        log.info("Onboarding completed for tenant: {}", tenant.getName());
        return ApiResponse.success("Tenant and Owner account created successfully", toResponse(tenant));
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
        tenant.setIsGpsMandatory(request.getIsGpsMandatory());
        tenant.setIsSelfieMandatory(request.getIsSelfieMandatory());

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
                .isGpsMandatory(t.getIsGpsMandatory()).isSelfieMandatory(t.getIsSelfieMandatory())
                .createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt()).build();
    }
}