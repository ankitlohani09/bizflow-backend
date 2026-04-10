package com.bizflow.modules.staff.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.staff.dto.AttendanceDto;
import com.bizflow.modules.staff.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Attendance")
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "Get attendance by staff")
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getByStaff(@PathVariable Long staffId) {
        return ResponseEntity.ok(attendanceService.getByStaff(staffId));
    }

    @Operation(summary = "Get attendance by date")
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getByDate(date));
    }

    @Operation(summary = "Mark attendance")
    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceDto>> mark(@RequestBody AttendanceDto dto) {
        return ResponseEntity.ok(attendanceService.mark(dto));
    }

    @Operation(summary = "Update attendance")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceDto>> update(@PathVariable Long id, @RequestBody AttendanceDto dto) {
        return ResponseEntity.ok(attendanceService.update(id, dto));
    }
}