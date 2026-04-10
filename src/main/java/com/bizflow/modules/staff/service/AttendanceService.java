package com.bizflow.modules.staff.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.staff.dto.AttendanceDto;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    ApiResponse<List<AttendanceDto>> getByStaff(Long staffId);

    ApiResponse<List<AttendanceDto>> getByDate(LocalDate date);

    ApiResponse<AttendanceDto> mark(AttendanceDto dto);

    ApiResponse<AttendanceDto> update(Long id, AttendanceDto dto);
}