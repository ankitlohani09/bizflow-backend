package com.bizflow.modules.staff.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StaffAdvanceDto {
    private Long id;
    private Long staffId;
    private String staffName;
    private BigDecimal amount;
    private LocalDate advanceDate;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
}