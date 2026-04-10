package com.bizflow.modules.expense.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.expense.dto.ExpenseDto;
import com.bizflow.modules.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Expenses")
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Get all expenses")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseDto>>> getAll() {
        return ResponseEntity.ok(expenseService.getAll());
    }

    @Operation(summary = "Get expenses by date range")
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<ExpenseDto>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(expenseService.getByDateRange(from, to));
    }

    @Operation(summary = "Get expense by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getById(id));
    }

    @Operation(summary = "Create expense")
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseDto>> create(@RequestBody ExpenseDto dto) {
        return ResponseEntity.ok(expenseService.create(dto));
    }

    @Operation(summary = "Update expense")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseDto>> update(@PathVariable Long id, @RequestBody ExpenseDto dto) {
        return ResponseEntity.ok(expenseService.update(id, dto));
    }

    @Operation(summary = "Delete expense")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.delete(id));
    }
}