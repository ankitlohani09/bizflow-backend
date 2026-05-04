package com.bizflow.modules.billing.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.billing.dto.TaxRuleDto;

import java.util.List;

public interface TaxRuleService {
    ApiResponse<List<TaxRuleDto>> getAll();

    ApiResponse<TaxRuleDto> getById(Long id);

    ApiResponse<TaxRuleDto> create(TaxRuleDto dto);

    ApiResponse<TaxRuleDto> update(Long id, TaxRuleDto dto);

    ApiResponse<Void> delete(Long id);
}
