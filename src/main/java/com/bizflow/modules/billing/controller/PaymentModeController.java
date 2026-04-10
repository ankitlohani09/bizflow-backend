package com.bizflow.modules.billing.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.billing.dto.PaymentModeDto;
import com.bizflow.modules.billing.service.PaymentModeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payment Modes")
@RestController
@RequestMapping("/payment-modes")
@RequiredArgsConstructor
public class PaymentModeController {

    private final PaymentModeService paymentModeService;

    @Operation(summary = "Get all active payment modes")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentModeDto>>> getAll() {
        return ResponseEntity.ok(paymentModeService.getAll());
    }

    @Operation(summary = "Create payment mode")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentModeDto>> create(@RequestBody PaymentModeDto dto) {
        return ResponseEntity.ok(paymentModeService.create(dto));
    }

    @Operation(summary = "Update payment mode")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentModeDto>> update(@PathVariable Long id, @RequestBody PaymentModeDto dto) {
        return ResponseEntity.ok(paymentModeService.update(id, dto));
    }

    @Operation(summary = "Delete payment mode")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(paymentModeService.delete(id));
    }
}