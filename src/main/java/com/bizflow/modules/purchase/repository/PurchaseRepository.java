package com.bizflow.modules.purchase.repository;

import com.bizflow.modules.purchase.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    Optional<Purchase> findByIdAndTenantId(Long id, Long tenantId);

    long countByTenantId(Long tenantId);
}