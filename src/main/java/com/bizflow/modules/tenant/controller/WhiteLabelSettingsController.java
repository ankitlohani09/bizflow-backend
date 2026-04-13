package com.bizflow.modules.tenant.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.tenant.dto.WhiteLabelSettingsDto;
import com.bizflow.modules.tenant.service.WhiteLabelSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "White Label")
@RestController
@RequestMapping("/white-label/settings")
@RequiredArgsConstructor
public class WhiteLabelSettingsController {

    private final WhiteLabelSettingsService whiteLabelSettingsService;

    @Operation(summary = "Get current tenant white-label settings")
    @GetMapping
    public ResponseEntity<ApiResponse<WhiteLabelSettingsDto>> getCurrentSettings() {
        return ResponseEntity.ok(whiteLabelSettingsService.getCurrentSettings());
    }

    @Operation(summary = "Update current tenant white-label settings")
    @PutMapping
    public ResponseEntity<ApiResponse<WhiteLabelSettingsDto>> updateCurrentSettings(
            @RequestBody WhiteLabelSettingsDto request) {
        return ResponseEntity.ok(whiteLabelSettingsService.updateCurrentSettings(request));
    }
}
