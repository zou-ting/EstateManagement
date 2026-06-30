package com.autogen.propmgmt.repository;

import com.autogen.propmgmt.entity.PropBuilding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropBuildingRepository extends JpaRepository<PropBuilding, Long> {
    // 按名称搜索
    List<PropBuilding> findByBuildingNameContaining(String keyword);

    // ✅ 按类型查询
    List<PropBuilding> findByBuildingType(String buildingType);
}