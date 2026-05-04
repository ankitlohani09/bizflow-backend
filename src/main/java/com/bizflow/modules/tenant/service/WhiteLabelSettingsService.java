package com.bizflow.modules.tenant.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.tenant.dto.WhiteLabelSettingsDto;

public interface WhiteLabelSettingsService {
    ApiResponse<WhiteLabelSettingsDto> getCurrentSettings();

    ApiResponse<WhiteLabelSettingsDto> updateCurrentSettings(WhiteLabelSettingsDto request);
}
