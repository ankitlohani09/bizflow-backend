package com.bizflow.modules.returns.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.returns.dto.ReturnDto;
import com.bizflow.modules.returns.service.ReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Tag(name = "Returns")
@RestController
@RequestMapping("/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @Operation(summary = "Get all returns")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReturnDto>>> getAll() {
        return ResponseEntity.ok(returnService.getAll());
    }

    @Operation(summary = "Get return by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(returnService.getById(id));
    }

    @Operation(summary = "Create return")
    @PostMapping
    public ResponseEntity<ApiResponse<ReturnDto>> create(@RequestBody ReturnDto dto) {
        return ResponseEntity.ok(returnService.create(dto));
    }

    @Operation(summary = "Approve return")
    @PostMapping("/{id}/approve")
    @PreAuthorize("@rbac.hasPermission('RETURN_WRITE')")
    public ResponseEntity<ApiResponse<ReturnDto>> approve(@PathVariable Long id, @RequestParam(required = false) java.math.BigDecimal overrideRefund) {
        return ResponseEntity.ok(returnService.approve(id, overrideRefund));
    }

    @Operation(summary = "Reject return")
    @PostMapping("/{id}/reject")
    @PreAuthorize("@rbac.hasPermission('RETURN_WRITE')")
    public ResponseEntity<ApiResponse<ReturnDto>> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(returnService.reject(id, reason));
    }
}