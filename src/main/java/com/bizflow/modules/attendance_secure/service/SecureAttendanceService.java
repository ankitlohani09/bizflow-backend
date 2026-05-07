package com.bizflow.modules.attendance_secure.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.attendance_secure.dto.SecureAttendanceRequest;
import com.bizflow.modules.staff.dto.AttendanceDto;
import com.bizflow.modules.staff.dto.StaffDto;
import com.bizflow.modules.tenant.dto.TenantResponse;
import java.util.List;

public interface SecureAttendanceService {
    ApiResponse<List<StaffDto>> getStaffByTenant(String tenantCode);

    ApiResponse<TenantResponse> getSettingsByTenant(String tenantCode);

    ApiResponse<AttendanceDto> markAttendance(SecureAttendanceRequest request);
}
