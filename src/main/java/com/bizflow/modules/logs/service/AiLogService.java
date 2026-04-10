package com.bizflow.modules.logs.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.logs.dto.AiLogDto;

import java.util.List;

public interface AiLogService {
    ApiResponse<List<AiLogDto>> getAll();

    ApiResponse<List<AiLogDto>> getByUser(Long userId);

    void log(String prompt, String response, String module, Integer tokensUsed);
}