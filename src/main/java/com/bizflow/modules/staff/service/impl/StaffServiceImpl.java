package com.bizflow.modules.staff.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.staff.dto.StaffDto;
import com.bizflow.modules.staff.entity.Staff;
import com.bizflow.modules.staff.repository.StaffRepository;
import com.bizflow.modules.staff.service.StaffService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;

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
    public ApiResponse<StaffDto> create(StaffDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Staff staff = Staff.builder().tenantId(tenantId).name(dto.getName()).phone(dto.getPhone()).email(dto.getEmail())
                .role(dto.getRole()).salary(dto.getSalary()).joinDate(dto.getJoinDate())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true).build();
        return ApiResponse.success(MessageConstant.CREATED, toDto(staffRepository.save(staff)));
    }

    @Override
    public ApiResponse<StaffDto> update(Long id, StaffDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Staff staff = staffRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.STAFF_NOT_FOUND));
        staff.setName(dto.getName());
        staff.setPhone(dto.getPhone());
        staff.setEmail(dto.getEmail());
        staff.setRole(dto.getRole());
        staff.setSalary(dto.getSalary());
        staff.setJoinDate(dto.getJoinDate());
        staff.setIsActive(dto.getIsActive());
        return ApiResponse.success(MessageConstant.UPDATED, toDto(staffRepository.save(staff)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Staff staff = staffRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.STAFF_NOT_FOUND));
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
        dto.setIsActive(s.getIsActive());
        return dto;
    }
}