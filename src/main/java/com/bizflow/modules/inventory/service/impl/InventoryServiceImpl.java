package com.bizflow.modules.inventory.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.catalogue.entity.Item;
import com.bizflow.modules.catalogue.repository.ItemRepository;
import com.bizflow.modules.inventory.dto.InventoryDto;
import com.bizflow.modules.inventory.entity.Inventory;
import com.bizflow.modules.inventory.repository.InventoryRepository;
import com.bizflow.modules.inventory.service.InventoryService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;

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
        dto.setSellingPrice(i.getItem().getSellingPrice());
        dto.setCostPrice(i.getItem().getCostPrice());
        dto.setLowStockThreshold(i.getLowStockThreshold());
        dto.setBatchNo(i.getBatchNo());
        dto.setExpiryDate(i.getExpiryDate());
        dto.setMrp(i.getMrp());
        dto.setLocation(i.getLocation());
        dto.setUpdatedAt(i.getUpdatedAt());
        if (i.getLowStockThreshold() != null) {
            dto.setLowStock(i.getAvailableQty().compareTo(i.getLowStockThreshold()) < 0);
        }
        return dto;
    }

    @Override
    @Transactional
    public ApiResponse<String> syncMissingInventory() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        List<Item> items = itemRepository.findAllByTenantId(tenantId);
        List<Inventory> existingInventories = inventoryRepository.findAllByTenantId(tenantId);

        Set<Long> existingItemIds = existingInventories.stream().map(inv -> inv.getItem().getId())
                .collect(Collectors.toSet());

        List<Item> missingItems = items.stream().filter(item -> !existingItemIds.contains(item.getId())).toList();

        for (Item item : missingItems) {
            Inventory inv = Inventory.builder().item(item).availableQty(BigDecimal.ZERO).damagedQty(BigDecimal.ZERO)
                    .expiredQty(BigDecimal.ZERO).reservedQty(BigDecimal.ZERO).tenantId(tenantId).build();
            inventoryRepository.save(inv);
        }

        return ApiResponse.success("Synchronized " + missingItems.size() + " items.");
    }
}