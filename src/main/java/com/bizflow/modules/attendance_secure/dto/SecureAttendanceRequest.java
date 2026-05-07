package com.bizflow.modules.attendance_secure.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SecureAttendanceRequest {
    private Long staffId;
    private String tenantCode;
    private String pin;
    private String photoBase64; // The selfie in base64 format
    private String location; // GPS coords "lat,long"
    private String notes;
}
