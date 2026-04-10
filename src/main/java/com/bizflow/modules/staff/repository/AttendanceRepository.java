package com.bizflow.modules.staff.repository;

import com.bizflow.modules.staff.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findAllByStaffIdAndTenantId(Long staffId, Long tenantId);

    List<Attendance> findAllByTenantIdAndDate(Long tenantId, LocalDate date);

    Optional<Attendance> findByStaffIdAndDateAndTenantId(Long staffId, LocalDate date, Long tenantId);

    Optional<Attendance> findByIdAndTenantId(Long id, Long tenantId);
}