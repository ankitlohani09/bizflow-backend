package com.bizflow.modules.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstin;
    private BigDecimal openingBalance = BigDecimal.ZERO;
    private Boolean isActive = true;
}