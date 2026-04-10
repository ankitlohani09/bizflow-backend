package com.bizflow.modules.inventory.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.inventory.dto.StockMovementDto;

import java.util.List;

public interface StockMovementService {
    ApiResponse<List<StockMovementDto>> getAll();

    ApiResponse<List<StockMovementDto>> getByItem(Long itemId);

    ApiResponse<StockMovementDto> create(StockMovementDto dto);
}