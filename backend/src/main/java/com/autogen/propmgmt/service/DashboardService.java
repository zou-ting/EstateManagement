package com.autogen.propmgmt.service;

import com.autogen.propmgmt.dto.ChartItem;
import com.autogen.propmgmt.dto.DashboardStats;
import com.autogen.propmgmt.entity.*;
import com.autogen.propmgmt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PropBuildingRepository buildingRepository;
    private final PropUnitRepository unitRepository;
    private final OwnerRepository ownerRepository;
    private final RepairRequestRepository repairRequestRepository;
    private final PropertyFeeRepository propertyFeeRepository;
    private final VisitorRecordRepository visitorRecordRepository;
    private final UserRepository userRepository;

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        stats.setBuildingCount(buildingRepository.count());
        stats.setUnitCount(unitRepository.count());
//        stats.setOwnerCount(ownerRepository.count());
        stats.setOwnerCount(ownerRepository.countByStatus(1));
        stats.setRepairCount(repairRequestRepository.count());
        stats.setUnpaidFeeCount(propertyFeeRepository.findAll().stream()
                .filter(f -> "UNPAID".equals(f.getPayStatus())).count());
        stats.setPendingVisitorCount(visitorRecordRepository.findAll().stream()
                .filter(v -> "PENDING".equals(v.getStatus())).count());
        stats.setBuildingDistribution(buildBuildingDistribution());
        stats.setRepairStatusDistribution(buildRepairStatusDistribution());
        stats.setUnitStatusDistribution(buildUnitStatusDistribution());
        stats.setFeePayStatusDistribution(buildFeePayStatusDistribution());
        stats.setFeeTrend(buildFeeTrend());
        stats.setRepairTypeDistribution(buildRepairTypeDistribution());
        stats.setUserRoleDistribution(buildUserRoleDistribution());
        stats.setFeeMonthDistribution(buildFeeMonthDistribution());
        return stats;
    }

    private List<ChartItem> buildBuildingDistribution() {
        Map<Long, String> names = buildingRepository.findAll().stream()
                .collect(Collectors.toMap(PropBuilding::getId, PropBuilding::getBuildingName));
        Map<String, Long> map = new LinkedHashMap<>();
        for (PropUnit unit : unitRepository.findAll()) {
            String name = names.getOrDefault(unit.getBuildingId(), "未知楼栋");
            map.merge(name, 1L, Long::sum);
        }
        return map.entrySet().stream()
                .map(e -> new ChartItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ChartItem> buildRepairStatusDistribution() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (RepairRequest req : repairRequestRepository.findAll()) {
            String status = StringUtils.hasText(req.getStatus()) ? req.getStatus() : "未知";
            map.merge(status, 1L, Long::sum);
        }
        return map.entrySet().stream()
                .map(e -> new ChartItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ChartItem> buildUnitStatusDistribution() {
        long occupied = unitRepository.findAll().stream().filter(u -> u.getStatus() != null && u.getStatus() == 1).count();
        long vacant = unitRepository.findAll().stream().filter(u -> u.getStatus() != null && u.getStatus() == 2).count();
        long decorating = unitRepository.findAll().stream().filter(u -> u.getStatus() != null && u.getStatus() == 0).count();
        return List.of(
                new ChartItem("已入住", occupied),
                new ChartItem("空置", vacant),
                new ChartItem("装修中", decorating)
        );
    }

    private List<ChartItem> buildFeePayStatusDistribution() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (PropertyFee fee : propertyFeeRepository.findAll()) {
            String status = StringUtils.hasText(fee.getPayStatus()) ? fee.getPayStatus() : "未知";
            map.merge(status, 1L, Long::sum);
        }
        return map.entrySet().stream()
                .map(e -> new ChartItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    // 物业费趋势 - 按月份统计（柱状图）
    private List<ChartItem> buildFeeTrend() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (PropertyFee fee : propertyFeeRepository.findAll()) {
            String month = fee.getBillMonth();
            if (month != null && month.length() >= 7) {
                Double amount = fee.getTotalFee() != null ? fee.getTotalFee().doubleValue() : 0.0;
                map.merge(month, amount, Double::sum);
            }
        }
        return map.entrySet().stream()
                .map(e -> new ChartItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    // 报修类型分布 - 按报修类型统计（饼图）
    private List<ChartItem> buildRepairTypeDistribution() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (RepairRequest req : repairRequestRepository.findAll()) {
            String type = classifyRepairType(req.getTitle());
            map.merge(type, 1L, Long::sum);
        }
        return map.entrySet().stream()
                .map(e -> new ChartItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    // 分类方法
    private String classifyRepairType(String title) {
        if (title == null) return "其他";
        if (title.contains("水") || title.contains("漏") || title.contains("管")) return "水电维修";
        if (title.contains("电") || title.contains("灯") || title.contains("空调") ||
                title.contains("网络") || title.contains("电器") || title.contains("制冷")) return "电器维修";
        if (title.contains("门") || title.contains("窗") || title.contains("锁")) return "门窗维修";
        if (title.contains("墙") || title.contains("漆") || title.contains("渗")) return "房屋修缮";
        return "其他";
    }

    // 用户角色分布（饼图）
    private List<ChartItem> buildUserRoleDistribution() {
        Map<String, Long> map = new LinkedHashMap<>();

        // 获取所有在册业主的 user_id
        List<Long> activeOwnerUserIds = ownerRepository.findAll().stream()
                .filter(o -> o.getStatus() != null && o.getStatus() == 1)
                .map(Owner::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<User> users = userRepository.findAll();
        for (User user : users) {
            // 只统计：role=OWNER，status=1，且关联了在册业主
            if ("OWNER".equals(user.getRole())
                    && user.getStatus() == 1
                    && activeOwnerUserIds.contains(user.getId())) {
                map.merge("业主", 1L, Long::sum);
            } else if (user.getStatus() == 1 && StringUtils.hasText(user.getRole())) {
                String role = user.getRole();
                if ("ADMIN".equals(role)) {
                    map.merge("管理员", 1L, Long::sum);
                } else if ("PROPERTY_MANAGER".equals(role)) {
                    map.merge("物业经理", 1L, Long::sum);
                }
            }
        }
        return map.entrySet().stream()
                .map(e -> new ChartItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    // 账单月份分布（柱状图）
    private List<ChartItem> buildFeeMonthDistribution() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (PropertyFee fee : propertyFeeRepository.findAll()) {
            String month = fee.getBillMonth();
            if (month != null && month.length() >= 7) {
                map.merge(month, 1L, Long::sum);
            }
        }
        return map.entrySet().stream()
                .map(e -> new ChartItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}