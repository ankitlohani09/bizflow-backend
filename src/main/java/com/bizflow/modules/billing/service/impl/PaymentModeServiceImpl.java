package com.bizflow.modules.billing.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.modules.billing.dto.PaymentModeDto;
import com.bizflow.modules.billing.entity.PaymentMode;
import com.bizflow.modules.billing.repository.PaymentModeRepository;
import com.bizflow.modules.billing.service.PaymentModeService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentModeServiceImpl implements PaymentModeService {

    private final PaymentModeRepository paymentModeRepository;

    @Override
    public ApiResponse<List<PaymentModeDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                paymentModeRepository.findAllByTenantIdAndIsActive(tenantId, true).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<PaymentModeDto> create(PaymentModeDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        PaymentMode mode = PaymentMode.builder().tenantId(tenantId).name(dto.getName()).isActive(true).build();
        return ApiResponse.success(MessageConstant.CREATED, toDto(paymentModeRepository.save(mode)));
    }

    @Override
    public ApiResponse<PaymentModeDto> update(Long id, PaymentModeDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        PaymentMode mode = paymentModeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.NOT_FOUND));
        mode.setName(dto.getName());
        mode.setIsActive(dto.getIsActive());
        return ApiResponse.success(MessageConstant.UPDATED, toDto(paymentModeRepository.save(mode)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        PaymentMode mode = paymentModeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.NOT_FOUND));
        mode.setIsActive(false); // Soft delete
        paymentModeRepository.save(mode);
        return ApiResponse.success(MessageConstant.DELETED, null);
    }

    private PaymentModeDto toDto(PaymentMode p) {
        PaymentModeDto dto = new PaymentModeDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setIsActive(p.getIsActive());
        return dto;
    }
}