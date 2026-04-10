package com.bizflow.modules.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private Long variantId;
    private String variantName;
    private BigDecimal availableQty;
    private BigDecimal damagedQty;
    private BigDecimal expiredQty;
    private BigDecimal reservedQty;
    private BigDecimal lowStockThreshold;
    private Boolean lowStock;
    private LocalDateTime updatedAt;
}