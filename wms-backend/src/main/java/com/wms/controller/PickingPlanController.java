package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.PickingPlan;
import com.wms.service.PickingPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "拣选计划管理")
@RestController
@RequestMapping("/order/picking-plan")
@RequiredArgsConstructor
public class PickingPlanController {

    private final PickingPlanService pickingPlanService;

    @Operation(summary = "分页查询拣选计划列表")
    @GetMapping("/page")
    public Result<Page<PickingPlan>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String planNo,
            @RequestParam(required = false) String status) {
        Page<PickingPlan> page = pickingPlanService.page(pageNum, pageSize, planNo, status);
        return Result.success(page);
    }

    @Operation(summary = "获取拣选计划详情")
    @GetMapping("/{id}")
    public Result<PickingPlan> getById(@PathVariable Long id) {
        return Result.success(pickingPlanService.getById(id));
    }

    @Operation(summary = "新增拣选计划")
    @PostMapping
    @OperLog("新增拣选计划")
    public Result<Void> save(@RequestBody PickingPlan plan) {
        pickingPlanService.save(plan);
        return Result.success();
    }

    @Operation(summary = "修改拣选计划")
    @PutMapping
    @OperLog("修改拣选计划")
    public Result<Void> update(@RequestBody PickingPlan plan) {
        pickingPlanService.update(plan);
        return Result.success();
    }

    @Operation(summary = "删除拣选计划")
    @DeleteMapping("/{id}")
    @OperLog("删除拣选计划")
    public Result<Void> delete(@PathVariable Long id) {
        pickingPlanService.delete(id);
        return Result.success();
    }
}
