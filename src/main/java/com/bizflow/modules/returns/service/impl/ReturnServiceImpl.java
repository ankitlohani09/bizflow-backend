package com.bizflow.modules.returns.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.enums.MovementDirection;
import com.bizflow.common.enums.MovementType;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.billing.entity.Invoice;
import com.bizflow.modules.billing.repository.InvoiceRepository;
import com.bizflow.modules.billing.entity.PaymentMode;
import com.bizflow.modules.billing.repository.PaymentModeRepository;
import com.bizflow.modules.catalogue.entity.Item;
import com.bizflow.modules.catalogue.entity.ItemVariant;
import com.bizflow.modules.catalogue.repository.ItemRepository;
import com.bizflow.modules.catalogue.repository.ItemVariantRepository;
import com.bizflow.modules.inventory.dto.StockMovementDto;
import com.bizflow.modules.inventory.service.StockMovementService;
import com.bizflow.modules.returns.dto.ReturnDto;
import com.bizflow.modules.returns.dto.ReturnItemDto;
import com.bizflow.modules.returns.entity.Return;
import com.bizflow.modules.returns.entity.ReturnItem;
import com.bizflow.modules.returns.repository.ReturnItemRepository;
import com.bizflow.modules.returns.repository.ReturnRepository;
import com.bizflow.modules.returns.service.ReturnService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import com.bizflow.modules.billing.repository.InvoiceItemRepository;
import com.bizflow.modules.billing.entity.InvoiceItem;
import com.bizflow.common.enums.ReturnStatus;

@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements ReturnService {

    private final ReturnRepository returnRepository;
    private final ReturnItemRepository returnItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final ItemRepository itemRepository;
    private final ItemVariantRepository variantRepository;
    private final StockMovementService stockMovementService;
    private final PaymentModeRepository paymentModeRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ReturnDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                returnRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ReturnDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Return ret = returnRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.RETURN_NOT_FOUND));
        return ApiResponse.success(toDto(ret));
    }

    @Override
    @Transactional
    public ApiResponse<ReturnDto> create(ReturnDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();

        Invoice invoice = dto.getInvoiceId() != null
                ? invoiceRepository.findByIdAndTenantId(dto.getInvoiceId(), tenantId).orElse(null) : null;

        // Auto-generate return number
        long count = returnRepository.countByTenantId(tenantId) + 1;
        String returnNumber = "RET-" + String.format("%05d", count);

        PaymentMode paymentMode = dto.getPaymentModeId() != null
                ? paymentModeRepository.findByIdAndTenantId(dto.getPaymentModeId(), tenantId).orElse(null) : null;

        Return ret = Return.builder().tenantId(tenantId).invoice(invoice).returnNumber(returnNumber)
                .customerName(dto.getCustomerName()).customerPhone(dto.getCustomerPhone())
                .totalRefund(dto.getTotalRefund()).paymentMode(paymentMode).reason(dto.getReason())
                .status(ReturnStatus.PENDING).build();

        ret = returnRepository.save(ret);

        // Save return items + Stock IN (GOOD condition) / Stock DAMAGE (DAMAGED)
        if (dto.getItems() != null) {
            for (ReturnItemDto itemDto : dto.getItems()) {
                Item item = itemRepository.findByIdAndTenantId(itemDto.getItemId(), tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.ITEM_NOT_FOUND));
                ItemVariant variant = itemDto.getVariantId() != null
                        ? variantRepository.findByIdAndTenantId(itemDto.getVariantId(), tenantId).orElse(null) : null;

                // Validate return quantity against invoice purchased quantity
                BigDecimal alreadyReturned = returnItemRepository.sumReturnedQuantity(invoice.getId(), item.getId(), variant != null ? variant.getId() : null);
                if (alreadyReturned == null) alreadyReturned = BigDecimal.ZERO;

                // Find purchased quantity from invoice items
                BigDecimal purchasedQty = BigDecimal.ZERO;
                List<InvoiceItem> invoiceItems = invoiceItemRepository.findAllByInvoiceId(invoice.getId());
                for (InvoiceItem ii : invoiceItems) {
                    if (ii.getItem().getId().equals(item.getId()) && 
                        (variant == null ? ii.getVariant() == null : ii.getVariant() != null && ii.getVariant().getId().equals(variant.getId()))) {
                        purchasedQty = ii.getQuantity();
                        break;
                    }
                }

                if (purchasedQty.compareTo(BigDecimal.ZERO) == 0) {
                    throw new IllegalArgumentException("Item not found in the original invoice");
                }

                BigDecimal remainingQty = purchasedQty.subtract(alreadyReturned);
                if (itemDto.getQuantity().compareTo(remainingQty) > 0) {
                    throw new IllegalArgumentException("Cannot return more than remaining quantity. Purchased: " + purchasedQty + ", Already Returned: " + alreadyReturned + ", Requested: " + itemDto.getQuantity());
                }

                ReturnItem returnItem = ReturnItem.builder().tenantId(tenantId).returnRef(ret).item(item)
                        .variant(variant).quantity(itemDto.getQuantity()).unitPrice(itemDto.getUnitPrice())
                        .lineTotal(itemDto.getLineTotal()).conditionType(itemDto.getConditionType()).build();
                returnItemRepository.save(returnItem);

                // Stock movement moved to approveReturn
            }
        }

        return ApiResponse.success(MessageConstant.RETURN_CREATED, toDto(ret));
    }

    private ReturnDto toDto(Return r) {
        ReturnDto dto = new ReturnDto();
        dto.setId(r.getId());
        dto.setInvoiceId(r.getInvoice() != null ? r.getInvoice().getId() : null);
        dto.setInvoiceNumber(r.getInvoice() != null ? r.getInvoice().getInvoiceNumber() : null);
        dto.setReturnNumber(r.getReturnNumber());
        dto.setCustomerName(r.getCustomerName() != null ? r.getCustomerName()
                : (r.getInvoice() != null && r.getInvoice().getCustomer() != null ? r.getInvoice().getCustomer().getName() : null));
        dto.setCustomerPhone(r.getCustomerPhone() != null ? r.getCustomerPhone()
                : (r.getInvoice() != null && r.getInvoice().getCustomer() != null ? r.getInvoice().getCustomer().getPhone() : null));
        dto.setTotalRefund(r.getTotalRefund());
        dto.setPaymentModeId(r.getPaymentMode() != null ? r.getPaymentMode().getId() : null);
        dto.setPaymentModeName(r.getPaymentMode() != null ? r.getPaymentMode().getName() : null);
        dto.setReason(r.getReason());
        dto.setStatus(r.getStatus());
        dto.setCreatedBy(r.getCreatedBy());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setItems(returnItemRepository.findAllByReturnRefId(r.getId()).stream().map(this::toItemDto).toList());
        return dto;
    }

    private ReturnItemDto toItemDto(ReturnItem ri) {
        ReturnItemDto dto = new ReturnItemDto();
        dto.setId(ri.getId());
        dto.setItemId(ri.getItem().getId());
        dto.setItemName(ri.getItem().getName());
        dto.setVariantId(ri.getVariant() != null ? ri.getVariant().getId() : null);
        dto.setVariantName(ri.getVariant() != null ? ri.getVariant().getVariantName() : null);
        dto.setQuantity(ri.getQuantity());
        dto.setUnitPrice(ri.getUnitPrice());
        dto.setLineTotal(ri.getLineTotal());
        dto.setConditionType(ri.getConditionType());
        return dto;
    }

    @Override
    @Transactional
    public ApiResponse<ReturnDto> approve(Long id, BigDecimal overrideRefund) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Return ret = returnRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with id " + id));

        if (ret.getStatus() != ReturnStatus.PENDING) {
            throw new IllegalStateException("Return is not in PENDING status");
        }

        if (overrideRefund != null) {
            ret.setTotalRefund(overrideRefund);
        }

        ret.setStatus(ReturnStatus.APPROVED);
        ret = returnRepository.save(ret);

        // Create Stock Movement now!
        List<ReturnItem> items = returnItemRepository.findAllByReturnRefId(ret.getId());
        for (ReturnItem item : items) {
            StockMovementDto movDto = new StockMovementDto();
            movDto.setItemId(item.getItem().getId());
            movDto.setVariantId(item.getVariant() != null ? item.getVariant().getId() : null);
            movDto.setMovementType(MovementType.RETURN);
            movDto.setDirection(MovementDirection.IN);
            movDto.setQuantity(item.getQuantity());
            movDto.setConditionType(item.getConditionType());
            movDto.setReferenceType("RETURN");
            movDto.setReferenceId(ret.getId());
            stockMovementService.create(movDto);
        }

        return ApiResponse.success("Return approved successfully", toDto(ret));
    }

    @Override
    @Transactional
    public ApiResponse<ReturnDto> reject(Long id, String reason) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Return ret = returnRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with id " + id));

        if (ret.getStatus() != ReturnStatus.PENDING) {
            throw new IllegalStateException("Return is not in PENDING status");
        }

        ret.setStatus(ReturnStatus.REJECTED);
        if (reason != null) {
            ret.setReason(ret.getReason() + " | Rejection Reason: " + reason);
        }
        ret = returnRepository.save(ret);

        return ApiResponse.success("Return rejected successfully", toDto(ret));
    }
}