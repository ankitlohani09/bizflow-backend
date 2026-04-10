package com.bizflow.modules.billing.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.enums.MovementDirection;
import com.bizflow.common.enums.MovementType;
import com.bizflow.common.enums.PaymentStatus;
import com.bizflow.modules.billing.dto.InvoiceDto;
import com.bizflow.modules.billing.dto.InvoiceItemDto;
import com.bizflow.modules.billing.dto.PaymentDto;
import com.bizflow.modules.billing.entity.Invoice;
import com.bizflow.modules.billing.entity.InvoiceItem;
import com.bizflow.modules.billing.entity.Payment;
import com.bizflow.modules.billing.entity.PaymentMode;
import com.bizflow.modules.billing.repository.InvoiceItemRepository;
import com.bizflow.modules.billing.repository.InvoiceRepository;
import com.bizflow.modules.billing.repository.PaymentModeRepository;
import com.bizflow.modules.billing.repository.PaymentRepository;
import com.bizflow.modules.billing.service.InvoiceService;
import com.bizflow.modules.catalogue.entity.Item;
import com.bizflow.modules.catalogue.entity.ItemVariant;
import com.bizflow.modules.catalogue.repository.ItemRepository;
import com.bizflow.modules.catalogue.repository.ItemVariantRepository;
import com.bizflow.modules.inventory.dto.StockMovementDto;
import com.bizflow.modules.inventory.service.StockMovementService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final PaymentModeRepository paymentModeRepository;

    @Override
    public ApiResponse<List<InvoiceDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                invoiceRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<InvoiceDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Invoice invoice = invoiceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.INVOICE_NOT_FOUND));
        return ApiResponse.success(toDto(invoice));
    }

    @Override
    @Transactional
    public ApiResponse<InvoiceDto> create(InvoiceDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();

        // Auto-generate invoice number
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

        invoice = invoiceRepository.save(invoice);

        // Save invoice items + trigger stock OUT
        if (dto.getItems() != null) {
            for (InvoiceItemDto itemDto : dto.getItems()) {
                Item item = itemRepository.findByIdAndTenantId(itemDto.getItemId(), tenantId)
                        .orElseThrow(() -> new RuntimeException(MessageConstant.ITEM_NOT_FOUND));
                ItemVariant variant = itemDto.getVariantId() != null
                        ? variantRepository.findByIdAndTenantId(itemDto.getVariantId(), tenantId).orElse(null) : null;

                InvoiceItem invoiceItem = InvoiceItem.builder().tenantId(tenantId).invoice(invoice).item(item)
                        .variant(variant).quantity(itemDto.getQuantity()).unitPrice(itemDto.getUnitPrice())
                        .discountPct(itemDto.getDiscountPct() != null ? itemDto.getDiscountPct() : BigDecimal.ZERO)
                        .taxRate(itemDto.getTaxRate() != null ? itemDto.getTaxRate() : BigDecimal.ZERO)
                        .lineTotal(itemDto.getLineTotal()).build();
                invoiceItemRepository.save(invoiceItem);

                // Stock OUT movement
                if (item.getTrackInventory()) {
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
        }

        // Save payments
        if (dto.getPayments() != null) {
            for (PaymentDto payDto : dto.getPayments()) {

                com.bizflow.modules.billing.entity.PaymentMode paymentMode = paymentModeRepository
                        .findByIdAndTenantId(payDto.getPaymentModeId(), tenantId)
                        .orElseThrow(() -> new RuntimeException(MessageConstant.NOT_FOUND));

                Payment payment = Payment.builder().tenantId(tenantId).invoice(invoice).paymentMode(paymentMode)
                        .amount(payDto.getAmount()).referenceNo(payDto.getReferenceNo()).paidAt(LocalDateTime.now())
                        .build();
                paymentRepository.save(payment);
            }
        }

        return ApiResponse.success(MessageConstant.INVOICE_CREATED, toDto(invoice));
    }

    @Override
    @Transactional
    public ApiResponse<InvoiceDto> addPayment(Long invoiceId, PaymentDto paymentDto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.INVOICE_NOT_FOUND));

        PaymentMode paymentMode = paymentModeRepository.findByIdAndTenantId(paymentDto.getPaymentModeId(), tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.NOT_FOUND));

        Payment payment = Payment.builder().tenantId(tenantId).invoice(invoice).paymentMode(paymentMode)
                .amount(paymentDto.getAmount()).referenceNo(paymentDto.getReferenceNo()).paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // Update paid amount & status
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
        dto.setCustomerName(i.getCustomerName());
        dto.setCustomerPhone(i.getCustomerPhone());
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
        dto.setUnitPrice(ii.getUnitPrice());
        dto.setDiscountPct(ii.getDiscountPct());
        dto.setTaxRate(ii.getTaxRate());
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