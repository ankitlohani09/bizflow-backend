package com.bizflow.modules.returns.repository;

import com.bizflow.modules.returns.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReturnRepository extends JpaRepository<Return, Long> {
    List<Return> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    Optional<Return> findByIdAndTenantId(Long id, Long tenantId);

    long countByTenantId(Long tenantId);
}