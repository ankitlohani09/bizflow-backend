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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WhiteLabelSettingsServiceImpl implements WhiteLabelSettingsService {

    private final WhiteLabelSettingsRepository whiteLabelSettingsRepository;
    private static final String UPLOAD_DIR = "uploads/branding/";

    @Override
    public ApiResponse<WhiteLabelSettingsDto> getCurrentSettings() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        WhiteLabelSettings settings = whiteLabelSettingsRepository.findByTenantId(tenantId)
                .orElse(WhiteLabelSettings.builder().tenantId(tenantId).brandName("BizFlow").primaryColor("#6366f1").build());
        return ApiResponse.success(toDto(settings));
    }

    @Override
    public ApiResponse<WhiteLabelSettingsDto> updateCurrentSettings(WhiteLabelSettingsDto request) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        WhiteLabelSettings settings = whiteLabelSettingsRepository.findByTenantId(tenantId)
                .orElse(WhiteLabelSettings.builder().tenantId(tenantId).build());

        settings.setBrandName(request.getBrandName() != null ? request.getBrandName() : "BizFlow");
        settings.setLogoUrl(request.getLogoUrl() != null ? request.getLogoUrl() : settings.getLogoUrl());
        settings.setPrimaryColor(request.getPrimaryColor() != null ? request.getPrimaryColor() : "#6366f1");
        settings.setSecondaryColor(request.getSecondaryColor());
        settings.setDomainName(request.getDomainName());
        settings.setSupportEmail(request.getSupportEmail());

        WhiteLabelSettings saved = whiteLabelSettingsRepository.save(settings);
        return ApiResponse.success(MessageConstant.UPDATED, toDto(saved));
    }

    @Override
    public ApiResponse<WhiteLabelSettingsDto> updateLogo(MultipartFile file) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        WhiteLabelSettings settings = whiteLabelSettingsRepository.findByTenantId(tenantId)
                .orElse(WhiteLabelSettings.builder()
                        .tenantId(tenantId)
                        .brandName("BizFlow")
                        .primaryColor("#6366f1")
                        .build());

        try {
            Path root = Paths.get(UPLOAD_DIR);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            // Delete old logo if exists
            if (settings.getLogoUrl() != null && settings.getLogoUrl().startsWith("/uploads/branding/")) {
                try {
                    String oldPath = settings.getLogoUrl().substring(1);
                    Files.deleteIfExists(Paths.get(oldPath));
                } catch (IOException e) {
                    // Log and continue
                }
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), root.resolve(filename));
            
            String logoUrl = "/" + UPLOAD_DIR + filename;
            settings.setLogoUrl(logoUrl);
            
            // Ensure we don't save nulls if they were already there
            if (settings.getBrandName() == null) settings.setBrandName("BizFlow");
            if (settings.getPrimaryColor() == null) settings.setPrimaryColor("#6366f1");

            WhiteLabelSettings saved = whiteLabelSettingsRepository.save(settings);
            return ApiResponse.success("Logo uploaded successfully", toDto(saved));
            
        } catch (IOException e) {
            return ApiResponse.error("Failed to store logo: " + e.getMessage());
        }
    }

    private WhiteLabelSettingsDto toDto(WhiteLabelSettings entity) {
        WhiteLabelSettingsDto dto = new WhiteLabelSettingsDto();
        dto.setId(entity.getId());
        dto.setBrandName(entity.getBrandName() != null ? entity.getBrandName() : "BizFlow");
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setPrimaryColor(entity.getPrimaryColor() != null ? entity.getPrimaryColor() : "#6366f1");
        dto.setSecondaryColor(entity.getSecondaryColor());
        dto.setDomainName(entity.getDomainName());
        dto.setSupportEmail(entity.getSupportEmail());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
