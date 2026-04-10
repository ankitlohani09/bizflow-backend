package com.bizflow.modules.returns.dto;

import com.bizflow.common.enums.ConditionType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReturnItemDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private Long variantId;
    private String variantName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private ConditionType conditionType;
}