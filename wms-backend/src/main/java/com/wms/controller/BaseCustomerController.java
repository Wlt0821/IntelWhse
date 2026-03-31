package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.BaseCustomer;
import com.wms.service.BaseCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "客户管理")
@RestController
@RequestMapping("/base/customer")
@RequiredArgsConstructor
public class BaseCustomerController {

    private final BaseCustomerService baseCustomerService;

    @Operation(summary = "分页查询客户列表")
    @GetMapping("/page")
    public Result<Page<BaseCustomer>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer status) {
        Page<BaseCustomer> page = baseCustomerService.page(pageNum, pageSize, customerName, status);
        return Result.success(page);
    }

    @Operation(summary = "获取客户详情")
    @GetMapping("/{id}")
    public Result<BaseCustomer> getById(@PathVariable Long id) {
        return Result.success(baseCustomerService.getById(id));
    }

    @Operation(summary = "新增客户")
    @PostMapping
    @OperLog("新增客户")
    public Result<Void> save(@RequestBody BaseCustomer customer) {
        baseCustomerService.save(customer);
        return Result.success();
    }

    @Operation(summary = "修改客户")
    @PutMapping
    @OperLog("修改客户")
    public Result<Void> update(@RequestBody BaseCustomer customer) {
        baseCustomerService.update(customer);
        return Result.success();
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/{id}")
    @OperLog("删除客户")
    public Result<Void> delete(@PathVariable Long id) {
        baseCustomerService.delete(id);
        return Result.success();
    }
}
