package com.bizflow.modules.logs.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.logs.dto.AiLogDto;
import com.bizflow.modules.logs.service.AiLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI Logs")
@RestController
@RequestMapping("/ai-logs")
@RequiredArgsConstructor
public class AiLogController {

    private final AiLogService aiLogService;

    @Operation(summary = "Get all AI logs")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AiLogDto>>> getAll() {
        return ResponseEntity.ok(aiLogService.getAll());
    }

    @Operation(summary = "Get AI logs by user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AiLogDto>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(aiLogService.getByUser(userId));
    }
}