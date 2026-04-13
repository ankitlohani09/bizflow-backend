package com.bizflow.modules.restaurant.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.restaurant.dto.KitchenOrderRequest;
import com.bizflow.modules.restaurant.dto.KitchenOrderResponse;
import com.bizflow.modules.restaurant.enums.KitchenOrderStatus;
import com.bizflow.modules.restaurant.service.KitchenOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Kitchen Orders")
@RestController
@RequestMapping("/kitchen-orders")
@RequiredArgsConstructor
public class KitchenOrderController {

    private final KitchenOrderService kitchenOrderService;

    @Operation(summary = "Get all kitchen orders")
    @GetMapping
    public ResponseEntity<ApiResponse<List<KitchenOrderResponse>>> getAll(
            @RequestParam(required = false) KitchenOrderStatus status) {
        return ResponseEntity.ok(kitchenOrderService.getAll(status));
    }

    @Operation(summary = "Get kitchen order by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KitchenOrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kitchenOrderService.getById(id));
    }

    @Operation(summary = "Create kitchen order")
    @PostMapping
    public ResponseEntity<ApiResponse<KitchenOrderResponse>> create(@RequestBody KitchenOrderRequest request) {
        return ResponseEntity.ok(kitchenOrderService.create(request));
    }

    @Operation(summary = "Update kitchen order status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<KitchenOrderResponse>> updateStatus(@PathVariable Long id,
            @RequestParam KitchenOrderStatus status) {
        return ResponseEntity.ok(kitchenOrderService.updateStatus(id, status));
    }
}
