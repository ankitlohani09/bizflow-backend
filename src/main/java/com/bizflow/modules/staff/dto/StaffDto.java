package com.bizflow.modules.staff.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StaffDto {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String role;
    private BigDecimal salary;
    private LocalDate joinDate;
    private Boolean isActive;
}