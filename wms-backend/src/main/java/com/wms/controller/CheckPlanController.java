package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.CheckPlan;
import com.wms.service.CheckPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "盘点计划管理")
@RestController
@RequestMapping("/stocktake/plan")
@RequiredArgsConstructor
public class CheckPlanController {

    private final CheckPlanService checkPlanService;

    @Operation(summary = "分页查询盘点计划列表")
    @GetMapping("/page")
    public Result<Page<CheckPlan>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String planNo,
            @RequestParam(required = false) String status) {
        Page<CheckPlan> page = checkPlanService.page(pageNum, pageSize, planNo, status);
        return Result.success(page);
    }

    @Operation(summary = "获取盘点计划详情")
    @GetMapping("/{id}")
    public Result<CheckPlan> getById(@PathVariable Long id) {
        return Result.success(checkPlanService.getById(id));
    }

    @Operation(summary = "新增盘点计划")
    @PostMapping
    @OperLog("新增盘点计划")
    public Result<Void> save(@RequestBody CheckPlan plan) {
        checkPlanService.save(plan);
        return Result.success();
    }

    @Operation(summary = "修改盘点计划")
    @PutMapping
    @OperLog("修改盘点计划")
    public Result<Void> update(@RequestBody CheckPlan plan) {
        checkPlanService.update(plan);
        return Result.success();
    }

    @Operation(summary = "删除盘点计划")
    @DeleteMapping("/{id}")
    @OperLog("删除盘点计划")
    public Result<Void> delete(@PathVariable Long id) {
        checkPlanService.delete(id);
        return Result.success();
    }
}
