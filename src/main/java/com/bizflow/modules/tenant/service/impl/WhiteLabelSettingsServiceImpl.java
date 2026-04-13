package com.bizflow.modules.tenant.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.modules.tenant.dto.WhiteLabelSettingsDto;
import com.bizflow.modules.tenant.entity.WhiteLabelSettings;
import com.bizflow.modules.tenant.repository.WhiteLabelSettingsRepository;
import com.bizflow.modules.tenant.service.WhiteLabelSettingsService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WhiteLabelSettingsServiceImpl implements WhiteLabelSettingsService {

    private final WhiteLabelSettingsRepository whiteLabelSettingsRepository;

    @Override
    public ApiResponse<WhiteLabelSettingsDto> getCurrentSettings() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        WhiteLabelSettings settings = whiteLabelSettingsRepository.findByTenantId(tenantId)
                .orElse(WhiteLabelSettings.builder().tenantId(tenantId).build());
        return ApiResponse.success(toDto(settings));
    }

    @Override
    public ApiResponse<WhiteLabelSettingsDto> updateCurrentSettings(WhiteLabelSettingsDto request) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        WhiteLabelSettings settings = whiteLabelSettingsRepository.findByTenantId(tenantId)
                .orElse(WhiteLabelSettings.builder().tenantId(tenantId).build());

        settings.setBrandName(request.getBrandName());
        settings.setLogoUrl(request.getLogoUrl());
        settings.setPrimaryColor(request.getPrimaryColor());
        settings.setSecondaryColor(request.getSecondaryColor());
        settings.setDomainName(request.getDomainName());
        settings.setSupportEmail(request.getSupportEmail());

        WhiteLabelSettings saved = whiteLabelSettingsRepository.save(settings);
        return ApiResponse.success(MessageConstant.UPDATED, toDto(saved));
    }

    private WhiteLabelSettingsDto toDto(WhiteLabelSettings entity) {
        WhiteLabelSettingsDto dto = new WhiteLabelSettingsDto();
        dto.setId(entity.getId());
        dto.setBrandName(entity.getBrandName());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setPrimaryColor(entity.getPrimaryColor());
        dto.setSecondaryColor(entity.getSecondaryColor());
        dto.setDomainName(entity.getDomainName());
        dto.setSupportEmail(entity.getSupportEmail());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
