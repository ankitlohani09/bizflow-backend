package com.bizflow.modules.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantStatsResponse {
    private Long activeUsers;
    private Long maxUsersLimit;
    private Long monthlyInvoices;
    private Double usagePercentage;
}
