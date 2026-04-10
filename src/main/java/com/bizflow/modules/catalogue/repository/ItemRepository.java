package com.bizflow.modules.catalogue.repository;

import com.bizflow.modules.catalogue.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByTenantId(Long tenantId);

    Optional<Item> findByIdAndTenantId(Long id, Long tenantId);
}