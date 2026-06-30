package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.entity.PropUnit;
import com.autogen.propmgmt.service.PropUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class PropUnitController {

    private final PropUnitService unitService;

    // ✅ 支持关键词、楼栋、状态筛选
    @GetMapping
    public Result<List<PropUnit>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(unitService.list(keyword, buildingId, status));
    }

    @GetMapping("/{id}")
    public Result<PropUnit> get(@PathVariable Long id) {
        return Result.ok(unitService.getById(id));
    }

    @PostMapping
    public Result<PropUnit> create(@RequestBody PropUnit unit) {
        return Result.ok(unitService.save(unit));
    }

    @PutMapping("/{id}")
    public Result<PropUnit> update(@PathVariable Long id, @RequestBody PropUnit unit) {
        unit.setId(id);
        return Result.ok(unitService.save(unit));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        unitService.delete(id);
        return Result.ok();
    }

    // ✅ 统计信息
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.ok(unitService.getStats());
    }

    // ✅ 按业主ID查询房屋（业主端使用）
    @GetMapping("/owner/{ownerId}")
    public Result<List<PropUnit>> listByOwner(@PathVariable Long ownerId) {
        return Result.ok(unitService.listByOwner(ownerId));
    }
}