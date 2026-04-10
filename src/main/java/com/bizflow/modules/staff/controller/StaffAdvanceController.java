package com.bizflow.modules.staff.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.staff.dto.StaffAdvanceDto;
import com.bizflow.modules.staff.service.StaffAdvanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Staff Advances")
@RestController
@RequestMapping("/staff-advances")
@RequiredArgsConstructor
public class StaffAdvanceController {

    private final StaffAdvanceService staffAdvanceService;

    @Operation(summary = "Get advances by staff")
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<List<StaffAdvanceDto>>> getByStaff(@PathVariable Long staffId) {
        return ResponseEntity.ok(staffAdvanceService.getByStaff(staffId));
    }

    @Operation(summary = "Create advance")
    @PostMapping
    public ResponseEntity<ApiResponse<StaffAdvanceDto>> create(@RequestBody StaffAdvanceDto dto) {
        return ResponseEntity.ok(staffAdvanceService.create(dto));
    }

    @Operation(summary = "Delete advance")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(staffAdvanceService.delete(id));
    }
}