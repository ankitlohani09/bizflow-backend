package com.bizflow.modules.billing.dto;

import lombok.Data;

@Data
public class PaymentModeDto {
    private Long id;
    private String name;
    private Boolean isActive;
}