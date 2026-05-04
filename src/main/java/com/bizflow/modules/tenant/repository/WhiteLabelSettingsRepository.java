package com.bizflow.modules.tenant.repository;

import com.bizflow.modules.tenant.entity.WhiteLabelSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WhiteLabelSettingsRepository extends JpaRepository<WhiteLabelSettings, Long> {
    Optional<WhiteLabelSettings> findByTenantId(Long tenantId);
}
