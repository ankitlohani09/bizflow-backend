package com.bizflow.modules.catalogue.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.modules.catalogue.dto.ItemDto;
import com.bizflow.modules.catalogue.entity.Category;
import com.bizflow.modules.catalogue.entity.Item;
import com.bizflow.modules.catalogue.entity.Unit;
import com.bizflow.modules.catalogue.repository.CategoryRepository;
import com.bizflow.modules.catalogue.repository.ItemRepository;
import com.bizflow.modules.catalogue.repository.UnitRepository;
import com.bizflow.modules.catalogue.service.ItemService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;

    @Override
    public ApiResponse<List<ItemDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(itemRepository.findAllByTenantId(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<ItemDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Item item = itemRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.ITEM_NOT_FOUND));
        return ApiResponse.success(toDto(item));
    }

    @Override
    public ApiResponse<ItemDto> create(ItemDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Item item = buildItem(new Item(), dto, tenantId);
        item.setTenantId(tenantId);
        return ApiResponse.success(MessageConstant.ITEM_CREATED, toDto(itemRepository.save(item)));
    }

    @Override
    public ApiResponse<ItemDto> update(Long id, ItemDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Item item = itemRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.ITEM_NOT_FOUND));
        buildItem(item, dto, tenantId);
        return ApiResponse.success(MessageConstant.ITEM_UPDATED, toDto(itemRepository.save(item)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Item item = itemRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException(MessageConstant.ITEM_NOT_FOUND));
        itemRepository.delete(item);
        return ApiResponse.success(MessageConstant.ITEM_DELETED, null);
    }

    private Item buildItem(Item item, ItemDto dto, Long tenantId) {
        Category category = dto.getCategoryId() != null
                ? categoryRepository.findByIdAndTenantId(dto.getCategoryId(), tenantId).orElse(null) : null;
        Unit unit = dto.getUnitId() != null ? unitRepository.findByIdAndTenantId(dto.getUnitId(), tenantId).orElse(null)
                : null;
        item.setName(dto.getName());
        item.setType(dto.getType());
        item.setCategory(category);
        item.setUnit(unit);
        item.setDescription(dto.getDescription());
        item.setBarcode(dto.getBarcode());
        item.setSellingPrice(dto.getSellingPrice());
        item.setCostPrice(dto.getCostPrice());
        item.setTaxRate(dto.getTaxRate());
        item.setHasVariants(dto.getHasVariants());
        item.setTrackInventory(dto.getTrackInventory());
        item.setIsActive(dto.getIsActive());
        return item;
    }

    private ItemDto toDto(Item i) {
        ItemDto dto = new ItemDto();
        dto.setId(i.getId());
        dto.setName(i.getName());
        dto.setType(i.getType());
        dto.setCategoryId(i.getCategory() != null ? i.getCategory().getId() : null);
        dto.setCategoryName(i.getCategory() != null ? i.getCategory().getName() : null);
        dto.setUnitId(i.getUnit() != null ? i.getUnit().getId() : null);
        dto.setUnitName(i.getUnit() != null ? i.getUnit().getName() : null);
        dto.setDescription(i.getDescription());
        dto.setBarcode(i.getBarcode());
        dto.setSellingPrice(i.getSellingPrice());
        dto.setCostPrice(i.getCostPrice());
        dto.setTaxRate(i.getTaxRate());
        dto.setHasVariants(i.getHasVariants());
        dto.setTrackInventory(i.getTrackInventory());
        dto.setIsActive(i.getIsActive());
        return dto;
    }
}