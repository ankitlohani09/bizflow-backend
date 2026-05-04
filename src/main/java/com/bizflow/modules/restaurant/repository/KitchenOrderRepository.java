package com.bizflow.modules.restaurant.repository;

import com.bizflow.modules.restaurant.entity.KitchenOrder;
import com.bizflow.modules.restaurant.enums.KitchenOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KitchenOrderRepository extends JpaRepository<KitchenOrder, Long> {
    List<KitchenOrder> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<KitchenOrder> findAllByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, KitchenOrderStatus status);

    Optional<KitchenOrder> findByIdAndTenantId(Long id, Long tenantId);

    long countByTenantId(Long tenantId);
}
