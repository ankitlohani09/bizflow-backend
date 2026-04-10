package com.bizflow.modules.expense.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseDto {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private Long paymentModeId;
    private String paymentModeName;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
}