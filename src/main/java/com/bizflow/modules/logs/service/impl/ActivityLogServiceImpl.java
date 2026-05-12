package com.bizflow.modules.logs.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.logs.dto.ActivityLogDto;
import com.bizflow.modules.logs.entity.ActivityLog;
import com.bizflow.modules.logs.repository.ActivityLogRepository;
import com.bizflow.modules.logs.service.ActivityLogService;
import com.bizflow.modules.user.entity.User;
import com.bizflow.modules.user.repository.UserRepository;
import com.bizflow.modules.user.repository.UserRoleRepository;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public ApiResponse<List<ActivityLogDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        List<ActivityLog> logs = activityLogRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId);

        List<Long> userIds = logs.stream().map(ActivityLog::getUserId).filter(Objects::nonNull).distinct().toList();
        List<User> users = userRepository.findAllById(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(com.bizflow.modules.user.entity.User::getId, u -> u));

        return ApiResponse.success(logs.stream().map(log -> {
            ActivityLogDto dto = toDto(log);
            User user = userMap.get(log.getUserId());
            if (user != null) {
                dto.setUserName(user.getName());
                List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
                if (!roles.isEmpty()) {
                    dto.setUserRole(roles.getFirst());
                }
            }
            return dto;
        }).toList());
    }

    @Override
    public ApiResponse<List<ActivityLogDto>> getByUser(Long userId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(activityLogRepository
                .findAllByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, userId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<List<ActivityLogDto>> getByEntity(String entityType, Long entityId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse
                .success(activityLogRepository.findAllByTenantIdAndEntityTypeAndEntityId(tenantId, entityType, entityId)
                        .stream().map(this::toDto).toList());
    }

    // Call this from any service to log activity
    @Override
    public void log(String action, String entityType, Long entityId, String description, String ipAddress) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Long userId = SecurityUtils.getCurrentUserId();
        ActivityLog log = ActivityLog.builder().tenantId(tenantId).userId(userId).action(action).entityType(entityType)
                .entityId(entityId).description(description).ipAddress(ipAddress).build();
        activityLogRepository.save(log);
    }

    @Override
    public void log(Long tenantId, Long userId, String action, String entityType, Long entityId, String description,
            String ipAddress) {
        ActivityLog log = ActivityLog.builder().tenantId(tenantId).userId(userId).action(action).entityType(entityType)
                .entityId(entityId).description(description).ipAddress(ipAddress).build();
        activityLogRepository.save(log);
    }

    private ActivityLogDto toDto(ActivityLog a) {
        ActivityLogDto dto = new ActivityLogDto();
        dto.setId(a.getId());
        dto.setUserId(a.getUserId());
        dto.setAction(a.getAction());
        dto.setEntityType(a.getEntityType());
        dto.setEntityId(a.getEntityId());
        dto.setDescription(a.getDescription());
        dto.setIpAddress(a.getIpAddress());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}