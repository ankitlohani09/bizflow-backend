package com.bizflow.modules.inventory.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.inventory.dto.InventoryDto;
import com.bizflow.modules.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Inventory")
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get all inventory")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryDto>>> getAll() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @Operation(summary = "Get inventory by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }

    @Operation(summary = "Update low stock threshold")
    @PatchMapping("/{id}/threshold")
    public ResponseEntity<ApiResponse<InventoryDto>> updateThreshold(@PathVariable Long id,
            @RequestParam BigDecimal threshold) {
        return ResponseEntity.ok(inventoryService.updateThreshold(id, threshold));
    }
}