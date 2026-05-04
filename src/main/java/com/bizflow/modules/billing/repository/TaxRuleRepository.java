package com.bizflow.modules.billing.repository;

import com.bizflow.modules.billing.entity.TaxRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxRuleRepository extends JpaRepository<TaxRule, Long> {
    List<TaxRule> findAllByTenantId(Long tenantId);

    List<TaxRule> findAllByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    Optional<TaxRule> findByIdAndTenantId(Long id, Long tenantId);
}
