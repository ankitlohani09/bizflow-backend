package com.bizflow.modules.restaurant.dto;

import com.bizflow.modules.restaurant.enums.KitchenOrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class KitchenOrderResponse {
    private Long id;
    private String orderNumber;
    private String tableNo;
    private String customerName;
    private KitchenOrderStatus status;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private List<KitchenOrderItemResponse> items;
}
