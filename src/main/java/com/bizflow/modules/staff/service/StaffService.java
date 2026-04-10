package com.bizflow.modules.staff.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.staff.dto.StaffDto;

import java.util.List;

public interface StaffService {
    ApiResponse<List<StaffDto>> getAll();

    ApiResponse<StaffDto> getById(Long id);

    ApiResponse<StaffDto> create(StaffDto dto);

    ApiResponse<StaffDto> update(Long id, StaffDto dto);

    ApiResponse<Void> delete(Long id);
}