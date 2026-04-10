package com.bizflow.modules.catalogue.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemVariantDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private String variantName;
    private String sku;
    private String barcode;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private Boolean isActive;
}