package com.bizflow.modules.catalogue.repository;

import com.bizflow.modules.catalogue.entity.ItemVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {
    List<ItemVariant> findAllByItemIdAndTenantId(Long itemId, Long tenantId);

    Optional<ItemVariant> findByIdAndTenantId(Long id, Long tenantId);
}