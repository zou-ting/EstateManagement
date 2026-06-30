package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.dto.PaymentRecordDetail;
import com.autogen.propmgmt.entity.PaymentRecord;
import com.autogen.propmgmt.service.PaymentRecordService;
import com.autogen.propmgmt.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;
    private final PaymentService paymentService;

    // ========== 查询接口 ==========

    // 获取所有缴费记录
    @GetMapping
    public Result<List<PaymentRecord>> list() {
        return Result.ok(paymentService.list());
    }

    // 按房间查询（带账单月份详情）
    @GetMapping("/room/{roomId}")
    public Result<List<PaymentRecordDetail>> getByRoom(@PathVariable Long roomId) {
        return Result.ok(paymentRecordService.findDetailByRoomId(roomId));
    }

    // 按房间查询（返回实体，兼容旧接口）
    @GetMapping("/room/{roomId}/simple")
    public Result<List<PaymentRecord>> listByRoom(@PathVariable Long roomId) {
        return Result.ok(paymentService.listByRoom(roomId));
    }

    // 按账单ID查询缴费记录
    @GetMapping("/fee/{feeId}")
    public Result<List<PaymentRecord>> getByFee(@PathVariable Long feeId) {
        return Result.ok(paymentRecordService.findByFeeId(feeId));
    }

    // 获取单个缴费记录
    @GetMapping("/{id}")
    public Result<PaymentRecord> get(@PathVariable Long id) {
        return Result.ok(paymentService.getById(id));
    }

    // ========== 支付操作 ==========

    // 在线支付（业主端使用）
    @PostMapping("/pay/{feeId}")
    public Result<PaymentRecord> pay(@PathVariable Long feeId, @RequestBody Map<String, String> body) {
        String payMethod = body.getOrDefault("payMethod", "CASH");
        String operator = body.get("operator");
        return Result.ok(paymentService.pay(feeId, payMethod, operator));
    }

    // ========== 管理端操作 ==========

    // 新增缴费记录
    @PostMapping
    public Result<PaymentRecord> create(@RequestBody PaymentRecord record) {
        return Result.ok(paymentRecordService.save(record));
    }

    // 按账单ID更新缴费记录
    @PutMapping("/fee/{feeId}")
    public Result<PaymentRecord> updateByFeeId(@PathVariable Long feeId, @RequestBody PaymentRecord record) {
        return Result.ok(paymentRecordService.updateByFeeId(feeId, record));
    }

    // 按账单ID删除缴费记录
    @DeleteMapping("/fee/{feeId}")
    public Result<Void> deleteByFeeId(@PathVariable Long feeId) {
        paymentRecordService.deleteByFeeId(feeId);
        return Result.ok();
    }

    // 删除缴费记录
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return Result.ok();
    }
}