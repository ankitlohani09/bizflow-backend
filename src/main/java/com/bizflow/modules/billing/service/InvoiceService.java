package com.bizflow.modules.billing.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.billing.dto.InvoiceDto;
import com.bizflow.modules.billing.dto.PaymentDto;

import java.util.List;

public interface InvoiceService {
    ApiResponse<List<InvoiceDto>> getAll();

    ApiResponse<InvoiceDto> getById(Long id);

    ApiResponse<InvoiceDto> create(InvoiceDto dto);

    ApiResponse<InvoiceDto> addPayment(Long invoiceId, PaymentDto paymentDto);
}