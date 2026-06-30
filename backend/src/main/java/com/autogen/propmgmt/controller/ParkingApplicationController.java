package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.entity.ParkingApplication;
import com.autogen.propmgmt.entity.ParkingSpace;
import com.autogen.propmgmt.service.ParkingApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-application")
@RequiredArgsConstructor
public class ParkingApplicationController {

    private final ParkingApplicationService applicationService;

    // ========== 业主端接口 ==========

    // 业主提交申请
    @PostMapping
    public Result<ParkingApplication> submit(@RequestBody ParkingApplication application) {
        return Result.ok(applicationService.submit(application));
    }

    // 业主取消申请（仅限待审批状态）
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        applicationService.cancel(id);
        return Result.ok();
    }

    // 业主查询自己的申请记录
    @GetMapping("/owner/{ownerId}")
    public Result<List<ParkingApplication>> listByOwner(@PathVariable Long ownerId) {
        return Result.ok(applicationService.listByOwner(ownerId));
    }

    // 业主查询申请详情
    @GetMapping("/{id}")
    public Result<ParkingApplication> get(@PathVariable Long id) {
        return Result.ok(applicationService.getById(id));
    }

    // 业主查询空车位（供申请时选择）
    @GetMapping("/available")
    public Result<List<ParkingSpace>> getAvailableSpaces() {
        return Result.ok(applicationService.getAvailableSpaces());
    }

    // 业主续租申请
    @PostMapping("/renew")
    public Result<ParkingApplication> renew(@RequestBody RenewRequest request) {
        return Result.ok(applicationService.renew(request));
    }

    // 业主退租申请
    @PostMapping("/cancel-rent")
    public Result<ParkingApplication> cancelRent(@RequestBody CancelRentRequest request) {
        return Result.ok(applicationService.cancelRent(request));
    }

    // ========== 管理员端接口 ==========

    // 管理员获取所有待审批申请
    @GetMapping("/pending")
    public Result<List<ParkingApplication>> getPendingList() {
        return Result.ok(applicationService.getPendingList());
    }

    // 管理员审批申请
    @PutMapping("/{id}/approve")
    public Result<ParkingApplication> approve(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) Double purchaseAmount) {
        return Result.ok(applicationService.approve(id, approved, remark, purchaseAmount));
    }

    // 业主标记购买申请为已付款
    @PostMapping("/{id}/mark-paid")
    public Result<ParkingApplication> markAsPaid(@PathVariable Long id) {
        return Result.ok(applicationService.markAsPaid(id));
    }

    // 管理员完成车位购买（确认付款后）
    @PostMapping("/{id}/complete")
    public Result<ParkingApplication> complete(@PathVariable Long id) {
        return Result.ok(applicationService.complete(id));
    }

    // ========== 内部请求类 ==========

    @lombok.Data
    public static class RenewRequest {
        private Long parkingSpaceId;
        private Long ownerId;
        private String rentalDuration;
        private String reason;
    }

    @lombok.Data
    public static class CancelRentRequest {
        private Long parkingSpaceId;
        private Long ownerId;
        private String reason;
    }
}