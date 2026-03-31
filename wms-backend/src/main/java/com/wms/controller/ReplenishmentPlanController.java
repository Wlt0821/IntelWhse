package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.ReplenishmentPlan;
import com.wms.service.ReplenishmentPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "补货计划管理")
@RestController
@RequestMapping("/replenish/plan")
@RequiredArgsConstructor
public class ReplenishmentPlanController {

    private final ReplenishmentPlanService replenishmentPlanService;

    @Operation(summary = "分页查询补货计划列表")
    @GetMapping("/page")
    public Result<Page<ReplenishmentPlan>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String planNo,
            @RequestParam(required = false) String status) {
        Page<ReplenishmentPlan> page = replenishmentPlanService.page(pageNum, pageSize, planNo, status);
        return Result.success(page);
    }

    @Operation(summary = "获取补货计划详情")
    @GetMapping("/{id}")
    public Result<ReplenishmentPlan> getById(@PathVariable Long id) {
        return Result.success(replenishmentPlanService.getById(id));
    }

    @Operation(summary = "新增补货计划")
    @PostMapping
    @OperLog("新增补货计划")
    public Result<Void> save(@RequestBody ReplenishmentPlan plan) {
        replenishmentPlanService.save(plan);
        return Result.success();
    }

    @Operation(summary = "修改补货计划")
    @PutMapping
    @OperLog("修改补货计划")
    public Result<Void> update(@RequestBody ReplenishmentPlan plan) {
        replenishmentPlanService.update(plan);
        return Result.success();
    }

    @Operation(summary = "删除补货计划")
    @DeleteMapping("/{id}")
    @OperLog("删除补货计划")
    public Result<Void> delete(@PathVariable Long id) {
        replenishmentPlanService.delete(id);
        return Result.success();
    }
}
