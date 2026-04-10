package com.bizflow.modules.billing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDto {
    private Long id;
    private Long invoiceId;
    private Long paymentModeId;
    private String paymentModeName;
    private BigDecimal amount;
    private String referenceNo;
    private LocalDateTime paidAt;
}