package com.bizflow.modules.catalogue.dto;

import com.bizflow.common.enums.ItemType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemDto {
    private Long id;
    private String name;
    private ItemType type;
    private Long categoryId;
    private String categoryName;
    private Long unitId;
    private String unitName;
    private String description;
    private String barcode;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private BigDecimal taxRate;
    private Boolean hasVariants;
    private Boolean trackInventory;
    private Boolean isActive;
}