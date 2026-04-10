package com.bizflow.modules.inventory.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.inventory.dto.StockMovementDto;
import com.bizflow.modules.inventory.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Stock Movements")
@RestController
@RequestMapping("/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @Operation(summary = "Get all stock movements")
    @GetMapping
    public ResponseEntity<ApiResponse<List<StockMovementDto>>> getAll() {
        return ResponseEntity.ok(stockMovementService.getAll());
    }

    @Operation(summary = "Get movements by item")
    @GetMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<List<StockMovementDto>>> getByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(stockMovementService.getByItem(itemId));
    }

    @Operation(summary = "Create stock movement (manual adjustment)")
    @PostMapping
    public ResponseEntity<ApiResponse<StockMovementDto>> create(@RequestBody StockMovementDto dto) {
        return ResponseEntity.ok(stockMovementService.create(dto));
    }
}