package com.bizflow.modules.staff.repository;

import com.bizflow.modules.staff.entity.StaffAdvance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffAdvanceRepository extends JpaRepository<StaffAdvance, Long> {
    List<StaffAdvance> findAllByStaffIdAndTenantId(Long staffId, Long tenantId);

    Optional<StaffAdvance> findByIdAndTenantId(Long id, Long tenantId);
}