package com.bizflow.modules.auth.repository;

import com.bizflow.modules.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findAllByUserId(Long userId);

    List<UserRole> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    @Query("SELECT r.name FROM UserRole ur JOIN Role r ON r.id = ur.roleId WHERE ur.userId = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}