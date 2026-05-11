package com.bizflow.modules.logs.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityLogDto {
    private Long id;
    private Long userId;
    private String action;
    private String entityType;
    private Long entityId;
    private String description;
    private String ipAddress;
    private String userName;
    private String userRole;
    private LocalDateTime createdAt;
}