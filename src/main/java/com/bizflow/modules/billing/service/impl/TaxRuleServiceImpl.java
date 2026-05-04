package com.bizflow.modules.billing.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.billing.dto.TaxRuleDto;
import com.bizflow.modules.billing.entity.TaxRule;
import com.bizflow.modules.billing.repository.TaxRuleRepository;
import com.bizflow.modules.billing.service.TaxRuleService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxRuleServiceImpl implements TaxRuleService {

    private final TaxRuleRepository taxRuleRepository;

    @Override
    public ApiResponse<List<TaxRuleDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(taxRuleRepository.findAllByTenantId(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<TaxRuleDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        TaxRule taxRule = taxRuleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));
        return ApiResponse.success(toDto(taxRule));
    }

    @Override
    public ApiResponse<TaxRuleDto> create(TaxRuleDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        TaxRule taxRule = TaxRule.builder().tenantId(tenantId).name(dto.getName()).rate(dto.getRate())
                .taxType(dto.getTaxType()).description(dto.getDescription()).isActive(true).build();
        return ApiResponse.success(MessageConstant.CREATED, toDto(taxRuleRepository.save(taxRule)));
    }

    @Override
    public ApiResponse<TaxRuleDto> update(Long id, TaxRuleDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        TaxRule taxRule = taxRuleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));

        taxRule.setName(dto.getName());
        taxRule.setRate(dto.getRate());
        taxRule.setTaxType(dto.getTaxType());
        taxRule.setDescription(dto.getDescription());
        taxRule.setIsActive(dto.getIsActive());

        return ApiResponse.success(MessageConstant.UPDATED, toDto(taxRuleRepository.save(taxRule)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        TaxRule taxRule = taxRuleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));

        taxRule.setIsActive(false);
        taxRuleRepository.save(taxRule);
        return ApiResponse.success(MessageConstant.DELETED, null);
    }

    private TaxRuleDto toDto(TaxRule t) {
        TaxRuleDto dto = new TaxRuleDto();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setRate(t.getRate());
        dto.setTaxType(t.getTaxType());
        dto.setDescription(t.getDescription());
        dto.setIsActive(t.getIsActive());
        return dto;
    }
}
