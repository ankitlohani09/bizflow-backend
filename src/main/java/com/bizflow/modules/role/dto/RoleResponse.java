package com.bizflow.modules.role.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoleResponse {
    private Long id;
    private Long tenantId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
