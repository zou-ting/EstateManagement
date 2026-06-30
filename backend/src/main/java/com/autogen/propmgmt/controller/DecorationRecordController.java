package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.entity.DecorationRecord;
import com.autogen.propmgmt.service.DecorationRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decoration")
@RequiredArgsConstructor
public class DecorationRecordController {

    private final DecorationRecordService service;

    @GetMapping
    public Result<List<DecorationRecord>> list() {
        return Result.ok(service.list());
    }

    @GetMapping("/{id}")
    public Result<DecorationRecord> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @PostMapping
    public Result<DecorationRecord> create(@RequestBody DecorationRecord body) {
        return Result.ok(service.save(body));
    }

    @PutMapping("/{id}")
    public Result<DecorationRecord> update(@PathVariable Long id, @RequestBody DecorationRecord body) {
        body.setId(id);
        return Result.ok(service.save(body));
    }

    @PutMapping("/{id}/approve")
    public Result<DecorationRecord> approve(@PathVariable Long id) {
        return Result.ok(service.approve(id));
    }

    @PutMapping("/{id}/reject")
    public Result<DecorationRecord> reject(@PathVariable Long id) {
        return Result.ok(service.reject(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }

    // ✅ 按业主ID查询（业主端使用）
    @GetMapping("/owner/{ownerId}")
    public Result<List<DecorationRecord>> listByOwner(@PathVariable Long ownerId) {
        return Result.ok(service.listByOwner(ownerId));
    }
}