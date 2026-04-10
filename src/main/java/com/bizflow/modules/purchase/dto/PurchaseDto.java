package com.bizflow.modules.purchase.dto;

import com.bizflow.common.enums.PurchasePaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseDto {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private String purchaseNumber;
    private LocalDate purchaseDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private PurchasePaymentStatus paymentStatus;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<PurchaseItemDto> items;
}