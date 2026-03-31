package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.InboundPlan;
import com.wms.service.InboundPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "入库计划管理")
@RestController
@RequestMapping("/inbound/plan")
@RequiredArgsConstructor
public class InboundPlanController {

    private final InboundPlanService inboundPlanService;

    @Operation(summary = "分页查询入库计划列表")
    @GetMapping("/page")
    public Result<Page<InboundPlan>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String planNo,
            @RequestParam(required = false) String status) {
        Page<InboundPlan> page = inboundPlanService.page(pageNum, pageSize, planNo, status);
        return Result.success(page);
    }

    @Operation(summary = "获取入库计划详情")
    @GetMapping("/{id}")
    public Result<InboundPlan> getById(@PathVariable Long id) {
        return Result.success(inboundPlanService.getById(id));
    }

    @Operation(summary = "新增入库计划")
    @PostMapping
    @OperLog("新增入库计划")
    public Result<Void> save(@RequestBody InboundPlan plan) {
        inboundPlanService.save(plan);
        return Result.success();
    }

    @Operation(summary = "修改入库计划")
    @PutMapping
    @OperLog("修改入库计划")
    public Result<Void> update(@RequestBody InboundPlan plan) {
        inboundPlanService.update(plan);
        return Result.success();
    }

    @Operation(summary = "删除入库计划")
    @DeleteMapping("/{id}")
    @OperLog("删除入库计划")
    public Result<Void> delete(@PathVariable Long id) {
        inboundPlanService.delete(id);
        return Result.success();
    }
}
