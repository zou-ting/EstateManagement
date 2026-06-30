package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.entity.PropAnnouncement;
import com.autogen.propmgmt.service.PropAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class PropAnnouncementController {

    private final PropAnnouncementService announcementService;

    // ✅ 支持按状态筛选和关键词搜索
    @GetMapping
    public Result<List<PropAnnouncement>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.ok(announcementService.list(status, keyword));
    }

    @GetMapping("/{id}")
    public Result<PropAnnouncement> get(@PathVariable Long id) {
        return Result.ok(announcementService.getById(id));
    }

    @PostMapping
    public Result<PropAnnouncement> create(@RequestBody PropAnnouncement body) {
        return Result.ok(announcementService.save(body));
    }

    @PutMapping("/{id}")
    public Result<PropAnnouncement> update(@PathVariable Long id, @RequestBody PropAnnouncement body) {
        body.setId(id);
        return Result.ok(announcementService.save(body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.ok();
    }
}