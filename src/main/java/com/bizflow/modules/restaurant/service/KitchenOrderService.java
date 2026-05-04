package com.bizflow.modules.restaurant.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.restaurant.dto.KitchenOrderRequest;
import com.bizflow.modules.restaurant.dto.KitchenOrderResponse;
import com.bizflow.modules.restaurant.enums.KitchenOrderStatus;

import java.util.List;

public interface KitchenOrderService {
    ApiResponse<List<KitchenOrderResponse>> getAll(KitchenOrderStatus status);

    ApiResponse<KitchenOrderResponse> getById(Long id);

    ApiResponse<KitchenOrderResponse> create(KitchenOrderRequest request);

    ApiResponse<KitchenOrderResponse> updateStatus(Long id, KitchenOrderStatus status);
}
