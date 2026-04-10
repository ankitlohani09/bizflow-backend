package com.bizflow.modules.catalogue.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.catalogue.dto.ItemVariantDto;

import java.util.List;

public interface ItemVariantService {
    ApiResponse<List<ItemVariantDto>> getAllByItem(Long itemId);

    ApiResponse<ItemVariantDto> getById(Long id);

    ApiResponse<ItemVariantDto> create(ItemVariantDto dto);

    ApiResponse<ItemVariantDto> update(Long id, ItemVariantDto dto);

    ApiResponse<Void> delete(Long id);
}