package com.bizflow.modules.logs.repository;

import com.bizflow.modules.logs.entity.AiLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiLogRepository extends JpaRepository<AiLog, Long> {
    List<AiLog> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<AiLog> findAllByTenantIdAndUserIdOrderByCreatedAtDesc(Long tenantId, Long userId);
}