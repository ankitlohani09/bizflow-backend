package com.bizflow.modules.restaurant.dto;

import lombok.Data;

import java.util.List;

@Data
public class KitchenOrderRequest {
    private String tableNo;
    private String customerName;
    private String notes;
    private List<KitchenOrderItemRequest> items;
}
