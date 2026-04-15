package com.bizflow.modules.report.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.report.enums.ReportGroupBy;
import com.bizflow.modules.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "Reports", description = "Business analytics and reporting")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Get dashboard summary")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(reportService.getDashboard(fromDate, toDate));
    }

    @Operation(summary = "Get sales report")
    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "DAY") ReportGroupBy groupBy) {
        return ResponseEntity.ok(reportService.getSalesReport(fromDate, toDate, groupBy));
    }

    @Operation(summary = "Get profit and loss report")
    @GetMapping("/profit-loss")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfitLossReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(reportService.getProfitLossReport(fromDate, toDate));
    }
}