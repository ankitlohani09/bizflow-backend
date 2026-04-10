package com.bizflow.modules.staff.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.staff.dto.StaffAdvanceDto;

import java.util.List;

public interface StaffAdvanceService {
    ApiResponse<List<StaffAdvanceDto>> getByStaff(Long staffId);

    ApiResponse<StaffAdvanceDto> create(StaffAdvanceDto dto);

    ApiResponse<Void> delete(Long id);
}