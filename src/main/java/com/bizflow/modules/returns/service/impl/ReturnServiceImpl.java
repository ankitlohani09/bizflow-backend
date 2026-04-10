package com.bizflow.modules.returns.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.enums.ConditionType;
import com.bizflow.common.enums.MovementDirection;
import com.bizflow.common.enums.MovementType;
import com.bizflow.modules.billing.entity.Invoice;
import com.bizflow.modules.billing.repository.InvoiceRepository;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements ReturnService {

    private final ReturnRepository returnRepository;
    private final ReturnItemRepository returnItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final ItemRepository itemRepository;
    private final ItemVariantRepository variantRepository;
    private final StockMovementService stockMovementService;

    @Override
    public ApiResponse<List<ReturnDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                returnRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<ReturnDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Return ret = returnRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.RETURN_NOT_FOUND));
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

        Return ret = Return.builder().tenantId(tenantId).invoice(invoice).returnNumber(returnNumber)
                .customerName(dto.getCustomerName()).customerPhone(dto.getCustomerPhone())
                .totalRefund(dto.getTotalRefund()).refundMode(dto.getRefundMode()).reason(dto.getReason()).build();

        ret = returnRepository.save(ret);

        // Save return items + Stock IN (GOOD condition) / Stock DAMAGE (DAMAGED)
        if (dto.getItems() != null) {
            for (ReturnItemDto itemDto : dto.getItems()) {
                Item item = itemRepository.findByIdAndTenantId(itemDto.getItemId(), tenantId)
                        .orElseThrow(() -> new RuntimeException(MessageConstant.ITEM_NOT_FOUND));
                ItemVariant variant = itemDto.getVariantId() != null
                        ? variantRepository.findByIdAndTenantId(itemDto.getVariantId(), tenantId).orElse(null) : null;

                ReturnItem returnItem = ReturnItem.builder().tenantId(tenantId).returnRef(ret).item(item)
                        .variant(variant).quantity(itemDto.getQuantity()).unitPrice(itemDto.getUnitPrice())
                        .lineTotal(itemDto.getLineTotal()).conditionType(itemDto.getConditionType()).build();
                returnItemRepository.save(returnItem);

                // Stock movement based on condition
                if (item.getTrackInventory()) {
                    StockMovementDto movDto = new StockMovementDto();
                    movDto.setItemId(item.getId());
                    movDto.setVariantId(variant != null ? variant.getId() : null);
                    movDto.setMovementType(MovementType.RETURN);
                    movDto.setDirection(MovementDirection.IN);
                    movDto.setQuantity(itemDto.getQuantity());
                    movDto.setConditionType(itemDto.getConditionType());
                    movDto.setReferenceType("RETURN");
                    movDto.setReferenceId(ret.getId());
                    stockMovementService.create(movDto);
                }
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
        dto.setCustomerName(r.getCustomerName());
        dto.setCustomerPhone(r.getCustomerPhone());
        dto.setTotalRefund(r.getTotalRefund());
        dto.setRefundMode(r.getRefundMode());
        dto.setReason(r.getReason());
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
}