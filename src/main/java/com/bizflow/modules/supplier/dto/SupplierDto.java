package com.bizflow.modules.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SupplierDto {
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;

    private String contactName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;
    private String address;
    private String gstin;
    private Boolean isActive;
}