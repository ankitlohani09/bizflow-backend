package com.bizflow.modules.billing.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.enums.MovementDirection;
import com.bizflow.common.enums.MovementType;
import com.bizflow.common.enums.PaymentStatus;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.billing.dto.InvoiceDto;
import com.bizflow.modules.billing.dto.InvoiceItemDto;
import com.bizflow.modules.billing.dto.PaymentDto;
import com.bizflow.modules.billing.entity.*;
import com.bizflow.modules.billing.repository.InvoiceItemRepository;
import com.bizflow.modules.billing.repository.InvoiceRepository;
import com.bizflow.modules.billing.repository.PaymentModeRepository;
import com.bizflow.modules.billing.repository.PaymentRepository;
import com.bizflow.modules.billing.repository.TaxRuleRepository;
import com.bizflow.modules.billing.service.InvoiceService;
import com.bizflow.modules.catalogue.entity.Item;
import com.bizflow.modules.catalogue.entity.ItemVariant;
import com.bizflow.modules.catalogue.repository.ItemRepository;
import com.bizflow.modules.catalogue.repository.ItemVariantRepository;
import com.bizflow.modules.customer.entity.Customer;
import com.bizflow.modules.customer.repository.CustomerRepository;
import com.bizflow.modules.inventory.dto.StockMovementDto;
import com.bizflow.modules.inventory.service.StockMovementService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bizflow.modules.returns.repository.ReturnItemRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PaymentRepository paymentRepository;
    private final ItemRepository itemRepository;
    private final ItemVariantRepository variantRepository;
    private final StockMovementService stockMovementService;
    private final ReturnItemRepository returnItemRepository;
    private final PaymentModeRepository paymentModeRepository;
    private final CustomerRepository customerRepository;
    private final TaxRuleRepository taxRuleRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<InvoiceDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                invoiceRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<InvoiceDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Invoice invoice = invoiceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.INVOICE_NOT_FOUND));
        return ApiResponse.success(toDto(invoice));
    }

    @Override
    @Transactional
    public ApiResponse<InvoiceDto> create(InvoiceDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();

        long count = invoiceRepository.countByTenantId(tenantId) + 1;
        String invoiceNumber = "INV-" + String.format("%05d", count);

        Invoice invoice = Invoice.builder().tenantId(tenantId).invoiceNumber(invoiceNumber)
                .invoiceType(dto.getInvoiceType()).customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone()).subtotal(dto.getSubtotal())
                .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO)
                .taxAmount(dto.getTaxAmount() != null ? dto.getTaxAmount() : BigDecimal.ZERO)
                .grandTotal(dto.getGrandTotal())
                .paidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO)
                .changeAmount(dto.getChangeAmount() != null ? dto.getChangeAmount() : BigDecimal.ZERO)
                .paymentStatus(dto.getPaymentStatus()).notes(dto.getNotes()).build();

        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findByIdAndTenantId(dto.getCustomerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));
            invoice.setCustomer(customer);

            if (invoice.getCustomerName() == null || invoice.getCustomerName().trim().isEmpty()) {
                invoice.setCustomerName(customer.getName());
            }
            if (invoice.getCustomerPhone() == null || invoice.getCustomerPhone().trim().isEmpty()) {
                invoice.setCustomerPhone(customer.getPhone());
            }

            // Award Loyalty Points (1% of Grand Total)
            int points = dto.getGrandTotal().divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR).intValue();
            if (points > 0) {
                customer.setLoyaltyPoints(
                        (customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0) + points);
                customerRepository.save(customer);
            }
        }

        invoice = invoiceRepository.save(invoice);

        if (dto.getItems() != null) {
            for (InvoiceItemDto itemDto : dto.getItems()) {
                Item item = itemRepository.findByIdAndTenantId(itemDto.getItemId(), tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.ITEM_NOT_FOUND));
                ItemVariant variant = itemDto.getVariantId() != null
                        ? variantRepository.findByIdAndTenantId(itemDto.getVariantId(), tenantId).orElse(null) : null;

                TaxRule taxRule = itemDto.getTaxRuleId() != null
                        ? taxRuleRepository.findByIdAndTenantId(itemDto.getTaxRuleId(), tenantId).orElse(null) : null;

                InvoiceItem invoiceItem = InvoiceItem.builder().tenantId(tenantId).invoice(invoice).item(item)
                        .variant(variant).quantity(itemDto.getQuantity()).unitPrice(itemDto.getUnitPrice())
                        .discountPct(itemDto.getDiscountPct() != null ? itemDto.getDiscountPct() : BigDecimal.ZERO)
                        .taxRate(itemDto.getTaxRate() != null ? itemDto.getTaxRate() : BigDecimal.ZERO).taxRule(taxRule)
                        .lineTotal(itemDto.getLineTotal()).build();
                invoiceItemRepository.save(invoiceItem);

                StockMovementDto movDto = new StockMovementDto();
                movDto.setItemId(item.getId());
                movDto.setVariantId(variant != null ? variant.getId() : null);
                movDto.setMovementType(MovementType.SALE);
                movDto.setDirection(MovementDirection.OUT);
                movDto.setQuantity(itemDto.getQuantity());
                movDto.setReferenceType("INVOICE");
                movDto.setReferenceId(invoice.getId());
                stockMovementService.create(movDto);
            }
        }

        if (dto.getPayments() != null && !dto.getPayments().isEmpty()) {
            BigDecimal totalPaid = BigDecimal.ZERO;
            for (PaymentDto payDto : dto.getPayments()) {
                PaymentMode paymentMode = paymentModeRepository.findByIdAndTenantId(payDto.getPaymentModeId(), tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment mode " + MessageConstant.NOT_FOUND));
                Payment payment = Payment.builder().tenantId(tenantId).invoice(invoice).paymentMode(paymentMode)
                        .amount(payDto.getAmount()).referenceNo(payDto.getReferenceNo()).paidAt(LocalDateTime.now())
                        .build();
                paymentRepository.save(payment);
                totalPaid = totalPaid.add(payDto.getAmount());
            }
            invoice.setPaidAmount(totalPaid);
            if (totalPaid.compareTo(invoice.getGrandTotal()) > 0) {
                invoice.setChangeAmount(totalPaid.subtract(invoice.getGrandTotal()));
            }
            invoiceRepository.save(invoice);
        }

        return ApiResponse.success(MessageConstant.INVOICE_CREATED, toDto(invoice));
    }

    @Override
    @Transactional
    public ApiResponse<InvoiceDto> addPayment(Long invoiceId, PaymentDto paymentDto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.INVOICE_NOT_FOUND));

        PaymentMode paymentMode = paymentModeRepository.findByIdAndTenantId(paymentDto.getPaymentModeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));

        Payment payment = Payment.builder().tenantId(tenantId).invoice(invoice).paymentMode(paymentMode)
                .amount(paymentDto.getAmount()).referenceNo(paymentDto.getReferenceNo()).paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        List<Payment> allPayments = paymentRepository.findAllByInvoiceId(invoiceId);
        BigDecimal totalPaid = allPayments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setPaidAmount(totalPaid);

        if (totalPaid.compareTo(invoice.getGrandTotal()) >= 0) {
            invoice.setPaymentStatus(PaymentStatus.PAID);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setPaymentStatus(PaymentStatus.PARTIAL);
        }
        invoiceRepository.save(invoice);

        return ApiResponse.success(MessageConstant.INVOICE_UPDATED, toDto(invoice));
    }

    private InvoiceDto toDto(Invoice i) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(i.getId());
        dto.setInvoiceNumber(i.getInvoiceNumber());
        dto.setInvoiceType(i.getInvoiceType());
        dto.setCustomerId(i.getCustomer() != null ? i.getCustomer().getId() : null);
        dto.setCustomerName(i.getCustomerName() != null ? i.getCustomerName()
                : (i.getCustomer() != null ? i.getCustomer().getName() : null));
        dto.setCustomerPhone(i.getCustomerPhone() != null ? i.getCustomerPhone()
                : (i.getCustomer() != null ? i.getCustomer().getPhone() : null));
        dto.setSubtotal(i.getSubtotal());
        dto.setDiscountAmount(i.getDiscountAmount());
        dto.setTaxAmount(i.getTaxAmount());
        dto.setGrandTotal(i.getGrandTotal());
        dto.setPaidAmount(i.getPaidAmount());
        dto.setChangeAmount(i.getChangeAmount());
        dto.setPaymentStatus(i.getPaymentStatus());
        dto.setNotes(i.getNotes());
        dto.setCreatedBy(i.getCreatedBy());
        dto.setCreatedAt(i.getCreatedAt());
        dto.setItems(invoiceItemRepository.findAllByInvoiceId(i.getId()).stream().map(this::toItemDto).toList());
        dto.setPayments(paymentRepository.findAllByInvoiceId(i.getId()).stream().map(this::toPaymentDto).toList());
        return dto;
    }

    private InvoiceItemDto toItemDto(InvoiceItem ii) {
        InvoiceItemDto dto = new InvoiceItemDto();
        dto.setId(ii.getId());
        dto.setItemId(ii.getItem().getId());
        dto.setItemName(ii.getItem().getName());
        dto.setVariantId(ii.getVariant() != null ? ii.getVariant().getId() : null);
        dto.setVariantName(ii.getVariant() != null ? ii.getVariant().getVariantName() : null);
        dto.setQuantity(ii.getQuantity());

        // Calculate remaining quantity
        BigDecimal returnedQty = returnItemRepository.sumReturnedQuantity(ii.getInvoice().getId(), ii.getItem().getId(),
                ii.getVariant() != null ? ii.getVariant().getId() : null);
        if (returnedQty == null)
            returnedQty = BigDecimal.ZERO;
        dto.setRemainingQuantity(ii.getQuantity().subtract(returnedQty));

        dto.setUnitPrice(ii.getUnitPrice());
        dto.setDiscountPct(ii.getDiscountPct());
        dto.setTaxRate(ii.getTaxRate());
        dto.setTaxRuleId(ii.getTaxRule() != null ? ii.getTaxRule().getId() : null);
        dto.setTaxRuleName(ii.getTaxRule() != null ? ii.getTaxRule().getName() : null);
        dto.setLineTotal(ii.getLineTotal());
        return dto;
    }

    private PaymentDto toPaymentDto(Payment p) {
        PaymentDto dto = new PaymentDto();
        dto.setId(p.getId());
        dto.setInvoiceId(p.getInvoice().getId());
        dto.setPaymentModeId(p.getPaymentMode().getId());
        dto.setPaymentModeName(p.getPaymentMode().getName());
        dto.setAmount(p.getAmount());
        dto.setReferenceNo(p.getReferenceNo());
        dto.setPaidAt(p.getPaidAt());
        return dto;
    }
}