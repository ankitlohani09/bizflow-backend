package com.bizflow.modules.tenant.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.tenant.dto.WhiteLabelSettingsDto;
import com.bizflow.modules.tenant.service.WhiteLabelSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(summary = "Upload company logo")
    @PostMapping("/logo")
    public ResponseEntity<ApiResponse<WhiteLabelSettingsDto>> updateLogo(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(whiteLabelSettingsService.updateLogo(file));
    }
}
