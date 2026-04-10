package com.bizflow.modules.inventory.repository;

import com.bizflow.modules.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<StockMovement> findAllByItemIdAndTenantId(Long itemId, Long tenantId);
}