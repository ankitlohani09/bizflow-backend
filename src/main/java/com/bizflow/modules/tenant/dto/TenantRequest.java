package com.bizflow.modules.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String code;

    private String ownerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String address;
    private String businessType;
    private Boolean isActive = true;
    private String subscriptionPlan;
    private java.time.LocalDateTime expiryDate;
    private Integer maxUsers;
    private Boolean isGpsMandatory = false;
    private Boolean isSelfieMandatory = false;
}