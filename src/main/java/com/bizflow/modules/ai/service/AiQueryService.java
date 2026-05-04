package com.bizflow.modules.ai.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.ai.dto.AiQueryRequest;
import com.bizflow.modules.ai.dto.AiQueryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AiQueryService {
    ApiResponse<AiQueryResponse> query(AiQueryRequest request);

    ApiResponse<List<Map<String, Object>>> getReorderSuggestions(LocalDate fromDate, LocalDate toDate);

    ApiResponse<List<Map<String, Object>>> getSeasonalTrends(LocalDate fromDate, LocalDate toDate);
}
