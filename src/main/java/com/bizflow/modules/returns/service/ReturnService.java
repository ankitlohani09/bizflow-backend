package com.bizflow.modules.returns.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.returns.dto.ReturnDto;

import java.util.List;

public interface ReturnService {
    ApiResponse<List<ReturnDto>> getAll();

    ApiResponse<ReturnDto> getById(Long id);

    ApiResponse<ReturnDto> create(ReturnDto dto);
}