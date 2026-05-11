package com.bizflow.modules.staff.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.staff.dto.StaffDto;
import org.springframework.http.HttpStatus;
import com.bizflow.modules.staff.entity.Staff;
import com.bizflow.modules.staff.repository.StaffRepository;
import com.bizflow.modules.staff.service.StaffService;
import com.bizflow.modules.role.entity.Role;
import com.bizflow.modules.role.repository.RoleRepository;
import com.bizflow.modules.user.dto.UserRequest;
import com.bizflow.modules.user.service.UserService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.bizflow.modules.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<List<StaffDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(staffRepository.findAllByTenantId(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<StaffDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Staff staff = staffRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.STAFF_NOT_FOUND));
        return ApiResponse.success(toDto(staff));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ApiResponse<StaffDto> create(StaffDto dto) {
        if ("OWNER".equalsIgnoreCase(dto.getRole())) {
            throw new BusinessException("Cannot assign OWNER role via staff management", HttpStatus.FORBIDDEN);
        }
        Long tenantId = SecurityUtils.getCurrentTenantId();

        // 1. Create User account for login
        String email = (dto.getEmail() != null && !dto.getEmail().isBlank()) ? dto.getEmail()
                : (dto.getPhone() + "@bizflow.com");

        Role role = roleRepository.findByNameAndTenantId(dto.getRole().toUpperCase(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + dto.getRole()));

        UserRequest userRequest = UserRequest.builder().name(dto.getName()).email(email).phone(dto.getPhone())
                .password(dto.getPin()) // Use PIN as initial password
                .roleIds(List.of(role.getId())).isActive(true).build();

        userService.createUser(userRequest);

        // 2. Create Staff record
        Staff staff = Staff.builder().tenantId(tenantId).name(dto.getName()).phone(dto.getPhone()).email(dto.getEmail())
                .role(dto.getRole()).salary(dto.getSalary()).joinDate(dto.getJoinDate()).pin(dto.getPin())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true).build();

        return ApiResponse.success(MessageConstant.CREATED, toDto(staffRepository.save(staff)));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ApiResponse<StaffDto> update(Long id, StaffDto dto) {
        if ("OWNER".equalsIgnoreCase(dto.getRole())) {
            throw new BusinessException("Cannot assign OWNER role via staff management", HttpStatus.FORBIDDEN);
        }
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Staff staff = staffRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.STAFF_NOT_FOUND));

        // Update User account if exists (finding by old phone number)
        userRepository.findByPhoneAndTenantId(staff.getPhone(), tenantId).ifPresent(user -> {
            user.setName(dto.getName());
            user.setPhone(dto.getPhone());
            if (dto.getPin() != null && !dto.getPin().isBlank()) {
                user.setPassword(passwordEncoder.encode(dto.getPin()));
            }
            user.setIsActive(dto.getIsActive());
            userRepository.save(user);
        });

        staff.setName(dto.getName());
        staff.setPhone(dto.getPhone());
        staff.setEmail(dto.getEmail());
        staff.setRole(dto.getRole());
        staff.setSalary(dto.getSalary());
        staff.setJoinDate(dto.getJoinDate());
        staff.setPin(dto.getPin());
        staff.setIsActive(dto.getIsActive());

        return ApiResponse.success(MessageConstant.UPDATED, toDto(staffRepository.save(staff)));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Staff staff = staffRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.STAFF_NOT_FOUND));

        // Deactivate User account as well
        userRepository.findByPhoneAndTenantId(staff.getPhone(), tenantId).ifPresent(user -> {
            user.setIsActive(false);
            userRepository.save(user);
        });

        staff.setIsActive(false);
        staffRepository.save(staff);
        return ApiResponse.success(MessageConstant.DELETED, null);
    }

    private StaffDto toDto(Staff s) {
        StaffDto dto = new StaffDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setPhone(s.getPhone());
        dto.setEmail(s.getEmail());
        dto.setRole(s.getRole());
        dto.setSalary(s.getSalary());
        dto.setJoinDate(s.getJoinDate());
        dto.setPin(s.getPin());
        dto.setIsActive(s.getIsActive());
        return dto;
    }
}