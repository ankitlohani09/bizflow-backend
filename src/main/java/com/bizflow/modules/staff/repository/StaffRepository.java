package com.bizflow.modules.staff.repository;

import com.bizflow.modules.staff.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findAllByTenantId(Long tenantId);

    List<Staff> findAllByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    Optional<Staff> findByIdAndTenantId(Long id, Long tenantId);
}