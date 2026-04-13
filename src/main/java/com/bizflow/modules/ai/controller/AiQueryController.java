package com.bizflow.modules.ai.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.ai.dto.AiQueryRequest;
import com.bizflow.modules.ai.dto.AiQueryResponse;
import com.bizflow.modules.ai.service.AiQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "AI")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiQueryController {

    private final AiQueryService aiQueryService;

    @Operation(summary = "AI business query")
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<AiQueryResponse>> query(@Valid @RequestBody AiQueryRequest request) {
        return ResponseEntity.ok(aiQueryService.query(request));
    }

    @Operation(summary = "Predictive reorder suggestions")
    @GetMapping("/reorder-suggestions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getReorderSuggestions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(aiQueryService.getReorderSuggestions(fromDate, toDate));
    }

    @Operation(summary = "Seasonal sales trend detection")
    @GetMapping("/seasonal-trends")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSeasonalTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(aiQueryService.getSeasonalTrends(fromDate, toDate));
    }
}
