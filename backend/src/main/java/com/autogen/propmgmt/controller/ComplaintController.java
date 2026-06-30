package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.entity.Complaint;
import com.autogen.propmgmt.entity.ComplaintReply;
import com.autogen.propmgmt.service.ComplaintService;
import com.autogen.propmgmt.service.ComplaintReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ComplaintReplyService replyService;

    // ========== 管理端接口 ==========

    // 列表查询（支持状态和类型筛选）
    @GetMapping
    public Result<List<Complaint>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        return Result.ok(complaintService.list(status, type));
    }

    // 获取详情
    @GetMapping("/{id}")
    public Result<Complaint> get(@PathVariable Long id) {
        return Result.ok(complaintService.getById(id));
    }

    // 新增投诉/建议（业主端也用）
    @PostMapping
    public Result<Complaint> create(@RequestBody Complaint complaint) {
        return Result.ok(complaintService.save(complaint));
    }

    // 更新投诉/建议
    @PutMapping("/{id}")
    public Result<Complaint> update(@PathVariable Long id, @RequestBody Complaint complaint) {
        complaint.setId(id);
        return Result.ok(complaintService.save(complaint));
    }

    // 更新状态
    @PutMapping("/{id}/status")
    public Result<Complaint> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return Result.ok(complaintService.updateStatus(id, body.get("status")));
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        complaintService.delete(id);
        return Result.ok();
    }

    // ========== 业主端接口 ==========

    // 按业主查询（业主端使用）
    @GetMapping("/owner/{ownerId}")
    public Result<List<Complaint>> listByOwner(@PathVariable Long ownerId) {
        return Result.ok(complaintService.listByOwner(ownerId));
    }

    // ========== 回复管理接口 ==========

    // 获取某投诉的所有回复
    @GetMapping("/{id}/replies")
    public Result<List<ComplaintReply>> listReplies(@PathVariable Long id) {
        return Result.ok(replyService.listByComplaintId(id));
    }

    // 添加回复（管理员回复）
    @PostMapping("/{id}/reply")
    public Result<ComplaintReply> addReply(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        complaintService.updateStatus(id, "PROCESSING");

        ComplaintReply reply = new ComplaintReply();
        reply.setComplaintId(id);
        reply.setReplyContent(body.get("reply").toString());
        reply.setAdminId(body.get("adminId") != null ? Long.valueOf(body.get("adminId").toString()) : null);
        reply.setAdminRead(true);
        reply.setOwnerRead(false);
        return Result.ok(replyService.save(reply));
    }

    // 业主回复（业主端使用）
    @PostMapping("/{id}/owner-reply")
    public Result<ComplaintReply> addOwnerReply(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        ComplaintReply reply = new ComplaintReply();
        reply.setComplaintId(id);
        reply.setReplyContent(body.get("reply").toString());
        reply.setOwnerId(body.get("ownerId") != null ? Long.valueOf(body.get("ownerId").toString()) : null);
        reply.setAdminRead(false);
        reply.setOwnerRead(true);
        return Result.ok(replyService.save(reply));
    }

    // 获取管理员未读数量
    @GetMapping("/admin/unread-count")
    public Result<Integer> getAdminUnreadCount() {
        return Result.ok(replyService.countAdminUnread());
    }

    // 获取业主未读数量
    @GetMapping("/owner/{ownerId}/unread-count")
    public Result<Integer> getOwnerUnreadCount(@PathVariable Long ownerId) {
        return Result.ok(replyService.countOwnerUnread(ownerId));
    }

    // 标记管理员所有未读为已读
    @PostMapping("/admin/mark-all-read")
    public Result<Void> markAllAdminRead() {
        replyService.markAllAdminRead();
        return Result.ok();
    }

    // 标记业主所有未读为已读
    @PostMapping("/owner/{ownerId}/mark-all-read")
    public Result<Void> markAllOwnerRead(@PathVariable Long ownerId) {
        replyService.markAllOwnerRead(ownerId);
        return Result.ok();
    }
}