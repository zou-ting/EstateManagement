package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.entity.PropertyFee;
import com.autogen.propmgmt.service.PropertyFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/property-fees")
@RequiredArgsConstructor
public class PropertyFeeController {

    private final PropertyFeeService propertyFeeService;

    // ✅ 支持按房间和状态筛选
    @GetMapping
    public Result<List<PropertyFee>> list(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String status) {
        if (roomId != null) {
            return Result.ok(propertyFeeService.listByRoom(roomId));
        }
        if (status != null && !status.isBlank()) {
            return Result.ok(propertyFeeService.listByStatus(status));
        }
        return Result.ok(propertyFeeService.list());
    }

    @GetMapping("/owner/{ownerId}")
    public Result<List<PropertyFee>> listByOwner(
            @PathVariable Long ownerId,
            @RequestParam(required = false) Long roomId) {
        return Result.ok(propertyFeeService.listByOwner(ownerId, roomId));
    }

    @GetMapping("/{id}")
    public Result<PropertyFee> get(@PathVariable Long id) {
        return Result.ok(propertyFeeService.getById(id));
    }

    @PostMapping
    public Result<PropertyFee> create(@RequestBody PropertyFee body) {
        return Result.ok(propertyFeeService.save(body));
    }

    @PutMapping("/{id}")
    public Result<PropertyFee> update(@PathVariable Long id, @RequestBody PropertyFee body) {
        body.setId(id);
        return Result.ok(propertyFeeService.save(body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        propertyFeeService.delete(id);
        return Result.ok();
    }

    // ✅ 批量生成账单
    @PostMapping("/generate")
    public Result<List<PropertyFee>> generateBatch(@RequestBody Map<String, Object> params) {
        String billMonth = (String) params.get("billMonth");
        BigDecimal baseRate = new BigDecimal(params.get("baseRate").toString());
        BigDecimal publicRate = new BigDecimal(params.get("publicRate").toString());
        return Result.ok(propertyFeeService.generateBatch(billMonth, baseRate, publicRate));
    }

    // ✅ 获取逾期账单
    @GetMapping("/overdue")
    public Result<List<PropertyFee>> getOverdueFees(@RequestParam(defaultValue = "0") int daysOverdue) {
        return Result.ok(propertyFeeService.getOverdueFees(daysOverdue));
    }

    // ✅ 标记逾期
    @PostMapping("/overdue/mark")
    public Result<Void> markAsOverdue() {
        propertyFeeService.markAsOverdue();
        return Result.ok();
    }
}