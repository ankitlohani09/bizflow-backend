package com.bizflow.modules.inventory.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.inventory.dto.InventoryDto;
import com.bizflow.modules.inventory.entity.Inventory;
import com.bizflow.modules.inventory.repository.InventoryRepository;
import com.bizflow.modules.inventory.service.InventoryService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    public ApiResponse<List<InventoryDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(inventoryRepository.findAllByTenantId(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<InventoryDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Inventory inv = inventoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.INVENTORY_NOT_FOUND));
        return ApiResponse.success(toDto(inv));
    }

    @Override
    public ApiResponse<InventoryDto> updateThreshold(Long id, BigDecimal threshold) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Inventory inv = inventoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.INVENTORY_NOT_FOUND));
        inv.setLowStockThreshold(threshold);
        return ApiResponse.success(MessageConstant.INVENTORY_UPDATED, toDto(inventoryRepository.save(inv)));
    }

    private InventoryDto toDto(Inventory i) {
        InventoryDto dto = new InventoryDto();
        dto.setId(i.getId());
        dto.setItemId(i.getItem().getId());
        dto.setItemName(i.getItem().getName());
        dto.setVariantId(i.getVariant() != null ? i.getVariant().getId() : null);
        dto.setVariantName(i.getVariant() != null ? i.getVariant().getVariantName() : null);
        dto.setAvailableQty(i.getAvailableQty());
        dto.setDamagedQty(i.getDamagedQty());
        dto.setExpiredQty(i.getExpiredQty());
        dto.setReservedQty(i.getReservedQty());
        dto.setLowStockThreshold(i.getLowStockThreshold());
        dto.setUpdatedAt(i.getUpdatedAt());
        if (i.getLowStockThreshold() != null) {
            dto.setLowStock(i.getAvailableQty().compareTo(i.getLowStockThreshold()) < 0);
        }
        return dto;
    }
}