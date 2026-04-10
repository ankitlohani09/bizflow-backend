package com.bizflow.modules.purchase.repository;

import com.bizflow.modules.purchase.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    List<PurchaseItem> findAllByPurchaseId(Long purchaseId);
}