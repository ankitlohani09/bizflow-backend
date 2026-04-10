package com.bizflow.modules.expense.dto;

import lombok.Data;

@Data
public class ExpenseCategoryDto {
    private Long id;
    private String name;
    private Boolean isActive;
}