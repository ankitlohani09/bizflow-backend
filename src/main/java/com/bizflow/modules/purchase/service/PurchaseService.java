package com.bizflow.modules.purchase.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.purchase.dto.PurchaseDto;

import java.util.List;

public interface PurchaseService {
    ApiResponse<List<PurchaseDto>> getAll();

    ApiResponse<PurchaseDto> getById(Long id);

    ApiResponse<PurchaseDto> create(PurchaseDto dto);

    ApiResponse<PurchaseDto> updatePaymentStatus(Long id, String status);
}