package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.Owner;
import com.autogen.propmgmt.entity.PropUnit;
import com.autogen.propmgmt.repository.OwnerRepository;
import com.autogen.propmgmt.repository.PropUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropUnitService {

    private final PropUnitRepository unitRepository;
    private final OwnerRepository ownerRepository;

    // ✅ 支持关键词、楼栋、状态筛选
    public List<PropUnit> list(String keyword, Long buildingId, Integer status) {
        List<PropUnit> result = unitRepository.findAll();

        // 按楼栋筛选
        if (buildingId != null) {
            result = result.stream()
                    .filter(u -> u.getBuildingId().equals(buildingId))
                    .collect(Collectors.toList());
        }

        // 按关键词搜索房号
        if (StringUtils.hasText(keyword)) {
            result = result.stream()
                    .filter(u -> u.getUnitNo() != null && u.getUnitNo().contains(keyword))
                    .collect(Collectors.toList());
        }

        // 按状态筛选
        if (status != null) {
            result = result.stream()
                    .filter(u -> u.getStatus() != null && u.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        return result;
    }

    public PropUnit getById(Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new BusinessException("房屋不存在"));
    }

    public PropUnit save(PropUnit unit) {
        if (unit.getOwnerCount() == null) {
            unit.setOwnerCount(0);
        }
        if (unit.getAreaSqm() == null || unit.getAreaSqm().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("面积必须大于0");
        }
        return unitRepository.save(unit);
    }

    public void delete(Long id) {
        // 检查是否有业主关联
        // 如果有业主关联，提示不能删除
        unitRepository.deleteById(id);
    }

    // ✅ 统计信息
    public Map<String, Object> getStats() {
        List<PropUnit> all = unitRepository.findAll();
        long total = all.size();
        long occupied = all.stream().filter(u -> u.getStatus() != null && u.getStatus() == 1).count();
        long vacant = all.stream().filter(u -> u.getStatus() != null && u.getStatus() == 2).count();
        long decorating = all.stream().filter(u -> u.getStatus() != null && u.getStatus() == 0).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("occupied", occupied);
        stats.put("vacant", vacant);
        stats.put("decorating", decorating);
        return stats;
    }

    // ✅ 按业主ID查询房屋（业主端使用）
    public List<PropUnit> listByOwner(Long ownerId) {
        return unitRepository.findByOwnerId(ownerId);
    }
}