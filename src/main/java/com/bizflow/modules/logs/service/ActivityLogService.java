package com.bizflow.modules.logs.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.logs.dto.ActivityLogDto;

import java.util.List;

public interface ActivityLogService {
    ApiResponse<List<ActivityLogDto>> getAll();

    ApiResponse<List<ActivityLogDto>> getByUser(Long userId);

    ApiResponse<List<ActivityLogDto>> getByEntity(String entityType, Long entityId);

    void log(String action, String entityType, Long entityId, String description, String ipAddress);
}