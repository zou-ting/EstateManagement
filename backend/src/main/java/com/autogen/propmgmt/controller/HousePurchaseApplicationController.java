package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.entity.HousePurchaseApplication;
import com.autogen.propmgmt.service.HousePurchaseApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/house-purchases")
@RequiredArgsConstructor
public class HousePurchaseApplicationController {

    private final HousePurchaseApplicationService service;

    @GetMapping
    public Result<List<HousePurchaseApplication>> list() {
        return Result.ok(service.listAll());
    }

    @PostMapping
    public Result<HousePurchaseApplication> submit(@RequestBody HousePurchaseApplication application) {
        return Result.ok(service.submit(application));
    }

    @GetMapping("/owner/{ownerId}")
    public Result<List<HousePurchaseApplication>> listByOwner(@PathVariable Long ownerId) {
        return Result.ok(service.listByOwner(ownerId));
    }

    @GetMapping("/pending")
    public Result<List<HousePurchaseApplication>> getPendingList() {
        return Result.ok(service.getPendingList());
    }

    @GetMapping("/{id}")
    public Result<HousePurchaseApplication> getById(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return Result.ok();
    }

    @PostMapping("/{id}/approve")
    public Result<HousePurchaseApplication> approve(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) Double purchaseAmount) {
        return Result.ok(service.approve(id, approved, remark, purchaseAmount));
    }

    @PostMapping("/{id}/mark-paid")
    public Result<HousePurchaseApplication> markAsPaid(@PathVariable Long id) {
        return Result.ok(service.markAsPaid(id));
    }

    @PostMapping("/{id}/complete")
    public Result<HousePurchaseApplication> complete(@PathVariable Long id) {
        return Result.ok(service.complete(id));
    }
}
