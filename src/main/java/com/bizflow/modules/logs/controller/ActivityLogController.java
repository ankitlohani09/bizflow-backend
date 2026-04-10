package com.bizflow.modules.logs.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.logs.dto.ActivityLogDto;
import com.bizflow.modules.logs.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Activity Logs")
@RestController
@RequestMapping("/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @Operation(summary = "Get all activity logs")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityLogDto>>> getAll() {
        return ResponseEntity.ok(activityLogService.getAll());
    }

    @Operation(summary = "Get logs by user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ActivityLogDto>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(activityLogService.getByUser(userId));
    }

    @Operation(summary = "Get logs by entity")
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<ActivityLogDto>>> getByEntity(@PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(activityLogService.getByEntity(entityType, entityId));
    }
}