package com.bizflow.modules.billing.repository;

import com.bizflow.modules.billing.entity.PaymentMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentModeRepository extends JpaRepository<PaymentMode, Long> {
    List<PaymentMode> findAllByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    Optional<PaymentMode> findByIdAndTenantId(Long id, Long tenantId);
}