package com.bizflow.modules.restaurant.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.restaurant.dto.KitchenOrderRequest;
import com.bizflow.modules.restaurant.dto.KitchenOrderResponse;
import com.bizflow.modules.restaurant.enums.KitchenOrderStatus;
import com.bizflow.modules.restaurant.service.KitchenOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Kitchen Orders", description = "Restaurant kitchen order management")
@RestController
@RequestMapping("/kitchen-orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class KitchenOrderController {

    private final KitchenOrderService kitchenOrderService;

    @Operation(summary = "Get all kitchen orders")
    @GetMapping
    @PreAuthorize("@rbac.hasPermission('KITCHEN_READ')")
    public ResponseEntity<ApiResponse<List<KitchenOrderResponse>>> getAll(
            @RequestParam(required = false) KitchenOrderStatus status) {
        return ResponseEntity.ok(kitchenOrderService.getAll(status));
    }

    @Operation(summary = "Get kitchen order by id")
    @GetMapping("/{id}")
    @PreAuthorize("@rbac.hasPermission('KITCHEN_READ')")
    public ResponseEntity<ApiResponse<KitchenOrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kitchenOrderService.getById(id));
    }

    @Operation(summary = "Create kitchen order")
    @PostMapping
    @PreAuthorize("@rbac.hasPermission('KITCHEN_WRITE')")
    public ResponseEntity<ApiResponse<KitchenOrderResponse>> create(@Valid @RequestBody KitchenOrderRequest request) {
        return ResponseEntity.ok(kitchenOrderService.create(request));
    }

    @Operation(summary = "Update kitchen order status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("@rbac.hasPermission('KITCHEN_WRITE')")
    public ResponseEntity<ApiResponse<KitchenOrderResponse>> updateStatus(@PathVariable Long id,
            @RequestParam KitchenOrderStatus status) {
        return ResponseEntity.ok(kitchenOrderService.updateStatus(id, status));
    }
}