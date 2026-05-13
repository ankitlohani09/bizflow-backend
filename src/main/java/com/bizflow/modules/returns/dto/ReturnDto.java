package com.bizflow.modules.returns.dto;

import com.bizflow.common.enums.ReturnStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReturnDto {
    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private String returnNumber;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalRefund;
    private Long paymentModeId;
    private String paymentModeName;
    private String reason;
    private ReturnStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<ReturnItemDto> items;
}