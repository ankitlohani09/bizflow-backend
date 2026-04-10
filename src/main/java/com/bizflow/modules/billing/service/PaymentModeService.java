package com.bizflow.modules.billing.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.billing.dto.PaymentModeDto;

import java.util.List;

public interface PaymentModeService {
    ApiResponse<List<PaymentModeDto>> getAll();

    ApiResponse<PaymentModeDto> create(PaymentModeDto dto);

    ApiResponse<PaymentModeDto> update(Long id, PaymentModeDto dto);

    ApiResponse<Void> delete(Long id);
}