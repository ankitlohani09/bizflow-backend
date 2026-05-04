package com.bizflow.modules.billing.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.billing.dto.TaxRuleDto;
import com.bizflow.modules.billing.service.TaxRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tax Rules")
@RestController
@RequestMapping("/tax-rules")
@RequiredArgsConstructor
public class TaxRuleController {

    private final TaxRuleService taxRuleService;

    @Operation(summary = "Get all tax rules")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaxRuleDto>>> getAll() {
        return ResponseEntity.ok(taxRuleService.getAll());
    }

    @Operation(summary = "Get tax rule by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxRuleDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taxRuleService.getById(id));
    }

    @Operation(summary = "Create tax rule")
    @PostMapping
    public ResponseEntity<ApiResponse<TaxRuleDto>> create(@RequestBody TaxRuleDto dto) {
        return ResponseEntity.ok(taxRuleService.create(dto));
    }

    @Operation(summary = "Update tax rule")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxRuleDto>> update(@PathVariable Long id, @RequestBody TaxRuleDto dto) {
        return ResponseEntity.ok(taxRuleService.update(id, dto));
    }

    @Operation(summary = "Delete tax rule (deactivate)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(taxRuleService.delete(id));
    }
}
