package com.bizflow.modules.tenant.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WhiteLabelSettingsDto {
    private Long id;
    private String brandName;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String domainName;
    private String supportEmail;
    private LocalDateTime updatedAt;
}
