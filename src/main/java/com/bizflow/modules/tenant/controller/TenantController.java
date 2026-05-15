package com.bizflow.modules.tenant.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.tenant.dto.TenantRequest;
import com.bizflow.modules.tenant.dto.TenantResponse;
import com.bizflow.modules.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tenants", description = "Tenant management operations")
@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "Get all tenants")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantResponse>>> getAll() {
        return ResponseEntity.ok(tenantService.getAll());
    }

    @Operation(summary = "Get global stats")
    @GetMapping("/global-stats")
    public ResponseEntity<ApiResponse<com.bizflow.modules.tenant.dto.GlobalStatsResponse>> getGlobalStats() {
        return ResponseEntity.ok(tenantService.getGlobalStats());
    }

    @Operation(summary = "Get tenant by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getById(id));
    }

    @Operation(summary = "Create tenant")
    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponse>> create(@Valid @RequestBody TenantRequest request) {
        return ResponseEntity.ok(tenantService.create(request));
    }

    @Operation(summary = "Update tenant")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> update(@PathVariable Long id,
            @Valid @RequestBody TenantRequest request) {
        return ResponseEntity.ok(tenantService.update(id, request));
    }

    @Operation(summary = "Get tenant stats")
    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<com.bizflow.modules.tenant.dto.TenantStatsResponse>> getStats(
            @PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getStats(id));
    }

    @Operation(summary = "Regenerate password reset link")
    @PostMapping("/{id}/regenerate-link")
    public ResponseEntity<ApiResponse<String>> regenerateLink(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.regenerateResetLink(id));
    }

    @Operation(summary = "Delete tenant")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.delete(id));
    }
}