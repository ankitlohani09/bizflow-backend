package com.bizflow.modules.role.repository;

import com.bizflow.modules.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findAllByTenantId(Long tenantId);

    Optional<Role> findByNameAndTenantId(String name, Long tenantId);

    Optional<Role> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByNameAndTenantId(String name, Long tenantId);
}
