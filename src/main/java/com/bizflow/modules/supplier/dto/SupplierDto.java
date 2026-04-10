package com.bizflow.modules.supplier.dto;

import lombok.Data;

@Data
public class SupplierDto {
    private Long id;
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String gstin;
    private Boolean isActive;
}