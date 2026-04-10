package com.bizflow.modules.billing.dto;

import com.bizflow.common.enums.InvoiceType;
import com.bizflow.common.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceDto {
    private Long id;
    private String invoiceNumber;
    private InvoiceType invoiceType;
    private String customerName;
    private String customerPhone;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;
    private PaymentStatus paymentStatus;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<InvoiceItemDto> items;
    private List<PaymentDto> payments;
}