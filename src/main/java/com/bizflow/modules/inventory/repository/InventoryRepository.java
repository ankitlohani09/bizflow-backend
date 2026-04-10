package com.bizflow.modules.inventory.repository;

import com.bizflow.modules.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findAllByTenantId(Long tenantId);

    Optional<Inventory> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Inventory> findByItemIdAndVariantIdAndTenantId(Long itemId, Long variantId, Long tenantId);

    Optional<Inventory> findByItemIdAndVariantIsNullAndTenantId(Long itemId, Long tenantId);
}