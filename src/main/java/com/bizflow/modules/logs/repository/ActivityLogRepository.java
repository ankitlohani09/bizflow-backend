package com.bizflow.modules.logs.repository;

import com.bizflow.modules.logs.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<ActivityLog> findAllByTenantIdAndUserIdOrderByCreatedAtDesc(Long tenantId, Long userId);

    List<ActivityLog> findAllByTenantIdAndEntityTypeAndEntityId(Long tenantId, String entityType, Long entityId);
}