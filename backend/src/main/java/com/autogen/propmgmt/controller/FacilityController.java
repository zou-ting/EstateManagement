package com.autogen.propmgmt.controller;

import com.autogen.propmgmt.common.Result;
import com.autogen.propmgmt.entity.Facility;
import com.autogen.propmgmt.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService service;

    @GetMapping
    public Result<List<Facility>> list() {
        return Result.ok(service.list());
    }

    @GetMapping("/{id}")
    public Result<Facility> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @PostMapping
    public Result<Facility> create(@RequestBody Facility body) {
        return Result.ok(service.save(body));
    }

    @PutMapping("/{id}")
    public Result<Facility> update(@PathVariable Long id, @RequestBody Facility body) {
        body.setId(id);
        return Result.ok(service.save(body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}