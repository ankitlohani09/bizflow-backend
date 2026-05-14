package com.bizflow.modules.tenant.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.tenant.dto.WhiteLabelSettingsDto;
import org.springframework.web.multipart.MultipartFile;

public interface WhiteLabelSettingsService {
    ApiResponse<WhiteLabelSettingsDto> getCurrentSettings();

    ApiResponse<WhiteLabelSettingsDto> updateCurrentSettings(WhiteLabelSettingsDto request);

    ApiResponse<WhiteLabelSettingsDto> updateLogo(MultipartFile file);

    ApiResponse<WhiteLabelSettingsDto> deleteLogo();
}
