package com.bizflow.modules.staff.repository;

import com.bizflow.modules.staff.entity.StaffBiometric;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StaffBiometricRepository extends JpaRepository<StaffBiometric, Long> {
    Optional<StaffBiometric> findByStaffId(Long staffId);

    Optional<StaffBiometric> findByCredentialId(String credentialId);
}
