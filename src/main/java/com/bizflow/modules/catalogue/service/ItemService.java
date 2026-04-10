package com.bizflow.modules.catalogue.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.catalogue.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ApiResponse<List<ItemDto>> getAll();

    ApiResponse<ItemDto> getById(Long id);

    ApiResponse<ItemDto> create(ItemDto dto);

    ApiResponse<ItemDto> update(Long id, ItemDto dto);

    ApiResponse<Void> delete(Long id);
}