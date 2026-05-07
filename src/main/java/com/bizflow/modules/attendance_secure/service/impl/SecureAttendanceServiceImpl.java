package com.bizflow.modules.attendance_secure.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.common.utility.FileStorageService;
import com.bizflow.modules.attendance_secure.dto.SecureAttendanceRequest;
import com.bizflow.modules.attendance_secure.service.SecureAttendanceService;
import com.bizflow.modules.staff.dto.AttendanceDto;
import com.bizflow.modules.staff.dto.StaffDto;
import com.bizflow.modules.tenant.dto.TenantResponse;
import com.bizflow.modules.staff.entity.Attendance;
import com.bizflow.modules.staff.entity.Staff;
import com.bizflow.common.enums.AttendanceStatus;
import com.bizflow.modules.staff.repository.AttendanceRepository;
import com.bizflow.modules.staff.repository.StaffRepository;
import com.bizflow.modules.tenant.entity.Tenant;
import com.bizflow.modules.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SecureAttendanceServiceImpl implements SecureAttendanceService {

    private final StaffRepository staffRepository;
    private final AttendanceRepository attendanceRepository;
    private final TenantRepository tenantRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StaffDto>> getStaffByTenant(String tenantCode) {
        Tenant tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        List<Staff> staffList = staffRepository.findAllByTenantId(tenant.getId());
        return ApiResponse.success(staffList.stream().map(this::toStaffDto).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<TenantResponse> getSettingsByTenant(String tenantCode) {
        Tenant tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        return ApiResponse.success(toTenantResponse(tenant));
    }

    @Override
    public ApiResponse<AttendanceDto> markAttendance(SecureAttendanceRequest request) {
        Tenant tenant = tenantRepository.findByCode(request.getTenantCode())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        Staff staff = staffRepository.findByIdAndTenantId(request.getStaffId(), tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.STAFF_NOT_FOUND));

        // PIN Verification
        if (staff.getPin() == null || !staff.getPin().equals(request.getPin())) {
            throw new BusinessException("Invalid Personal PIN");
        }

        // 1. Mandatory Selfie Check
        if (tenant.getIsSelfieMandatory()
                && (request.getPhotoBase64() == null || request.getPhotoBase64().equals("DEV_BYPASS"))) {
            throw new BusinessException("Selfie verification is required by owner");
        }

        // 2. Mandatory GPS Check
        if (tenant.getIsGpsMandatory() && (request.getLocation() == null || request.getLocation().equals("0.0,0.0"))) {
            throw new BusinessException("GPS location is required by owner");
        }

        // Check if already marked for today
        attendanceRepository.findByStaffIdAndDateAndTenantId(staff.getId(), LocalDate.now(), tenant.getId())
                .ifPresent(a -> {
                    throw new BusinessException(MessageConstant.ATTENDANCE_ALREADY_MARKED);
                });

        // Save Selfie
        String photoUrl = null;
        if (request.getPhotoBase64() != null) {
            photoUrl = fileStorageService.uploadBase64(request.getPhotoBase64(), "attendance",
                    "selfie_" + staff.getId() + ".jpg");
        }

        Attendance attendance = Attendance.builder().tenantId(tenant.getId()).staff(staff).date(LocalDate.now())
                .status(AttendanceStatus.PRESENT).checkIn(LocalTime.now()).photoUrl(photoUrl)
                .location(request.getLocation()).notes(request.getNotes()).build();

        return ApiResponse.success("Attendance marked successfully with Selfie",
                toAttendanceDto(attendanceRepository.save(attendance)));
    }

    private StaffDto toStaffDto(Staff s) {
        StaffDto dto = new StaffDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setRole(s.getRole());
        dto.setPin(s.getPin());
        dto.setIsActive(s.getIsActive());
        return dto;
    }

    private TenantResponse toTenantResponse(Tenant t) {
        return TenantResponse.builder().id(t.getId()).name(t.getName()).code(t.getCode())
                .isGpsMandatory(t.getIsGpsMandatory()).isSelfieMandatory(t.getIsSelfieMandatory()).build();
    }

    private AttendanceDto toAttendanceDto(Attendance a) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(a.getId());
        dto.setStaffId(a.getStaff().getId());
        dto.setStaffName(a.getStaff().getName());
        dto.setDate(a.getDate());
        dto.setStatus(a.getStatus());
        dto.setCheckIn(a.getCheckIn());
        dto.setCheckOut(a.getCheckOut());
        dto.setNotes(a.getNotes());
        dto.setPhotoUrl(a.getPhotoUrl());
        dto.setLocation(a.getLocation());
        return dto;
    }
}
