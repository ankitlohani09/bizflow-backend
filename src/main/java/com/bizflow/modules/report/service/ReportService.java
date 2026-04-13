package com.bizflow.modules.report.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.report.enums.ReportGroupBy;

import java.time.LocalDate;
import java.util.Map;

public interface ReportService {
    ApiResponse<Map<String, Object>> getDashboard(LocalDate fromDate, LocalDate toDate);

    ApiResponse<Map<String, Object>> getSalesReport(LocalDate fromDate, LocalDate toDate, ReportGroupBy groupBy);

    ApiResponse<Map<String, Object>> getProfitLossReport(LocalDate fromDate, LocalDate toDate);
}
