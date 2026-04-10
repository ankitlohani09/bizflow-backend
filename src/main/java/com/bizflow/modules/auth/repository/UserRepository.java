package com.bizflow.modules.auth.repository;

import com.bizflow.modules.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndTenantId(String email, Long tenantId);

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndTenantId(String email, Long tenantId);
}