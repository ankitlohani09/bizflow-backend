package com.bizflow.modules.user.repository;

import com.bizflow.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByTenantId(Long tenantId);

    Optional<User> findByEmailAndTenantId(String email, Long tenantId);

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndTenantId(String email, Long tenantId);
}
