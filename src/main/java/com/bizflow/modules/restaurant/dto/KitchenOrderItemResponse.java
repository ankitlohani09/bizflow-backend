package com.bizflow.modules.restaurant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KitchenOrderItemResponse {
    private Long id;
    private Long itemId;
    private String itemName;
    private Long variantId;
    private String variantName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private String notes;
}
