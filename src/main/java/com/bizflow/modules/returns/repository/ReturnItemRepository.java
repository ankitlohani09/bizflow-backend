package com.bizflow.modules.returns.repository;

import com.bizflow.modules.returns.entity.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    List<ReturnItem> findAllByReturnRefId(Long returnId);

    @Query("SELECT SUM(ri.quantity) FROM ReturnItem ri WHERE ri.returnRef.invoice.id = :invoiceId AND ri.item.id = :itemId AND (:variantId IS NULL OR ri.variant.id = :variantId)")
    BigDecimal sumReturnedQuantity(@Param("invoiceId") Long invoiceId, @Param("itemId") Long itemId,
            @Param("variantId") Long variantId);
}