package com.bizflow.modules.staff.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.staff.dto.StaffDto;
import com.bizflow.modules.staff.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Staff")
@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @Operation(summary = "Get all staff")
    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffDto>>> getAll() {
        return ResponseEntity.ok(staffService.getAll());
    }

    @Operation(summary = "Get staff by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getById(id));
    }

    @Operation(summary = "Create staff")
    @PostMapping
    public ResponseEntity<ApiResponse<StaffDto>> create(@RequestBody StaffDto dto) {
        return ResponseEntity.ok(staffService.create(dto));
    }

    @Operation(summary = "Update staff")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffDto>> update(@PathVariable Long id, @RequestBody StaffDto dto) {
        return ResponseEntity.ok(staffService.update(id, dto));
    }

    @Operation(summary = "Delete staff (soft)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.delete(id));
    }
}