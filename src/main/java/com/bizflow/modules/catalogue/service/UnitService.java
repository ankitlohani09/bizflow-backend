package com.bizflow.modules.catalogue.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.catalogue.dto.UnitDto;

import java.util.List;

public interface UnitService {
    ApiResponse<List<UnitDto>> getAll();

    ApiResponse<UnitDto> getById(Long id);

    ApiResponse<UnitDto> create(UnitDto dto);

    ApiResponse<UnitDto> update(Long id, UnitDto dto);

    ApiResponse<Void> delete(Long id);
}