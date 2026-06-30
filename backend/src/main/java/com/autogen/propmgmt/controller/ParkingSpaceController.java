package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.entity.ParkingSpace;
import com.autogen.propmgmt.service.ParkingSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingSpaceController {

    private final ParkingSpaceService parkingSpaceService;

    // 列表查询（支持按楼栋和状态筛选）
    @GetMapping
    public Result<List<ParkingSpace>> list(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(parkingSpaceService.list(buildingId, status));
    }

    // 按业主查询（业主端使用）
    @GetMapping("/owner/{ownerId}")
    public Result<List<ParkingSpace>> listByOwner(@PathVariable Long ownerId) {
        return Result.ok(parkingSpaceService.listByOwner(ownerId));
    }

    // 获取详情
    @GetMapping("/{id}")
    public Result<ParkingSpace> get(@PathVariable Long id) {
        return Result.ok(parkingSpaceService.getById(id));
    }

    // 新增
    @PostMapping
    public Result<ParkingSpace> create(@RequestBody ParkingSpace space) {
        return Result.ok(parkingSpaceService.save(space));
    }

    // 更新
    @PutMapping("/{id}")
    public Result<ParkingSpace> update(@PathVariable Long id, @RequestBody ParkingSpace space) {
        space.setId(id);
        return Result.ok(parkingSpaceService.save(space));
    }

    // 分配车位给业主
    @PutMapping("/{id}/assign")
    public Result<ParkingSpace> assignToOwner(@PathVariable Long id, @RequestParam Long ownerId) {
        return Result.ok(parkingSpaceService.assignToOwner(id, ownerId));
    }

    // 释放车位
    @PutMapping("/{id}/release")
    public Result<ParkingSpace> releaseOwner(@PathVariable Long id) {
        return Result.ok(parkingSpaceService.releaseOwner(id));
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        parkingSpaceService.delete(id);
        return Result.ok();
    }
}