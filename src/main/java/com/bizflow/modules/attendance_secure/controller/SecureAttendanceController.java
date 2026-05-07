package com.bizflow.modules.attendance_secure.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.attendance_secure.dto.SecureAttendanceRequest;
import com.bizflow.modules.attendance_secure.service.SecureAttendanceService;
import com.bizflow.modules.staff.dto.AttendanceDto;
import com.bizflow.modules.staff.dto.StaffDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.bizflow.modules.tenant.dto.TenantResponse;
import com.bizflow.modules.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Secure Attendance (QR & Selfie)")
@RestController
@RequestMapping("/public/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow access from any mobile browser
public class SecureAttendanceController {

    private final SecureAttendanceService secureAttendanceService;

    @GetMapping("/staff/{tenantCode}")
    public ResponseEntity<ApiResponse<List<StaffDto>>> getStaff(@PathVariable String tenantCode) {
        return ResponseEntity.ok(secureAttendanceService.getStaffByTenant(tenantCode));
    }

    @Operation(summary = "Get business settings for attendance (Public)")
    @GetMapping("/settings/{tenantCode}")
    public ResponseEntity<ApiResponse<TenantResponse>> getSettings(@PathVariable String tenantCode) {
        return ResponseEntity.ok(secureAttendanceService.getSettingsByTenant(tenantCode));
    }

    @Operation(summary = "Mark secure attendance with PIN and Selfie (Public)")
    @PostMapping("/mark")
    public ResponseEntity<ApiResponse<AttendanceDto>> mark(@RequestBody SecureAttendanceRequest request) {
        return ResponseEntity.ok(secureAttendanceService.markAttendance(request));
    }
}
