package com.bizflow.modules.catalogue.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.catalogue.dto.ItemVariantDto;
import com.bizflow.modules.catalogue.service.ItemVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Item Variants")
@RestController
@RequestMapping("/item-variants")
@RequiredArgsConstructor
public class ItemVariantController {

    private final ItemVariantService itemVariantService;

    @Operation(summary = "Get all variants by item")
    @GetMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<List<ItemVariantDto>>> getAllByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemVariantService.getAllByItem(itemId));
    }

    @Operation(summary = "Get variant by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemVariantDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itemVariantService.getById(id));
    }

    @Operation(summary = "Create variant")
    @PostMapping
    public ResponseEntity<ApiResponse<ItemVariantDto>> create(@RequestBody ItemVariantDto dto) {
        return ResponseEntity.ok(itemVariantService.create(dto));
    }

    @Operation(summary = "Update variant")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemVariantDto>> update(@PathVariable Long id, @RequestBody ItemVariantDto dto) {
        return ResponseEntity.ok(itemVariantService.update(id, dto));
    }

    @Operation(summary = "Delete variant")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(itemVariantService.delete(id));
    }
}