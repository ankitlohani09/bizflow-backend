package com.bizflow.modules.inventory.entity;

import com.bizflow.common.BaseEntity;
import com.bizflow.modules.catalogue.entity.Item;
import com.bizflow.modules.catalogue.entity.ItemVariant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Inventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ItemVariant variant;

    @Builder.Default
    @Column(name = "available_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal availableQty = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "damaged_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal damagedQty = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "expired_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal expiredQty = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "reserved_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal reservedQty = BigDecimal.ZERO;

    @Column(name = "low_stock_threshold", precision = 12, scale = 3)
    private BigDecimal lowStockThreshold;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}