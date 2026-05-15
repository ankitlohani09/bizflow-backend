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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        try {
            String q = request.getQuery() == null ? "" : request.getQuery().toLowerCase();
            AiQueryType type = request.getType();

            // Auto-detect query type from keywords
            if (type == null || type == AiQueryType.GENERAL) {
                if (q.contains("sale") || q.contains("revenue") || q.contains("profit") || q.contains("performance")
                        || q.contains("sold")) {
                    type = AiQueryType.SALES;
                } else if (q.contains("stock") || q.contains("product") || q.contains("inventory") || q.contains("item")
                        || q.contains("damaged")) {
                    type = AiQueryType.INVENTORY;
                } else if (q.contains("expense") || q.contains("spent") || q.contains("cost") || q.contains("bill")) {
                    type = AiQueryType.EXPENSE;
                } else if (q.contains("staff") || q.contains("employee") || q.contains("attendance")
                        || q.contains("salary")) {
                    type = AiQueryType.STAFF;
                } else {
                    type = AiQueryType.GENERAL;
                }
            }

            DateRange range = resolveRange(request.getFromDate(), request.getToDate());
            Long tenantId = SecurityUtils.getCurrentTenantId();

            String contextSummary = "";
            Object chartData = null;

            switch (type) {
            case SALES -> {
                Map<String, Object> sales = reportService.getSalesReport(range.from, range.to, ReportGroupBy.DAY)
                        .getData();
                Map<String, Object> dashboard = reportService.getDashboard(range.from, range.to).getData();

                BigDecimal totalSales = decimal(sales.get("totalSales"));
                int totalInvoices = intValue(sales.get("totalInvoices"));
                List<?> topItems = listValue(dashboard.get("topSellingItems"));

                String topItemName = "N/A";
                if (!topItems.isEmpty()) {
                    Object firstItem = topItems.get(0);
                    if (firstItem instanceof Map<?, ?> map) {
                        Object nameObj = map.get("itemName");
                        if (nameObj == null)
                            nameObj = map.get("name");
                        if (nameObj != null)
                            topItemName = String.valueOf(nameObj);
                    }
                }

                contextSummary = "Sales from " + range.from + " to " + range.to + ": " + totalInvoices
                        + " invoices, total sales amount ₹" + scale2(totalSales) + ". Top selling item is "
                        + topItemName + ".";
                chartData = sales.get("records");
            }
            case INVENTORY -> {
                List<Inventory> inventoryList = inventoryRepository.findAllByTenantId(tenantId);

                if (q.contains("damaged")) {
                    BigDecimal damagedUnits = inventoryList.stream().map(i -> safe(i.getDamagedQty()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long damagedSkus = inventoryList.stream()
                            .filter(i -> safe(i.getDamagedQty()).compareTo(BigDecimal.ZERO) > 0).count();

                    List<String> damagedItemNames = inventoryList.stream()
                            .filter(i -> safe(i.getDamagedQty()).compareTo(BigDecimal.ZERO) > 0)
                            .map(i -> i.getItem().getName()).collect(java.util.stream.Collectors.toList());

                    contextSummary = "Inventory status for damaged items: " + damagedSkus
                            + " SKUs have damaged units. The damaged items are: " + String.join(", ", damagedItemNames)
                            + ". Total damaged quantity is " + damagedUnits.stripTrailingZeros().toPlainString() + ".";

                    Map<String, Object> snapshot = new LinkedHashMap<>();
                    snapshot.put("damagedSkus", damagedSkus);
                    snapshot.put("damagedQty", scale3(damagedUnits));
                    chartData = snapshot;

                } else if (q.contains("expired") || q.contains("expiry")) {
                    BigDecimal expiredUnits = inventoryList.stream().map(i -> safe(i.getExpiredQty()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long expiredSkus = inventoryList.stream()
                            .filter(i -> safe(i.getExpiredQty()).compareTo(BigDecimal.ZERO) > 0).count();

                    List<String> expiredItemNames = inventoryList.stream()
                            .filter(i -> safe(i.getExpiredQty()).compareTo(BigDecimal.ZERO) > 0)
                            .map(i -> i.getItem().getName()).collect(java.util.stream.Collectors.toList());

                    if (expiredSkus == 0) {
                        contextSummary = "No products have any expired quantity in inventory right now.";
                    } else {
                        contextSummary = "Inventory status for expired items: " + expiredSkus
                                + " SKUs have expired units. The expired items are: "
                                + String.join(", ", expiredItemNames) + ". Total expired quantity is "
                                + expiredUnits.stripTrailingZeros().toPlainString() + ".";
                    }

                    Map<String, Object> snapshot = new LinkedHashMap<>();
                    snapshot.put("expiredSkus", expiredSkus);
                    snapshot.put("expiredQty", scale3(expiredUnits));
                    chartData = snapshot;

                } else {
                    long lowStock = inventoryList.stream().filter(this::isLowStock).count();
                    BigDecimal availableUnits = inventoryList.stream().map(i -> safe(i.getAvailableQty()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    List<String> availableItemDetails = inventoryList.stream()
                            .filter(i -> safe(i.getAvailableQty()).compareTo(BigDecimal.ZERO) > 0)
                            .map(i -> i.getItem().getName() + " ("
                                    + safe(i.getAvailableQty()).stripTrailingZeros().toPlainString() + " units)")
                            .limit(20).collect(java.util.stream.Collectors.toList());

                    contextSummary = "Inventory summary: total " + inventoryList.size()
                            + " tracked SKUs, low-stock items " + lowStock
                            + ", total available quantity across all items is "
                            + availableUnits.stripTrailingZeros().toPlainString() + ".";

                    if (!availableItemDetails.isEmpty()) {
                        contextSummary += " Products in stock: " + String.join(", ", availableItemDetails);
                        if (inventoryList.stream().filter(i -> safe(i.getAvailableQty()).compareTo(BigDecimal.ZERO) > 0)
                                .count() > 20) {
                            contextSummary += " ... and others.";
                        }
                        contextSummary += ".";
                    }

                    Map<String, Object> generalSnapshot = new LinkedHashMap<>();
                    generalSnapshot.put("totalSkus", inventoryList.size());
                    generalSnapshot.put("lowStockItems", lowStock);
                    generalSnapshot.put("availableQty", scale3(availableUnits));
                    chartData = generalSnapshot;
                }
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

                String topCategory = byCategory.entrySet().stream().max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey).orElse("N/A");

                contextSummary = "Expenses from " + range.from + " to " + range.to + ": total expenditure ₹"
                        + scale2(totalExpense) + ", highest expense category is " + topCategory + ".";
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

                contextSummary = "Staff summary: active staff " + activeStaff + "/" + totalStaff
                        + ", attendance rate is " + scale2(attendanceRate) + "%, total advances paid to staff is ₹"
                        + scale2(advances) + ".";

                Map<String, Object> staffSnapshot = new LinkedHashMap<>();
                staffSnapshot.put("totalStaff", totalStaff);
                staffSnapshot.put("activeStaff", activeStaff);
                staffSnapshot.put("attendanceRate", scale2(attendanceRate));
                staffSnapshot.put("advances", scale2(advances));
                chartData = staffSnapshot;
            }
            case GENERAL -> {
                Map<String, Object> dashboard = reportService.getDashboard(range.from, range.to).getData();
                contextSummary = "Business snapshot from " + range.from + " to " + range.to + ": today's sales ₹"
                        + dashboard.get("todaySales") + ", today's expenses ₹" + dashboard.get("todayExpenses")
                        + ", today's profit ₹" + dashboard.get("todayProfit") + ".";
                chartData = dashboard.get("salesChart");
            }
            }

            // Call local Ollama for smart response
            String answer = callOllama(request.getQuery(), contextSummary, type.name());

            AiQueryResponse response = new AiQueryResponse();
            response.setQuery(request.getQuery());
            response.setAnswer(answer);
            response.setQueryType(type.name());
            response.setChartData(chartData);
            response.setCreatedAt(LocalDateTime.now());

            aiLogService.log(request.getQuery(), answer, type.name(), estimateTokens(answer));
            return ApiResponse.success(MessageConstant.SUCCESS, response);

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR in query method: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private String callOllama(String query, String context, String type) {
        String url = "http://127.0.0.1:11434/api/generate";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("model", "phi3:latest");

        String prompt = "You are BizFlow AI, a smart business assistant. Answer the user's question based on the provided context.\n\n"
                + "Context: " + context + "\n\n" + "User Question: " + query + "\n\n"
                + "Answer in a very simple, clear, and direct way. Avoid complex business jargon and long explanations. Keep it short and easy to understand. "
                + "Use markdown bolding (e.g., **text**) to highlight key information like product names, quantities, and amounts. "
                + "If you need to mention any prices or money, use Indian Rupees (₹), never use dollars ($). "
                + "Do not talk about currency if the question is only about quantities. "
                + "If the context does not contain enough info, state what is available simply.";

        request.put("prompt", prompt);
        request.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response != null && response.containsKey("response")) {
                String answer = String.valueOf(response.get("response"));

                // Post-processing: remove unnecessary currency/price disclaimers
                // when the user's question is not about money
                String lowerQuery = query.toLowerCase();
                boolean isMoneyQuery = lowerQuery.contains("price") || lowerQuery.contains("cost")
                        || lowerQuery.contains("money") || lowerQuery.contains("rupees") || lowerQuery.contains("₹")
                        || lowerQuery.contains("expense") || lowerQuery.contains("sales")
                        || lowerQuery.contains("revenue");

                if (!isMoneyQuery) {
                    String[] sentences = answer.split("(?<=[.!?])\\s+");
                    StringBuilder cleanAnswer = new StringBuilder();
                    for (String sentence : sentences) {
                        String lowerSentence = sentence.toLowerCase();
                        if (!lowerSentence.contains("price") && !lowerSentence.contains("currency")
                                && !lowerSentence.contains("monetary") && !lowerSentence.contains("rupees")
                                && !lowerSentence.contains("₹") && !lowerSentence.contains("not requested")) {
                            cleanAnswer.append(sentence).append(" ");
                        }
                    }
                    return cleanAnswer.toString().trim();
                }

                return answer;
            }
        } catch (Exception e) {
            System.err.println("Failed to call Ollama: " + e.getMessage());
            return "Local AI is currently unavailable, but here is the data: " + context;
        }
        return "Local AI returned no response, but here is the data: " + context;
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
            if (!(recordObj instanceof Map<?, ?> raw))
                continue;

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

    // ─── Helper Methods ───────────────────────────────────────────────────────────

    private DateRange resolveRange(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate effectiveFrom = from == null ? today.withDayOfMonth(1) : from;
        LocalDate effectiveTo = to == null ? today : to;
        if (effectiveFrom.isAfter(effectiveTo))
            effectiveFrom = effectiveTo;
        return new DateRange(effectiveFrom, effectiveTo);
    }

    private boolean isLowStock(Inventory inventory) {
        if (inventory.getLowStockThreshold() == null)
            return false;
        return safe(inventory.getAvailableQty()).compareTo(inventory.getLowStockThreshold()) <= 0;
    }

    private int estimateTokens(String text) {
        if (text == null || text.isBlank())
            return 0;
        return Math.max(1, text.length() / 4);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal bd)
            return bd;
        if (value instanceof Number n)
            return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    private int intValue(Object value) {
        if (value instanceof Integer i)
            return i;
        if (value instanceof Number n)
            return n.intValue();
        return 0;
    }

    private List<?> listValue(Object value) {
        if (value instanceof List<?> list)
            return list;
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
