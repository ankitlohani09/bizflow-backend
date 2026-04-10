package com.bizflow.modules.inventory.dto;

import com.bizflow.common.enums.ConditionType;
import com.bizflow.common.enums.MovementDirection;
import com.bizflow.common.enums.MovementType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockMovementDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private Long variantId;
    private String variantName;
    private MovementType movementType;
    private ConditionType conditionType;
    private BigDecimal quantity;
    private MovementDirection direction;
    private String referenceType;
    private Long referenceId;
    private String batchNo;
    private LocalDate expiryDate;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
}