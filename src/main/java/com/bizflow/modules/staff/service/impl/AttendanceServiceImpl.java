package com.bizflow.modules.staff.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.staff.dto.AttendanceDto;
import com.bizflow.modules.staff.entity.Attendance;
import com.bizflow.modules.staff.entity.Staff;
import com.bizflow.modules.staff.repository.AttendanceRepository;
import com.bizflow.modules.staff.repository.StaffRepository;
import com.bizflow.modules.staff.service.AttendanceService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StaffRepository staffRepository;

    @Override
    public ApiResponse<List<AttendanceDto>> getByStaff(Long staffId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                attendanceRepository.findAllByStaffIdAndTenantId(staffId, tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<List<AttendanceDto>> getByDate(LocalDate date) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                attendanceRepository.findAllByTenantIdAndDate(tenantId, date).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<AttendanceDto> mark(AttendanceDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Staff staff = staffRepository.findByIdAndTenantId(dto.getStaffId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.STAFF_NOT_FOUND));

        // Check if already marked
        attendanceRepository.findByStaffIdAndDateAndTenantId(staff.getId(), dto.getDate(), tenantId).ifPresent(a -> {
            throw new com.bizflow.common.exception.BusinessException(MessageConstant.ATTENDANCE_ALREADY_MARKED);
        });

        Attendance attendance = Attendance.builder().tenantId(tenantId).staff(staff).date(dto.getDate())
                .status(dto.getStatus()).checkIn(dto.getCheckIn()).checkOut(dto.getCheckOut()).notes(dto.getNotes())
                .build();

        return ApiResponse.success(MessageConstant.CREATED, toDto(attendanceRepository.save(attendance)));
    }

    @Override
    public ApiResponse<AttendanceDto> update(Long id, AttendanceDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Attendance attendance = attendanceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));
        attendance.setStatus(dto.getStatus());
        attendance.setCheckIn(dto.getCheckIn());
        attendance.setCheckOut(dto.getCheckOut());
        attendance.setNotes(dto.getNotes());
        return ApiResponse.success(MessageConstant.UPDATED, toDto(attendanceRepository.save(attendance)));
    }

    private AttendanceDto toDto(Attendance a) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(a.getId());
        dto.setStaffId(a.getStaff().getId());
        dto.setStaffName(a.getStaff().getName());
        dto.setDate(a.getDate());
        dto.setStatus(a.getStatus());
        dto.setCheckIn(a.getCheckIn());
        dto.setCheckOut(a.getCheckOut());
        dto.setNotes(a.getNotes());
        return dto;
    }
}