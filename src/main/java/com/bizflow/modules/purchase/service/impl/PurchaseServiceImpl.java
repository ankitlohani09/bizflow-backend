package com.bizflow.modules.purchase.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.enums.MovementDirection;
import com.bizflow.common.enums.MovementType;
import com.bizflow.common.enums.PurchasePaymentStatus;
import com.bizflow.modules.catalogue.entity.Item;
import com.bizflow.modules.catalogue.entity.ItemVariant;
import com.bizflow.modules.catalogue.repository.ItemRepository;
import com.bizflow.modules.catalogue.repository.ItemVariantRepository;
import com.bizflow.modules.inventory.dto.StockMovementDto;
import com.bizflow.modules.inventory.service.StockMovementService;
import com.bizflow.modules.purchase.dto.PurchaseDto;
import com.bizflow.modules.purchase.dto.PurchaseItemDto;
import com.bizflow.modules.purchase.entity.Purchase;
import com.bizflow.modules.purchase.entity.PurchaseItem;
import com.bizflow.modules.purchase.repository.PurchaseItemRepository;
import com.bizflow.modules.purchase.repository.PurchaseRepository;
import com.bizflow.modules.purchase.service.PurchaseService;
import com.bizflow.modules.supplier.entity.Supplier;
import com.bizflow.modules.supplier.repository.SupplierRepository;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;
    private final ItemVariantRepository variantRepository;
    private final StockMovementService stockMovementService;

    @Override
    public ApiResponse<List<PurchaseDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                purchaseRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<PurchaseDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Purchase purchase = purchaseRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.PURCHASE_NOT_FOUND));
        return ApiResponse.success(toDto(purchase));
    }

    @Override
    @Transactional
    public ApiResponse<PurchaseDto> create(PurchaseDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();

        Supplier supplier = dto.getSupplierId() != null
                ? supplierRepository.findByIdAndTenantId(dto.getSupplierId(), tenantId).orElse(null) : null;

        // Auto-generate purchase number
        long count = purchaseRepository.countByTenantId(tenantId) + 1;
        String purchaseNumber = "PO-" + String.format("%05d", count);

        Purchase purchase = Purchase.builder().tenantId(tenantId).supplier(supplier).purchaseNumber(purchaseNumber)
                .purchaseDate(dto.getPurchaseDate()).subtotal(dto.getSubtotal())
                .taxAmount(dto.getTaxAmount() != null ? dto.getTaxAmount() : java.math.BigDecimal.ZERO)
                .grandTotal(dto.getGrandTotal()).paymentStatus(dto.getPaymentStatus()).notes(dto.getNotes()).build();

        purchase = purchaseRepository.save(purchase);

        // Save purchase items + Stock IN
        if (dto.getItems() != null) {
            for (PurchaseItemDto itemDto : dto.getItems()) {
                Item item = itemRepository.findByIdAndTenantId(itemDto.getItemId(), tenantId)
                        .orElseThrow(() -> new RuntimeException(MessageConstant.ITEM_NOT_FOUND));
                ItemVariant variant = itemDto.getVariantId() != null
                        ? variantRepository.findByIdAndTenantId(itemDto.getVariantId(), tenantId).orElse(null) : null;

                PurchaseItem purchaseItem = PurchaseItem.builder().tenantId(tenantId).purchase(purchase).item(item)
                        .variant(variant).quantity(itemDto.getQuantity()).unitCost(itemDto.getUnitCost())
                        .taxRate(itemDto.getTaxRate() != null ? itemDto.getTaxRate() : java.math.BigDecimal.ZERO)
                        .lineTotal(itemDto.getLineTotal()).batchNo(itemDto.getBatchNo())
                        .expiryDate(itemDto.getExpiryDate()).build();
                purchaseItemRepository.save(purchaseItem);

                // Stock IN movement
                if (item.getTrackInventory()) {
                    StockMovementDto movDto = new StockMovementDto();
                    movDto.setItemId(item.getId());
                    movDto.setVariantId(variant != null ? variant.getId() : null);
                    movDto.setMovementType(MovementType.PURCHASE);
                    movDto.setDirection(MovementDirection.IN);
                    movDto.setQuantity(itemDto.getQuantity());
                    movDto.setBatchNo(itemDto.getBatchNo());
                    movDto.setExpiryDate(itemDto.getExpiryDate());
                    movDto.setReferenceType("PURCHASE");
                    movDto.setReferenceId(purchase.getId());
                    stockMovementService.create(movDto);
                }
            }
        }

        return ApiResponse.success(MessageConstant.PURCHASE_CREATED, toDto(purchase));
    }

    @Override
    @Transactional
    public ApiResponse<PurchaseDto> updatePaymentStatus(Long id, String status) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Purchase purchase = purchaseRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.PURCHASE_NOT_FOUND));
        purchase.setPaymentStatus(PurchasePaymentStatus.valueOf(status));
        return ApiResponse.success(MessageConstant.PURCHASE_UPDATED, toDto(purchaseRepository.save(purchase)));
    }

    private PurchaseDto toDto(Purchase p) {
        PurchaseDto dto = new PurchaseDto();
        dto.setId(p.getId());
        dto.setSupplierId(p.getSupplier() != null ? p.getSupplier().getId() : null);
        dto.setSupplierName(p.getSupplier() != null ? p.getSupplier().getName() : null);
        dto.setPurchaseNumber(p.getPurchaseNumber());
        dto.setPurchaseDate(p.getPurchaseDate());
        dto.setSubtotal(p.getSubtotal());
        dto.setTaxAmount(p.getTaxAmount());
        dto.setGrandTotal(p.getGrandTotal());
        dto.setPaymentStatus(p.getPaymentStatus());
        dto.setNotes(p.getNotes());
        dto.setCreatedBy(p.getCreatedBy());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setItems(purchaseItemRepository.findAllByPurchaseId(p.getId()).stream().map(this::toItemDto).toList());
        return dto;
    }

    private PurchaseItemDto toItemDto(PurchaseItem pi) {
        PurchaseItemDto dto = new PurchaseItemDto();
        dto.setId(pi.getId());
        dto.setItemId(pi.getItem().getId());
        dto.setItemName(pi.getItem().getName());
        dto.setVariantId(pi.getVariant() != null ? pi.getVariant().getId() : null);
        dto.setVariantName(pi.getVariant() != null ? pi.getVariant().getVariantName() : null);
        dto.setQuantity(pi.getQuantity());
        dto.setUnitCost(pi.getUnitCost());
        dto.setTaxRate(pi.getTaxRate());
        dto.setLineTotal(pi.getLineTotal());
        dto.setBatchNo(pi.getBatchNo());
        dto.setExpiryDate(pi.getExpiryDate());
        return dto;
    }
}