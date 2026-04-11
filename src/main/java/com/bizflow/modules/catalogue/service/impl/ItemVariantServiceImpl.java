package com.bizflow.modules.catalogue.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.ResourceNotFoundException;
import com.bizflow.modules.catalogue.dto.ItemVariantDto;
import com.bizflow.modules.catalogue.entity.Item;
import com.bizflow.modules.catalogue.entity.ItemVariant;
import com.bizflow.modules.catalogue.repository.ItemRepository;
import com.bizflow.modules.catalogue.repository.ItemVariantRepository;
import com.bizflow.modules.catalogue.service.ItemVariantService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemVariantServiceImpl implements ItemVariantService {

    private final ItemVariantRepository variantRepository;
    private final ItemRepository itemRepository;

    @Override
    public ApiResponse<List<ItemVariantDto>> getAllByItem(Long itemId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                variantRepository.findAllByItemIdAndTenantId(itemId, tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<ItemVariantDto> getById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        ItemVariant variant = variantRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));
        return ApiResponse.success(toDto(variant));
    }

    @Override
    public ApiResponse<ItemVariantDto> create(ItemVariantDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Item item = itemRepository.findByIdAndTenantId(dto.getItemId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.ITEM_NOT_FOUND));
        ItemVariant variant = ItemVariant.builder().tenantId(tenantId).item(item).variantName(dto.getVariantName())
                .sku(dto.getSku()).barcode(dto.getBarcode()).sellingPrice(dto.getSellingPrice())
                .costPrice(dto.getCostPrice()).isActive(dto.getIsActive()).build();
        return ApiResponse.success(MessageConstant.CREATED, toDto(variantRepository.save(variant)));
    }

    @Override
    public ApiResponse<ItemVariantDto> update(Long id, ItemVariantDto dto) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        ItemVariant variant = variantRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));
        variant.setVariantName(dto.getVariantName());
        variant.setSku(dto.getSku());
        variant.setBarcode(dto.getBarcode());
        variant.setSellingPrice(dto.getSellingPrice());
        variant.setCostPrice(dto.getCostPrice());
        variant.setIsActive(dto.getIsActive());
        return ApiResponse.success(MessageConstant.UPDATED, toDto(variantRepository.save(variant)));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        ItemVariant variant = variantRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.NOT_FOUND));
        variantRepository.delete(variant);
        return ApiResponse.success(MessageConstant.DELETED, null);
    }

    private ItemVariantDto toDto(ItemVariant v) {
        ItemVariantDto dto = new ItemVariantDto();
        dto.setId(v.getId());
        dto.setItemId(v.getItem().getId());
        dto.setItemName(v.getItem().getName());
        dto.setVariantName(v.getVariantName());
        dto.setSku(v.getSku());
        dto.setBarcode(v.getBarcode());
        dto.setSellingPrice(v.getSellingPrice());
        dto.setCostPrice(v.getCostPrice());
        dto.setIsActive(v.getIsActive());
        return dto;
    }
}