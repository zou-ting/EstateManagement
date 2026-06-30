package com.autogen.propmgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long buildingCount;
    private long unitCount;
    private long ownerCount;
    private long repairCount;
    private long unpaidFeeCount;
    private long pendingVisitorCount;
    private List<ChartItem> buildingDistribution = new ArrayList<>();
    private List<ChartItem> repairStatusDistribution = new ArrayList<>();
    private List<ChartItem> unitStatusDistribution = new ArrayList<>();
    private List<ChartItem> feePayStatusDistribution = new ArrayList<>();
    // 新增字段
    private List<ChartItem> feeTrend;          // 物业费趋势（柱状图）
    private List<ChartItem> repairTypeDistribution; // 报修类型分布（饼图）
    private List<ChartItem> userRoleDistribution;   // 用户角色分布（饼图）
    private List<ChartItem> feeMonthDistribution;   // 账单月份分布（柱状图）
}
