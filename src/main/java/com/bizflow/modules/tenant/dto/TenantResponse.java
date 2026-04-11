package com.bizflow.modules.tenant.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private Long id;
    private String name;
    private String code;
    private String email;
    private String phone;
    private String address;
    private String businessType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}