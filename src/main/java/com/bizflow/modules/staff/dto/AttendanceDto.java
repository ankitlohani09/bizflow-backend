package com.bizflow.modules.staff.dto;

import com.bizflow.common.enums.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AttendanceDto {
    private Long id;
    private Long staffId;
    private String staffName;
    private LocalDate date;
    private AttendanceStatus status;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private String notes;
}