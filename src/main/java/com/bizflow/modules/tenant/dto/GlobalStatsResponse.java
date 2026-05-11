package com.bizflow.modules.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalStatsResponse {
    private Long totalTenants;
    private Long activeTenants;
    private Long trialTenants;
    private Long proTenants;
    private Long enterpriseTenants;
    private Long totalUsers;
    private String systemHealth;
    private Double totalRevenue;
    private java.util.List<GrowthDataPoint> tenantGrowth;
    private java.util.List<GrowthDataPoint> revenueGrowth;
}
