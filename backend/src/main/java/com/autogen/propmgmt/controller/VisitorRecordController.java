package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.entity.VisitorRecord;
import com.autogen.propmgmt.service.VisitorRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
public class VisitorRecordController {

    private final VisitorRecordService visitorRecordService;

    // ✅ 支持按状态筛选
    @GetMapping
    public Result<List<VisitorRecord>> list(@RequestParam(required = false) String status) {
        return Result.ok(visitorRecordService.list(status));
    }

    // ✅ 按业主查询（业主端使用）
    @GetMapping("/owner/{ownerId}")
    public Result<List<VisitorRecord>> listByOwner(@PathVariable Long ownerId) {
        return Result.ok(visitorRecordService.listByOwner(ownerId));
    }

    @GetMapping("/{id}")
    public Result<VisitorRecord> get(@PathVariable Long id) {
        return Result.ok(visitorRecordService.getById(id));
    }

    @PostMapping
    public Result<VisitorRecord> create(@RequestBody VisitorRecord body) {
        return Result.ok(visitorRecordService.save(body));
    }

    @PutMapping("/{id}")
    public Result<VisitorRecord> update(@PathVariable Long id, @RequestBody VisitorRecord body) {
        body.setId(id);
        return Result.ok(visitorRecordService.save(body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        visitorRecordService.delete(id);
        return Result.ok();
    }
}