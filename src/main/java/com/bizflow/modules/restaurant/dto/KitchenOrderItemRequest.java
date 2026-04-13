package com.bizflow.modules.restaurant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KitchenOrderItemRequest {
    private Long itemId;
    private Long variantId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String notes;
}
