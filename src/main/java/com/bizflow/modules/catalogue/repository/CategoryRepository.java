package com.bizflow.modules.catalogue.repository;

import com.bizflow.modules.catalogue.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByTenantId(Long tenantId);

    Optional<Category> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);

    boolean existsByTenantIdAndNameIgnoreCaseAndIdNot(Long tenantId, String name, Long id);

    boolean existsByTenantIdAndParentId(Long tenantId, Long parentId);
}
