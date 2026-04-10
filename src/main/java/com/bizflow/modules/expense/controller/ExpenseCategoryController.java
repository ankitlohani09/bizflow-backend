package com.bizflow.modules.expense.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.expense.dto.ExpenseCategoryDto;
import com.bizflow.modules.expense.service.ExpenseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Expense Categories")
@RestController
@RequestMapping("/expense-categories")
@RequiredArgsConstructor
public class ExpenseCategoryController {

    private final ExpenseCategoryService expenseCategoryService;

    @Operation(summary = "Get all expense categories")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseCategoryDto>>> getAll() {
        return ResponseEntity.ok(expenseCategoryService.getAll());
    }

    @Operation(summary = "Create expense category")
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseCategoryDto>> create(@RequestBody ExpenseCategoryDto dto) {
        return ResponseEntity.ok(expenseCategoryService.create(dto));
    }

    @Operation(summary = "Update expense category")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseCategoryDto>> update(@PathVariable Long id,
            @RequestBody ExpenseCategoryDto dto) {
        return ResponseEntity.ok(expenseCategoryService.update(id, dto));
    }

    @Operation(summary = "Delete expense category")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(expenseCategoryService.delete(id));
    }
}