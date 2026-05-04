package com.bizflow.modules.restaurant.repository;

import com.bizflow.modules.restaurant.entity.KitchenOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KitchenOrderItemRepository extends JpaRepository<KitchenOrderItem, Long> {
    List<KitchenOrderItem> findAllByKitchenOrderId(Long kitchenOrderId);
}
