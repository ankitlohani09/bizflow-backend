package com.bizflow.modules.catalogue.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.catalogue.dto.UnitDto;
import com.bizflow.modules.catalogue.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Units")
@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @Operation(summary = "Get all units")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UnitDto>>> getAll() {
        return ResponseEntity.ok(unitService.getAll());
    }

    @Operation(summary = "Get unit by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.getById(id));
    }

    @Operation(summary = "Create unit")
    @PostMapping
    public ResponseEntity<ApiResponse<UnitDto>> create(@RequestBody UnitDto dto) {
        return ResponseEntity.ok(unitService.create(dto));
    }

    @Operation(summary = "Update unit")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitDto>> update(@PathVariable Long id, @RequestBody UnitDto dto) {
        return ResponseEntity.ok(unitService.update(id, dto));
    }

    @Operation(summary = "Delete unit")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.delete(id));
    }
}