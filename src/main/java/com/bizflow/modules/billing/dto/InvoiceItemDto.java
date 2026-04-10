package com.bizflow.modules.billing.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceItemDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private Long variantId;
    private String variantName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPct;
    private BigDecimal taxRate;
    private BigDecimal lineTotal;
}