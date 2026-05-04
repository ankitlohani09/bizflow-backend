package com.bizflow.modules.ai.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.enums.AttendanceStatus;
import com.bizflow.modules.ai.dto.AiQueryRequest;
import com.bizflow.modules.ai.dto.AiQueryResponse;
import com.bizflow.modules.ai.enums.AiQueryType;
import com.bizflow.modules.ai.service.AiQueryService;
import com.bizflow.modules.billing.entity.InvoiceItem;
import com.bizflow.modules.billing.repository.InvoiceItemRepository;
import com.bizflow.modules.expense.entity.Expense;
import com.bizflow.modules.expense.repository.ExpenseRepository;
import com.bizflow.modules.inventory.entity.Inventory;
import com.bizflow.modules.inventory.repository.InventoryRepository;
import com.bizflow.modules.logs.service.AiLogService;
import com.bizflow.modules.report.enums.ReportGroupBy;
import com.bizflow.modules.report.service.ReportService;
import com.bizflow.modules.staff.entity.Attendance;
import com.bizflow.modules.staff.repository.AttendanceRepository;
import com.bizflow.modules.staff.repository.StaffAdvanceRepository;
import com.bizflow.modules.staff.repository.StaffRepository;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AiQueryServiceImpl implements AiQueryService {

    private final ReportService reportService;
    private final InventoryRepository inventoryRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final ExpenseRepository expenseRepository;
    private final StaffRepository staffRepository;
    private final AttendanceRepository attendanceRepository;
    private final StaffAdvanceRepository staffAdvanceRepository;
    private final AiLogService aiLogService;

    @Override
    public ApiResponse<AiQueryResponse> query(AiQueryRequest request) {
        AiQueryType type = request.getType() == null ? AiQueryType.GENERAL : request.getType();
        DateRange range = resolveRange(request.getFromDate(), request.getToDate());
        Long tenantId = SecurityUtils.getCurrentTenantId();

        String answer = MessageConstant.SUCCESS;
        Object chartData = null;

        switch (type) {
        case SALES -> {
            Map<String, Object> sales = reportService.getSalesReport(range.from, range.to, ReportGroupBy.DAY).getData();
            Map<String, Object> dashboard = reportService.getDashboard(range.from, range.to).getData();

            BigDecimal totalSales = decimal(sales.get("totalSales"));
            int totalInvoices = intValue(sales.get("totalInvoices"));
            List<?> topItems = listValue(dashboard.get("topSellingItems"));

            answer = "Sales from " + range.from + " to " + range.to + ": " + totalInvoices + " invoices, total "
                    + scale2(totalSales) + ". Top selling item count available: " + topItems.size() + ".";
            chartData = sales.get("records");
        }
        case INVENTORY -> {
            List<Inventory> inventoryList = inventoryRepository.findAllByTenantId(tenantId);
            long lowStock = inventoryList.stream().filter(this::isLowStock).count();
            BigDecimal availableUnits = inventoryList.stream().map(i -> safe(i.getAvailableQty()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            answer = "Inventory summary: " + inventoryList.size() + " tracked SKUs, low-stock items " + lowStock
                    + ", total available quantity " + scale3(availableUnits) + ".";

            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("totalSkus", inventoryList.size());
            snapshot.put("lowStockItems", lowStock);
            snapshot.put("availableQty", scale3(availableUnits));
            chartData = snapshot;
        }
        case EXPENSE -> {
            List<Expense> expenses = expenseRepository.findAllByTenantIdAndExpenseDateBetween(tenantId, range.from,
                    range.to);
            BigDecimal totalExpense = expenses.stream().map(e -> safe(e.getAmount())).reduce(BigDecimal.ZERO,
                    BigDecimal::add);

            Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
            for (Expense expense : expenses) {
                String key = expense.getCategory() == null ? "UNCATEGORIZED" : expense.getCategory().getName();
                byCategory.merge(key, safe(expense.getAmount()), BigDecimal::add);
            }

            String topCategory = byCategory.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                    .orElse("N/A");

            answer = "Expenses from " + range.from + " to " + range.to + ": total " + scale2(totalExpense)
                    + ", top category: " + topCategory + ".";
            chartData = byCategory;
        }
        case STAFF -> {
            int totalStaff = staffRepository.findAllByTenantId(tenantId).size();
            int activeStaff = staffRepository.findAllByTenantIdAndIsActive(tenantId, true).size();

            List<Attendance> attendance = attendanceRepository.findAllByTenantIdAndDateBetween(tenantId, range.from,
                    range.to);
            long presentCount = attendance.stream().filter(
                    a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.HALF_DAY)
                    .count();

            BigDecimal advances = staffAdvanceRepository
                    .findAllByTenantIdAndAdvanceDateBetween(tenantId, range.from, range.to).stream()
                    .map(a -> safe(a.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal attendanceRate = attendance.isEmpty() ? BigDecimal.ZERO
                    : BigDecimal.valueOf(presentCount).multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(attendance.size()), 2, RoundingMode.HALF_UP);

            answer = "Staff summary: active " + activeStaff + "/" + totalStaff + ", attendance rate "
                    + scale2(attendanceRate) + "%, advances paid " + scale2(advances) + ".";

            Map<String, Object> staffSnapshot = new LinkedHashMap<>();
            staffSnapshot.put("totalStaff", totalStaff);
            staffSnapshot.put("activeStaff", activeStaff);
            staffSnapshot.put("attendanceRate", scale2(attendanceRate));
            staffSnapshot.put("advances", scale2(advances));
            chartData = staffSnapshot;
        }
        case GENERAL -> {
            Map<String, Object> dashboard = reportService.getDashboard(range.from, range.to).getData();
            answer = "Business snapshot from " + range.from + " to " + range.to + ": sales "
                    + dashboard.get("todaySales") + ", expenses " + dashboard.get("todayExpenses") + ", profit "
                    + dashboard.get("todayProfit") + ".";
            chartData = dashboard.get("salesChart");
        }
        }

        AiQueryResponse response = new AiQueryResponse();
        response.setQuery(request.getQuery());
        response.setAnswer(answer);
        response.setQueryType(type.name());
        response.setChartData(chartData);
        response.setCreatedAt(LocalDateTime.now());

        aiLogService.log(request.getQuery(), answer, type.name(), estimateTokens(answer));
        return ApiResponse.success(MessageConstant.SUCCESS, response);
    }

    @Override
    public ApiResponse<List<Map<String, Object>>> getReorderSuggestions(LocalDate fromDate, LocalDate toDate) {
        DateRange range = resolveRange(fromDate, toDate);
        Long tenantId = SecurityUtils.getCurrentTenantId();

        List<InvoiceItem> soldItems = invoiceItemRepository.findAllByTenantIdAndInvoiceCreatedAtBetween(tenantId,
                range.from.atStartOfDay(), range.to.atTime(LocalTime.MAX));
        Map<Long, BigDecimal> soldQtyByItem = new HashMap<>();
        for (InvoiceItem soldItem : soldItems) {
            soldQtyByItem.merge(soldItem.getItem().getId(), safe(soldItem.getQuantity()), BigDecimal::add);
        }

        int dayCount = Math.max(1, (int) (range.to.toEpochDay() - range.from.toEpochDay() + 1));
        List<Map<String, Object>> suggestions = new ArrayList<>();

        for (Inventory inventory : inventoryRepository.findAllByTenantId(tenantId)) {
            Long itemId = inventory.getItem().getId();
            BigDecimal soldQty = soldQtyByItem.getOrDefault(itemId, BigDecimal.ZERO);
            BigDecimal avgDailyDemand = soldQty.divide(BigDecimal.valueOf(dayCount), 3, RoundingMode.HALF_UP);
            BigDecimal targetStock = avgDailyDemand.multiply(BigDecimal.valueOf(7)); // one-week target
            BigDecimal availableQty = safe(inventory.getAvailableQty());
            BigDecimal reorderQty = targetStock.subtract(availableQty);

            if (reorderQty.compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("itemId", itemId);
                row.put("itemName", inventory.getItem().getName());
                row.put("availableQty", scale3(availableQty));
                row.put("avgDailyDemand", scale3(avgDailyDemand));
                row.put("suggestedReorderQty", scale3(reorderQty));
                suggestions.add(row);
            }
        }

        aiLogService.log("AI reorder suggestions", "Generated " + suggestions.size() + " reorder suggestions", "SALES",
                estimateTokens("Generated " + suggestions.size() + " reorder suggestions"));
        return ApiResponse.success(MessageConstant.SUCCESS, suggestions);
    }

    @Override
    public ApiResponse<List<Map<String, Object>>> getSeasonalTrends(LocalDate fromDate, LocalDate toDate) {
        DateRange range = resolveRange(fromDate, toDate);
        Map<String, Object> sales = reportService.getSalesReport(range.from, range.to, ReportGroupBy.MONTH).getData();
        List<?> records = listValue(sales.get("records"));

        List<Map<String, Object>> trends = new ArrayList<>();
        BigDecimal prev = null;
        for (Object recordObj : records) {
            if (!(recordObj instanceof Map<?, ?> raw)) {
                continue;
            }
            String period = String.valueOf(raw.get("date"));
            BigDecimal value = decimal(raw.get("totalSales"));
            BigDecimal growthPct = BigDecimal.ZERO;
            if (prev != null && prev.compareTo(BigDecimal.ZERO) > 0) {
                growthPct = value.subtract(prev).multiply(BigDecimal.valueOf(100)).divide(prev, 2,
                        RoundingMode.HALF_UP);
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("period", period);
            row.put("sales", scale2(value));
            row.put("growthPct", scale2(growthPct));
            trends.add(row);
            prev = value;
        }

        aiLogService.log("AI seasonal trends", "Generated " + trends.size() + " trend points", "SALES",
                estimateTokens("Generated " + trends.size() + " trend points"));
        return ApiResponse.success(MessageConstant.SUCCESS, trends);
    }

    private DateRange resolveRange(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate effectiveFrom = from == null ? today.withDayOfMonth(1) : from;
        LocalDate effectiveTo = to == null ? today : to;
        if (effectiveFrom.isAfter(effectiveTo)) {
            effectiveFrom = effectiveTo;
        }
        return new DateRange(effectiveFrom, effectiveTo);
    }

    private boolean isLowStock(Inventory inventory) {
        if (inventory.getLowStockThreshold() == null) {
            return false;
        }
        return safe(inventory.getAvailableQty()).compareTo(inventory.getLowStockThreshold()) <= 0;
    }

    private int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return Math.max(1, text.length() / 4);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private int intValue(Object value) {
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        return 0;
    }

    private List<?> listValue(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        return List.of();
    }

    private BigDecimal scale2(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale3(BigDecimal value) {
        return safe(value).setScale(3, RoundingMode.HALF_UP);
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }
}
