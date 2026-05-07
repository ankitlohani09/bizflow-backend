package com.bizflow.modules.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private com.bizflow.common.enums.ItemType type;
    private String name;
    private String barcode;
    private String categoryName;
    private Long variantId;
    private String variantName;
    private BigDecimal availableQty;
    private BigDecimal damagedQty;
    private BigDecimal expiredQty;
    private BigDecimal reservedQty;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private BigDecimal lowStockThreshold;
    private Boolean lowStock;
    private String batchNo;
    private LocalDateTime expiryDate;
    private BigDecimal mrp;
    private String location;
    private LocalDateTime updatedAt;
}