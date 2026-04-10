package com.bizflow.modules.purchase.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.purchase.dto.PurchaseDto;
import com.bizflow.modules.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Purchases")
@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @Operation(summary = "Get all purchases")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseDto>>> getAll() {
        return ResponseEntity.ok(purchaseService.getAll());
    }

    @Operation(summary = "Get purchase by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getById(id));
    }

    @Operation(summary = "Create purchase")
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseDto>> create(@RequestBody PurchaseDto dto) {
        return ResponseEntity.ok(purchaseService.create(dto));
    }

    @Operation(summary = "Update payment status")
    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<ApiResponse<PurchaseDto>> updatePaymentStatus(@PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(purchaseService.updatePaymentStatus(id, status));
    }
}