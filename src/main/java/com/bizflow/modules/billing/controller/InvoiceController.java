package com.bizflow.modules.billing.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.billing.dto.InvoiceDto;
import com.bizflow.modules.billing.dto.PaymentDto;
import com.bizflow.modules.billing.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Invoices")
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "Get all invoices")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceDto>>> getAll() {
        return ResponseEntity.ok(invoiceService.getAll());
    }

    @Operation(summary = "Get invoice by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @Operation(summary = "Create invoice")
    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceDto>> create(@RequestBody InvoiceDto dto) {
        return ResponseEntity.ok(invoiceService.create(dto));
    }

    @Operation(summary = "Add payment to invoice")
    @PostMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<InvoiceDto>> addPayment(@PathVariable Long id,
            @RequestBody PaymentDto paymentDto) {
        return ResponseEntity.ok(invoiceService.addPayment(id, paymentDto));
    }
}