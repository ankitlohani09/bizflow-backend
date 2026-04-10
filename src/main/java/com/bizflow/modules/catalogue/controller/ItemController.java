package com.bizflow.modules.catalogue.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.catalogue.dto.ItemDto;
import com.bizflow.modules.catalogue.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Items")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "Get all items")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemDto>>> getAll() {
        return ResponseEntity.ok(itemService.getAll());
    }

    @Operation(summary = "Get item by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getById(id));
    }

    @Operation(summary = "Create item")
    @PostMapping
    public ResponseEntity<ApiResponse<ItemDto>> create(@RequestBody ItemDto dto) {
        return ResponseEntity.ok(itemService.create(dto));
    }

    @Operation(summary = "Update item")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto>> update(@PathVariable Long id, @RequestBody ItemDto dto) {
        return ResponseEntity.ok(itemService.update(id, dto));
    }

    @Operation(summary = "Delete item")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.delete(id));
    }
}