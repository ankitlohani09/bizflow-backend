package com.bizflow.modules.report.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.enums.InvoiceType;
import com.bizflow.common.enums.PaymentStatus;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.modules.billing.entity.Invoice;
import com.bizflow.modules.billing.entity.InvoiceItem;
import com.bizflow.modules.billing.entity.Payment;
import com.bizflow.modules.billing.repository.InvoiceItemRepository;
import com.bizflow.modules.billing.repository.InvoiceRepository;
import com.bizflow.modules.billing.repository.PaymentRepository;
import com.bizflow.modules.expense.entity.Expense;
import com.bizflow.modules.expense.repository.ExpenseRepository;
import com.bizflow.modules.inventory.entity.Inventory;
import com.bizflow.modules.inventory.repository.InventoryRepository;
import com.bizflow.modules.purchase.entity.PurchaseItem;
import com.bizflow.modules.purchase.repository.PurchaseItemRepository;
import com.bizflow.modules.report.enums.ReportGroupBy;
import com.bizflow.modules.report.service.ReportService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final InventoryRepository inventoryRepository;
    private final PurchaseItemRepository purchaseItemRepository;

    @Override
    public ApiResponse<Map<String, Object>> getDashboard(LocalDate fromDate, LocalDate toDate) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        List<Invoice> salesInvoices;
        List<Expense> expenses;
        DateRange range;

        if (fromDate == null && toDate == null) {
            salesInvoices = invoiceRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                    .filter(i -> i.getInvoiceType() == InvoiceType.SALE)
                    .filter(i -> i.getPaymentStatus() != PaymentStatus.CANCELLED).toList();
            expenses = expenseRepository.findAllByTenantIdOrderByExpenseDateDesc(tenantId);
            
            LocalDate minInvoiceDate = salesInvoices.stream().map(i -> i.getCreatedAt().toLocalDate()).min(LocalDate::compareTo).orElse(LocalDate.now());
            LocalDate minExpenseDate = expenses.stream().map(Expense::getExpenseDate).min(LocalDate::compareTo).orElse(LocalDate.now());
            LocalDate minDate = minInvoiceDate.isBefore(minExpenseDate) ? minInvoiceDate : minExpenseDate;
            LocalDate maxDate = LocalDate.now();
            
            range = new DateRange(minDate, maxDate, minDate.atStartOfDay(), maxDate.atTime(LocalTime.MAX));
        } else {
            range = resolveRange(fromDate, toDate, false);
            salesInvoices = findSalesInvoices(tenantId, range);
            expenses = expenseRepository.findAllByTenantIdAndExpenseDateBetween(tenantId, range.from, range.to);
        }

        BigDecimal totalSales = sumInvoices(salesInvoices);
        BigDecimal totalExpenses = sumExpenses(expenses);
        BigDecimal totalProfit = totalSales.subtract(totalExpenses);

        BigDecimal pendingPayments = salesInvoices.stream()
                .filter(i -> i.getPaymentStatus() == PaymentStatus.UNPAID
                        || i.getPaymentStatus() == PaymentStatus.PARTIAL)
                .map(i -> safe(i.getGrandTotal()).subtract(safe(i.getPaidAmount())))
                .filter(v -> v.compareTo(BigDecimal.ZERO) > 0).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Inventory> inventoryList = inventoryRepository.findAllByTenantId(tenantId);
        long lowStockItems = inventoryList.stream().filter(this::isLowStock).count();

        BigDecimal totalInventoryValue = inventoryList.stream().map(inv -> {
            BigDecimal qty = safe(inv.getAvailableQty());
            BigDecimal costPrice = BigDecimal.ZERO;
            if (inv.getVariant() != null && inv.getVariant().getCostPrice() != null) {
                costPrice = inv.getVariant().getCostPrice();
            } else if (inv.getItem() != null && inv.getItem().getCostPrice() != null) {
                costPrice = inv.getItem().getCostPrice();
            }
            return qty.multiply(costPrice);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> topSellingItems = computeTopSellingItems(tenantId, salesInvoices, 5);
        List<Map<String, Object>> salesChart = buildDashboardChart(range, salesInvoices, expenses);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("todaySales", scale2(totalSales));
        data.put("todayExpenses", scale2(totalExpenses));
        data.put("todayProfit", scale2(totalProfit));
        data.put("totalInvoices", salesInvoices.size());
        data.put("pendingPayments", scale2(pendingPayments));
        data.put("lowStockItems", lowStockItems);
        data.put("totalItems", inventoryList.size());
        data.put("totalInventoryValue", scale2(totalInventoryValue));
        data.put("topSellingItems", topSellingItems);
        data.put("salesChart", salesChart);

        return ApiResponse.success(MessageConstant.SUCCESS, data);
    }

    @Override
    public ApiResponse<Map<String, Object>> getSalesReport(LocalDate fromDate, LocalDate toDate,
            ReportGroupBy groupBy) {
        DateRange range = resolveRange(fromDate, toDate, true);
        ReportGroupBy safeGroupBy = groupBy == null ? ReportGroupBy.DAY : groupBy;
        Long tenantId = SecurityUtils.getCurrentTenantId();

        List<Invoice> salesInvoices = findSalesInvoices(tenantId, range);
        Set<Long> invoiceIds = salesInvoices.stream().map(Invoice::getId).collect(java.util.stream.Collectors.toSet());
        List<Payment> payments = invoiceIds.isEmpty() ? List.of()
                : paymentRepository.findAllByTenantIdAndInvoiceIdIn(tenantId, invoiceIds);

        BigDecimal totalSales = sumInvoices(salesInvoices);
        BigDecimal totalDiscount = salesInvoices.stream().map(i -> safe(i.getDiscountAmount())).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        BigDecimal totalTax = salesInvoices.stream().map(i -> safe(i.getTaxAmount())).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        BigDecimal cashSales = sumByMode(payments, "CASH");
        BigDecimal upiSales = sumByMode(payments, "UPI");
        BigDecimal cardSales = sumByMode(payments, "CARD");
        BigDecimal creditSales = sumByMode(payments, "CREDIT");

        Map<String, SalesBucket> grouped = new TreeMap<>();
        for (Invoice invoice : salesInvoices) {
            String key = groupKey(invoice.getCreatedAt().toLocalDate(), safeGroupBy);
            SalesBucket bucket = grouped.computeIfAbsent(key, k -> new SalesBucket());
            bucket.totalSales = bucket.totalSales.add(safe(invoice.getGrandTotal()));
            bucket.totalInvoices++;
        }

        List<Map<String, Object>> records = grouped.entrySet().stream().map(entry -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", entry.getKey());
            row.put("totalSales", scale2(entry.getValue().totalSales));
            row.put("totalInvoices", entry.getValue().totalInvoices);
            return row;
        }).toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalSales", scale2(totalSales));
        data.put("totalInvoices", salesInvoices.size());
        data.put("totalDiscount", scale2(totalDiscount));
        data.put("totalTax", scale2(totalTax));
        data.put("cashSales", scale2(cashSales));
        data.put("upiSales", scale2(upiSales));
        data.put("cardSales", scale2(cardSales));
        data.put("creditSales", scale2(creditSales));
        data.put("records", records);

        return ApiResponse.success(MessageConstant.SUCCESS, data);
    }

    @Override
    public ApiResponse<Map<String, Object>> getProfitLossReport(LocalDate fromDate, LocalDate toDate) {
        DateRange range = resolveRange(fromDate, toDate, true);
        Long tenantId = SecurityUtils.getCurrentTenantId();

        List<Invoice> salesInvoices = findSalesInvoices(tenantId, range);
        List<PurchaseItem> purchaseItems = purchaseItemRepository
                .findAllByTenantIdAndPurchasePurchaseDateBetween(tenantId, range.from, range.to);
        List<Expense> expenses = expenseRepository.findAllByTenantIdAndExpenseDateBetween(tenantId, range.from,
                range.to);

        BigDecimal totalRevenue = sumInvoices(salesInvoices);
        BigDecimal totalCogs = purchaseItems.stream().map(pi -> safe(pi.getLineTotal())).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        BigDecimal totalExpenses = sumExpenses(expenses);

        BigDecimal grossProfit = totalRevenue.subtract(totalCogs);
        BigDecimal netProfit = grossProfit.subtract(totalExpenses);
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : netProfit.multiply(BigDecimal.valueOf(100)).divide(totalRevenue, 2, RoundingMode.HALF_UP);

        Map<LocalDate, BigDecimal> revenueByDate = new HashMap<>();
        for (Invoice invoice : salesInvoices) {
            LocalDate date = invoice.getCreatedAt().toLocalDate();
            revenueByDate.merge(date, safe(invoice.getGrandTotal()), BigDecimal::add);
        }

        Map<LocalDate, BigDecimal> cogsByDate = new HashMap<>();
        for (PurchaseItem item : purchaseItems) {
            LocalDate date = item.getPurchase().getPurchaseDate();
            cogsByDate.merge(date, safe(item.getLineTotal()), BigDecimal::add);
        }

        Map<LocalDate, BigDecimal> expenseByDate = new HashMap<>();
        for (Expense expense : expenses) {
            expenseByDate.merge(expense.getExpenseDate(), safe(expense.getAmount()), BigDecimal::add);
        }

        List<Map<String, Object>> breakdown = new ArrayList<>();
        for (LocalDate date = range.from; !date.isAfter(range.to); date = date.plusDays(1)) {
            BigDecimal revenue = revenueByDate.getOrDefault(date, BigDecimal.ZERO);
            BigDecimal cogs = cogsByDate.getOrDefault(date, BigDecimal.ZERO);
            BigDecimal exp = expenseByDate.getOrDefault(date, BigDecimal.ZERO);
            BigDecimal dayNet = revenue.subtract(cogs).subtract(exp);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", date.toString());
            row.put("revenue", scale2(revenue));
            row.put("cogs", scale2(cogs));
            row.put("expenses", scale2(exp));
            row.put("netProfit", scale2(dayNet));
            breakdown.add(row);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalRevenue", scale2(totalRevenue));
        data.put("totalCOGS", scale2(totalCogs));
        data.put("grossProfit", scale2(grossProfit));
        data.put("totalExpenses", scale2(totalExpenses));
        data.put("netProfit", scale2(netProfit));
        data.put("profitMargin", scale2(profitMargin));
        data.put("breakdown", breakdown);

        return ApiResponse.success(MessageConstant.SUCCESS, data);
    }

    private List<Invoice> findSalesInvoices(Long tenantId, DateRange range) {
        return invoiceRepository
                .findAllByTenantIdAndCreatedAtBetweenOrderByCreatedAtAsc(tenantId, range.fromDateTime, range.toDateTime)
                .stream().filter(i -> i.getInvoiceType() == InvoiceType.SALE)
                .filter(i -> i.getPaymentStatus() != PaymentStatus.CANCELLED).toList();
    }

    private List<Map<String, Object>> computeTopSellingItems(Long tenantId, List<Invoice> salesInvoices, int limit) {
        Set<Long> invoiceIds = salesInvoices.stream().map(Invoice::getId).collect(java.util.stream.Collectors.toSet());
        if (invoiceIds.isEmpty()) {
            return List.of();
        }

        List<InvoiceItem> items = invoiceItemRepository.findAllByTenantIdAndInvoiceIdIn(tenantId, invoiceIds);
        Map<Long, TopItemBucket> map = new HashMap<>();

        for (InvoiceItem item : items) {
            Long key = item.getItem().getId();
            TopItemBucket bucket = map.computeIfAbsent(key, k -> new TopItemBucket(item.getItem().getName()));
            bucket.quantity = bucket.quantity.add(safe(item.getQuantity()));
            bucket.revenue = bucket.revenue.add(safe(item.getSubtotal()));
        }

        return map.entrySet().stream().sorted((a, b) -> b.getValue().quantity.compareTo(a.getValue().quantity))
                .limit(limit).map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("itemId", entry.getKey());
                    row.put("itemName", entry.getValue().itemName);
                    row.put("totalQuantity", scale3(entry.getValue().quantity));
                    row.put("totalRevenue", scale2(entry.getValue().revenue));
                    return row;
                }).toList();
    }

    private List<Map<String, Object>> buildDashboardChart(DateRange range, List<Invoice> invoices,
            List<Expense> expenses) {
        Map<LocalDate, BigDecimal> salesByDate = new HashMap<>();
        for (Invoice invoice : invoices) {
            LocalDate date = invoice.getCreatedAt().toLocalDate();
            salesByDate.merge(date, safe(invoice.getGrandTotal()), BigDecimal::add);
        }

        Map<LocalDate, BigDecimal> expenseByDate = new HashMap<>();
        for (Expense expense : expenses) {
            expenseByDate.merge(expense.getExpenseDate(), safe(expense.getAmount()), BigDecimal::add);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        long days = java.time.temporal.ChronoUnit.DAYS.between(range.from, range.to);
        
        if (days <= 90) {
            for (LocalDate date = range.from; !date.isAfter(range.to); date = date.plusDays(1)) {
                BigDecimal sale = salesByDate.getOrDefault(date, BigDecimal.ZERO);
                BigDecimal exp = expenseByDate.getOrDefault(date, BigDecimal.ZERO);
                BigDecimal profit = sale.subtract(exp);

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("date", date.toString());
                row.put("sales", scale2(sale));
                row.put("expenses", scale2(exp));
                row.put("profit", scale2(profit));
                rows.add(row);
            }
        } else {
            Set<LocalDate> allDates = new TreeSet<>(salesByDate.keySet());
            allDates.addAll(expenseByDate.keySet());
            
            for (LocalDate date : allDates) {
                BigDecimal sale = salesByDate.getOrDefault(date, BigDecimal.ZERO);
                BigDecimal exp = expenseByDate.getOrDefault(date, BigDecimal.ZERO);
                BigDecimal profit = sale.subtract(exp);

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("date", date.toString());
                row.put("sales", scale2(sale));
                row.put("expenses", scale2(exp));
                row.put("profit", scale2(profit));
                rows.add(row);
            }
        }
        return rows;
    }

    private String groupKey(LocalDate date, ReportGroupBy groupBy) {
        return switch (groupBy) {
            case DAY -> date.toString();
            case WEEK -> {
                WeekFields wf = WeekFields.of(DayOfWeek.MONDAY, 4);
                int week = date.get(wf.weekOfWeekBasedYear());
                int year = date.get(wf.weekBasedYear());
                yield year + "-W" + String.format("%02d", week);
            }
            case MONTH -> date.getYear() + "-" + String.format("%02d", date.getMonthValue());
        };
    }

    private BigDecimal sumInvoices(List<Invoice> invoices) {
        return invoices.stream().map(i -> safe(i.getGrandTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumExpenses(List<Expense> expenses) {
        return expenses.stream().map(e -> safe(e.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByMode(List<Payment> payments, String mode) {
        return payments.stream().filter(p -> p.getPaymentMode() != null && p.getPaymentMode().getName() != null)
                .filter(p -> mode.equalsIgnoreCase(p.getPaymentMode().getName())).map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isLowStock(Inventory inventory) {
        if (inventory.getLowStockThreshold() == null) {
            return false;
        }
        return safe(inventory.getAvailableQty()).compareTo(inventory.getLowStockThreshold()) <= 0;
    }

    private DateRange resolveRange(LocalDate fromDate, LocalDate toDate, boolean required) {
        if (required && (fromDate == null || toDate == null)) {
            throw new BusinessException("fromDate and toDate are required", HttpStatus.BAD_REQUEST);
        }

        LocalDate from = fromDate;
        LocalDate to = toDate;
        if (from == null && to == null) {
            from = LocalDate.now();
            to = LocalDate.now();
        } else if (from == null) {
            from = to;
        } else if (to == null) {
            to = from;
        }

        if (from.isAfter(to)) {
            throw new BusinessException("fromDate cannot be after toDate", HttpStatus.BAD_REQUEST);
        }

        return new DateRange(from, to, from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale2(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale3(BigDecimal value) {
        return safe(value).setScale(3, RoundingMode.HALF_UP);
    }

    private record DateRange(LocalDate from, LocalDate to, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    }

    private static class TopItemBucket {
        private final String itemName;
        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal revenue = BigDecimal.ZERO;

        private TopItemBucket(String itemName) {
            this.itemName = itemName;
        }
    }

    private static class SalesBucket {
        private BigDecimal totalSales = BigDecimal.ZERO;
        private int totalInvoices = 0;
    }
}
