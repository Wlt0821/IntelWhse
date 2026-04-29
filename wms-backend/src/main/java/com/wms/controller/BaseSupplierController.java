package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.BaseSupplier;
import com.wms.service.BaseSupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "供应商管理")
@RestController
@RequestMapping("/base/supplier")
@RequiredArgsConstructor
public class BaseSupplierController {

    private final BaseSupplierService baseSupplierService;

    @Operation(summary = "获取所有供应商列表（不分页）")
    @GetMapping("/all")
    public Result<List<BaseSupplier>> getAll(
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<BaseSupplier> wrapper = new LambdaQueryWrapper<>();
        if (supplierName != null && !supplierName.isEmpty()) {
            wrapper.like(BaseSupplier::getSupplierName, supplierName);
        }
        if (status != null) {
            wrapper.eq(BaseSupplier::getStatus, status);
        }
        wrapper.orderByDesc(BaseSupplier::getCreateTime);
        List<BaseSupplier> list = baseSupplierService.list(wrapper);
        return Result.success(list);
    }

    @Operation(summary = "分页查询供应商列表")
    @GetMapping("/page")
    public Result<Page<BaseSupplier>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) Integer status) {
        Page<BaseSupplier> page = baseSupplierService.page(pageNum, pageSize, supplierName, status);
        return Result.success(page);
    }

    @Operation(summary = "获取供应商详情")
    @GetMapping("/{id}")
    public Result<BaseSupplier> getById(@PathVariable Long id) {
        return Result.success(baseSupplierService.getById(id));
    }

    @Operation(summary = "新增供应商")
    @PostMapping
    @OperLog("新增供应商")
    public Result<Void> save(@RequestBody BaseSupplier supplier) {
        baseSupplierService.save(supplier);
        return Result.success();
    }

    @Operation(summary = "修改供应商")
    @PutMapping
    @OperLog("修改供应商")
    public Result<Void> update(@RequestBody BaseSupplier supplier) {
        baseSupplierService.update(supplier);
        return Result.success();
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/{id}")
    @OperLog("删除供应商")
    public Result<Void> delete(@PathVariable Long id) {
        baseSupplierService.delete(id);
        return Result.success();
    }
}
