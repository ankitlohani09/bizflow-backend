package com.bizflow.modules.inventory.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.enums.MovementDirection;
import com.bizflow.modules.catalogue.entity.Item;
import com.bizflow.modules.catalogue.entity.ItemVariant;
import com.bizflow.modules.catalogue.repository.ItemRepository;
import com.bizflow.modules.catalogue.repository.ItemVariantRepository;
import com.bizflow.modules.inventory.dto.StockMovementDto;
import com.bizflow.modules.inventory.entity.Inventory;
import com.bizflow.modules.inventory.entity.StockMovement;
import com.bizflow.modules.inventory.repository.InventoryRepository;
import com.bizflow.modules.inventory.repository.StockMovementRepository;
import com.bizflow.modules.inventory.service.StockMovementService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository movementRepository;
    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final ItemVariantRepository variantRepository;

    @Override
    public ApiResponse<List<StockMovementDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                movementRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<List<StockMovementDto>> getByItem(Long itemId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                movementRepository.findAllByItemIdAndTenantId(itemId, tenantId).stream().map(this::toDto).toList());
    }

    @Override
    @Transactional
    public ApiResponse<StockMovementDto> create(StockMovementDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();

        Item item = itemRepository.findByIdAndTenantId(dto.getItemId(), tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.ITEM_NOT_FOUND));

        ItemVariant variant = dto.getVariantId() != null
                ? variantRepository.findByIdAndTenantId(dto.getVariantId(), tenantId).orElse(null) : null;

        // Save movement
        StockMovement movement = StockMovement.builder().tenantId(tenantId).item(item).variant(variant)
                .movementType(dto.getMovementType()).conditionType(dto.getConditionType()).quantity(dto.getQuantity())
                .direction(dto.getDirection()).referenceType(dto.getReferenceType()).referenceId(dto.getReferenceId())
                .batchNo(dto.getBatchNo()).expiryDate(dto.getExpiryDate()).notes(dto.getNotes()).build();

        movementRepository.save(movement);

        // Update inventory
        Inventory inventory = variant != null
                ? inventoryRepository.findByItemIdAndVariantIdAndTenantId(item.getId(), variant.getId(), tenantId)
                        .orElse(Inventory.builder().tenantId(tenantId).item(item).variant(variant)
                                .availableQty(java.math.BigDecimal.ZERO).damagedQty(java.math.BigDecimal.ZERO)
                                .expiredQty(java.math.BigDecimal.ZERO).reservedQty(java.math.BigDecimal.ZERO).build())
                : inventoryRepository.findByItemIdAndVariantIsNullAndTenantId(item.getId(), tenantId)
                        .orElse(Inventory.builder().tenantId(tenantId).item(item)
                                .availableQty(java.math.BigDecimal.ZERO).damagedQty(java.math.BigDecimal.ZERO)
                                .expiredQty(java.math.BigDecimal.ZERO).reservedQty(java.math.BigDecimal.ZERO).build());

        if (dto.getDirection() == MovementDirection.IN) {
            inventory.setAvailableQty(inventory.getAvailableQty().add(dto.getQuantity()));
        } else {
            inventory.setAvailableQty(inventory.getAvailableQty().subtract(dto.getQuantity()));
        }
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);

        return ApiResponse.success(MessageConstant.STOCK_ADJUSTED, toDto(movement));
    }

    private StockMovementDto toDto(StockMovement m) {
        StockMovementDto dto = new StockMovementDto();
        dto.setId(m.getId());
        dto.setItemId(m.getItem().getId());
        dto.setItemName(m.getItem().getName());
        dto.setVariantId(m.getVariant() != null ? m.getVariant().getId() : null);
        dto.setVariantName(m.getVariant() != null ? m.getVariant().getVariantName() : null);
        dto.setMovementType(m.getMovementType());
        dto.setConditionType(m.getConditionType());
        dto.setQuantity(m.getQuantity());
        dto.setDirection(m.getDirection());
        dto.setReferenceType(m.getReferenceType());
        dto.setReferenceId(m.getReferenceId());
        dto.setBatchNo(m.getBatchNo());
        dto.setExpiryDate(m.getExpiryDate());
        dto.setNotes(m.getNotes());
        dto.setCreatedBy(m.getCreatedBy());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}