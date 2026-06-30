package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.entity.PropBuilding;
import com.autogen.propmgmt.service.PropBuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class PropBuildingController {

    private final PropBuildingService buildingService;

    // ✅ 支持 keyword 和 buildingType 两个参数
    @GetMapping
    public Result<List<PropBuilding>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String buildingType) {
        return Result.ok(buildingService.list(keyword, buildingType));
    }

    @GetMapping("/{id}")
    public Result<PropBuilding> get(@PathVariable Long id) {
        return Result.ok(buildingService.getById(id));
    }

    @PostMapping
    public Result<PropBuilding> create(@RequestBody PropBuilding building) {
        return Result.ok(buildingService.save(building));
    }

    @PutMapping("/{id}")
    public Result<PropBuilding> update(@PathVariable Long id, @RequestBody PropBuilding building) {
        building.setId(id);
        return Result.ok(buildingService.save(building));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return Result.ok();
    }

    // ✅ 批量删除
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        buildingService.batchDelete(ids);
        return Result.ok();
    }

    // ✅ 统计信息
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.ok(buildingService.getStats());
    }
}