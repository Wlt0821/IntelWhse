package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.CustomerOrder;
import com.wms.service.CustomerOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "客户订单管理")
@RestController
@RequestMapping("/order/customer")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    @Operation(summary = "分页查询客户订单列表")
    @GetMapping("/page")
    public Result<Page<CustomerOrder>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status) {
        Page<CustomerOrder> page = customerOrderService.page(pageNum, pageSize, orderNo, status);
        return Result.success(page);
    }

    @Operation(summary = "获取客户订单详情")
    @GetMapping("/{id}")
    public Result<CustomerOrder> getById(@PathVariable Long id) {
        return Result.success(customerOrderService.getById(id));
    }

    @Operation(summary = "新增客户订单")
    @PostMapping
    @OperLog("新增客户订单")
    public Result<Void> save(@RequestBody CustomerOrder order) {
        customerOrderService.save(order);
        return Result.success();
    }

    @Operation(summary = "修改客户订单")
    @PutMapping
    @OperLog("修改客户订单")
    public Result<Void> update(@RequestBody CustomerOrder order) {
        customerOrderService.update(order);
        return Result.success();
    }

    @Operation(summary = "删除客户订单")
    @DeleteMapping("/{id}")
    @OperLog("删除客户订单")
    public Result<Void> delete(@PathVariable Long id) {
        customerOrderService.delete(id);
        return Result.success();
    }
}
