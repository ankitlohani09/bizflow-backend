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
import com.bizflow.modules.logs.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bizflow.modules.billing.repository.InvoiceRepository;
import com.bizflow.modules.tenant.dto.GlobalStatsResponse;
import com.bizflow.modules.tenant.dto.GrowthDataPoint;
import com.bizflow.modules.tenant.dto.TenantStatsResponse;

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
    private final InvoiceRepository invoiceRepository;
    private final ActivityLogService activityLogService;

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
                .subscriptionPlan(request.getSubscriptionPlan() != null ? request.getSubscriptionPlan() : "TRIAL")
                .expiryDate(
                        request.getExpiryDate() != null ? request.getExpiryDate() : LocalDateTime.now().plusMonths(1))
                .maxUsers(request.getMaxUsers() != null ? request.getMaxUsers() : 5)
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
        roleRepository.save(Role.builder().name("CASHIER").tenantId(tenantId).description("Staff/Cashier")
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
        tenant.setSubscriptionPlan(request.getSubscriptionPlan());
        tenant.setExpiryDate(request.getExpiryDate());
        tenant.setMaxUsers(request.getMaxUsers());
        tenant.setIsGpsMandatory(request.getIsGpsMandatory());
        tenant.setIsSelfieMandatory(request.getIsSelfieMandatory());
        tenant.setIsKitchenEnabled(request.getIsKitchenEnabled());

        activityLogService.log("UPDATE_TENANT", "TENANT", tenant.getId(), "Tenant settings updated", null);

        return ApiResponse.success("Tenant updated successfully", toResponse(tenantRepository.save(tenant)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Tenant tenant = tenantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tenant", id));
        tenant.setIsActive(false);
        tenantRepository.save(tenant);
        return ApiResponse.success("Tenant deleted successfully", null);
    }

    @Override
    public ApiResponse<TenantStatsResponse> getStats(Long id) {
        Tenant tenant = tenantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tenant", id));

        long userCount = userRepository.countByTenantId(id);

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long invoiceCount = invoiceRepository.countByTenantIdAndCreatedAtBetween(id, startOfMonth, LocalDateTime.now());

        Double usagePercentage = (userCount * 100.0) / tenant.getMaxUsers();

        TenantStatsResponse stats = TenantStatsResponse.builder().activeUsers(userCount)
                .maxUsersLimit(tenant.getMaxUsers().longValue()).monthlyInvoices(invoiceCount)
                .usagePercentage(usagePercentage).build();

        return ApiResponse.success(stats);
    }

    @Override
    public ApiResponse<GlobalStatsResponse> getGlobalStats() {
        List<Tenant> tenants = tenantRepository.findAll();
        long totalUsers = userRepository.count();

        long active = tenants.stream().filter(Tenant::getIsActive).count();
        long trial = tenants.stream().filter(t -> "TRIAL".equalsIgnoreCase(t.getSubscriptionPlan())).count();
        long pro = tenants.stream().filter(t -> "PRO".equalsIgnoreCase(t.getSubscriptionPlan())).count();
        long enterprise = tenants.stream().filter(t -> "ENTERPRISE".equalsIgnoreCase(t.getSubscriptionPlan())).count();

        // Calculate monthly growth (last 6 months)
        java.util.List<GrowthDataPoint> growth = new java.util.ArrayList<>();
        java.util.List<GrowthDataPoint> revGrowth = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM");

        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusNanos(1);

            long count = tenants.stream()
                    .filter(t -> t.getCreatedAt().isAfter(monthStart) && t.getCreatedAt().isBefore(monthEnd)).count();

            growth.add(GrowthDataPoint.builder().month(monthStart.format(formatter)).count(count).build());

            Double rev = invoiceRepository.sumGrandTotalBetween(monthStart, monthEnd);
            revGrowth.add(GrowthDataPoint.builder().month(monthStart.format(formatter))
                    .count((long) (rev != null ? rev : 0.0)).build());
        }

        Double totalRev = invoiceRepository.sumGrandTotalAllTime();

        GlobalStatsResponse stats = GlobalStatsResponse.builder().totalTenants((long) tenants.size())
                .activeTenants(active).trialTenants(trial).proTenants(pro).enterpriseTenants(enterprise)
                .totalUsers(totalUsers).systemHealth("OPTIMAL").totalRevenue(totalRev != null ? totalRev : 0.0)
                .tenantGrowth(growth).revenueGrowth(revGrowth).build();

        return ApiResponse.success(stats);
    }

    private TenantResponse toResponse(Tenant t) {
        return TenantResponse.builder().id(t.getId()).name(t.getName()).code(t.getCode()).email(t.getEmail())
                .phone(t.getPhone()).address(t.getAddress()).businessType(t.getBusinessType()).isActive(t.getIsActive())
                .subscriptionPlan(t.getSubscriptionPlan()).expiryDate(t.getExpiryDate()).maxUsers(t.getMaxUsers())
                .isGpsMandatory(t.getIsGpsMandatory()).isSelfieMandatory(t.getIsSelfieMandatory())
                .isKitchenEnabled(t.getIsKitchenEnabled()).createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt())
                .build();
    }
}