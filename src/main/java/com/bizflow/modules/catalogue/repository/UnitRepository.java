package com.bizflow.modules.catalogue.repository;

import com.bizflow.modules.catalogue.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    List<Unit> findAllByTenantId(Long tenantId);

    Optional<Unit> findByIdAndTenantId(Long id, Long tenantId);
}