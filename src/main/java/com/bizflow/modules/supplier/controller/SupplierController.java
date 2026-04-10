package com.bizflow.modules.supplier.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.supplier.dto.SupplierDto;
import com.bizflow.modules.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Suppliers")
@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "Get all suppliers")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierDto>>> getAll() {
        return ResponseEntity.ok(supplierService.getAll());
    }

    @Operation(summary = "Get supplier by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @Operation(summary = "Create supplier")
    @PostMapping
    public ResponseEntity<ApiResponse<SupplierDto>> create(@RequestBody SupplierDto dto) {
        return ResponseEntity.ok(supplierService.create(dto));
    }

    @Operation(summary = "Update supplier")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDto>> update(@PathVariable Long id, @RequestBody SupplierDto dto) {
        return ResponseEntity.ok(supplierService.update(id, dto));
    }

    @Operation(summary = "Delete supplier")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.delete(id));
    }
}