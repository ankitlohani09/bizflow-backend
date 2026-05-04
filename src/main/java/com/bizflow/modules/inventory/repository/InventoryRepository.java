package com.bizflow.modules.inventory.repository;

import com.bizflow.modules.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT i FROM Inventory i JOIN FETCH i.item LEFT JOIN FETCH i.variant WHERE i.tenantId = ?1")
    List<Inventory> findAllByTenantId(Long tenantId);

    @org.springframework.data.jpa.repository.Query("SELECT i FROM Inventory i JOIN FETCH i.item LEFT JOIN FETCH i.variant WHERE i.id = ?1 AND i.tenantId = ?2")
    Optional<Inventory> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Inventory> findByItemIdAndVariantIdAndTenantId(Long itemId, Long variantId, Long tenantId);

    Optional<Inventory> findByItemIdAndVariantIsNullAndTenantId(Long itemId, Long tenantId);
}