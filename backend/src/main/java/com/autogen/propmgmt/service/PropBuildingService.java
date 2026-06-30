package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.PropBuilding;
import com.autogen.propmgmt.entity.PropUnit;
import com.autogen.propmgmt.repository.PropBuildingRepository;
import com.autogen.propmgmt.repository.PropUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PropBuildingService {

    private final PropBuildingRepository buildingRepository;
    private final PropUnitRepository unitRepository;  // ✅ 新增注入

    // ✅ 支持按类型筛选
    public List<PropBuilding> list(String keyword, String buildingType) {
        if (StringUtils.hasText(keyword)) {
            return buildingRepository.findByBuildingNameContaining(keyword);
        }
        if (StringUtils.hasText(buildingType)) {
            return buildingRepository.findByBuildingType(buildingType);
        }
        return buildingRepository.findAll();
    }

    public PropBuilding getById(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("楼栋不存在"));
    }

    public PropBuilding save(PropBuilding building) {
        if (!StringUtils.hasText(building.getBuildingName())) {
            throw new BusinessException("楼栋名称不能为空");
        }
        if (!StringUtils.hasText(building.getBuildingCode())) {
            throw new BusinessException("楼栋编码不能为空");
        }
        return buildingRepository.save(building);
    }

    public void delete(Long id) {
        // 检查是否有房屋关联
        List<PropUnit> units = unitRepository.findByBuildingId(id);
        if (!units.isEmpty()) {
            PropBuilding building = getById(id);
            throw new BusinessException("楼栋「" + building.getBuildingName() + "」下存在房屋，不能删除");
        }
        buildingRepository.deleteById(id);
    }

    // ✅ 批量删除
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的楼栋");
        }
        for (Long id : ids) {
            List<PropUnit> units = unitRepository.findByBuildingId(id);
            if (!units.isEmpty()) {
                PropBuilding building = getById(id);
                throw new BusinessException("楼栋「" + building.getBuildingName() + "」下存在房屋，不能删除");
            }
        }
        buildingRepository.deleteAllById(ids);
    }

    // ✅ 统计信息
    public Map<String, Object> getStats() {
        List<PropBuilding> all = buildingRepository.findAll();
        long total = all.size();
        long residential = all.stream().filter(b -> "住宅".equals(b.getBuildingType())).count();
        long commercial = all.stream().filter(b -> "商业".equals(b.getBuildingType())).count();
        long mixed = all.stream().filter(b -> "混合".equals(b.getBuildingType())).count();

        long totalUnits = 0;
        long totalOwners = 0;
        for (PropBuilding building : all) {
            List<PropUnit> units = unitRepository.findByBuildingId(building.getId());
            totalUnits += units.size();
            for (PropUnit unit : units) {
                totalOwners += unit.getOwnerCount() != null ? unit.getOwnerCount() : 0;
            }
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("residential", residential);
        stats.put("commercial", commercial);
        stats.put("mixed", mixed);
        stats.put("totalUnits", totalUnits);
        stats.put("totalOwners", totalOwners);
        return stats;
    }
}