package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.dto.TransferPlanVO;
import com.wms.entity.TransferPlan;
import com.wms.service.TransferPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "移库计划管理")
@RestController
@RequestMapping("/transfer/plan")
@RequiredArgsConstructor
public class TransferPlanController {

    private final TransferPlanService transferPlanService;

    @Operation(summary = "分页查询移库计划列表")
    @GetMapping("/page")
    public Result<Page<TransferPlanVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String planNo,
            @RequestParam(required = false) String status) {
        Page<TransferPlanVO> page = transferPlanService.page(pageNum, pageSize, planNo, status);
        return Result.success(page);
    }

    @Operation(summary = "获取移库计划详情")
    @GetMapping("/{id}")
    public Result<TransferPlan> getById(@PathVariable Long id) {
        return Result.success(transferPlanService.getById(id));
    }

    @Operation(summary = "新增移库计划")
    @PostMapping
    @OperLog("新增移库计划")
    public Result<Void> save(@RequestBody TransferPlan plan) {
        transferPlanService.save(plan);
        return Result.success();
    }

    @Operation(summary = "修改移库计划")
    @PutMapping
    @OperLog("修改移库计划")
    public Result<Void> update(@RequestBody TransferPlan plan) {
        transferPlanService.update(plan);
        return Result.success();
    }

    @Operation(summary = "删除移库计划")
    @DeleteMapping("/{id}")
    @OperLog("删除移库计划")
    public Result<Void> delete(@PathVariable Long id) {
        transferPlanService.delete(id);
        return Result.success();
    }
}
