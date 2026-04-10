package com.bizflow.modules.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseItemDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private Long variantId;
    private String variantName;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal taxRate;
    private BigDecimal lineTotal;
    private String batchNo;
    private LocalDate expiryDate;
}