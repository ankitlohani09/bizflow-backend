package com.bizflow.modules.billing.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaxRuleDto {
    private Long id;
    private String name;
    private BigDecimal rate;
    private String taxType;
    private String description;
    private Boolean isActive;
}
