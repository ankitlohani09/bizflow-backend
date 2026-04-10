package com.bizflow.modules.inventory.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.inventory.dto.InventoryDto;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {
    ApiResponse<List<InventoryDto>> getAll();

    ApiResponse<InventoryDto> getById(Long id);

    ApiResponse<InventoryDto> updateThreshold(Long id, BigDecimal threshold);
}