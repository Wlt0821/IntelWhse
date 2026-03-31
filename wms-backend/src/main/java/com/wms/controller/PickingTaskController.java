package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.PickingTask;
import com.wms.service.PickingTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "拣货作业管理")
@RestController
@RequestMapping("/outbound/picking-task")
@RequiredArgsConstructor
public class PickingTaskController {

    private final PickingTaskService pickingTaskService;

    @Operation(summary = "分页查询拣货作业列表")
    @GetMapping("/page")
    public Result<Page<PickingTask>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String taskNo,
            @RequestParam(required = false) String status) {
        Page<PickingTask> page = pickingTaskService.page(pageNum, pageSize, taskNo, status);
        return Result.success(page);
    }

    @Operation(summary = "获取拣货作业详情")
    @GetMapping("/{id}")
    public Result<PickingTask> getById(@PathVariable Long id) {
        return Result.success(pickingTaskService.getById(id));
    }

    @Operation(summary = "新增拣货作业")
    @PostMapping
    @OperLog("新增拣货作业")
    public Result<Void> save(@RequestBody PickingTask task) {
        pickingTaskService.save(task);
        return Result.success();
    }

    @Operation(summary = "修改拣货作业")
    @PutMapping
    @OperLog("修改拣货作业")
    public Result<Void> update(@RequestBody PickingTask task) {
        pickingTaskService.update(task);
        return Result.success();
    }

    @Operation(summary = "删除拣货作业")
    @DeleteMapping("/{id}")
    @OperLog("删除拣货作业")
    public Result<Void> delete(@PathVariable Long id) {
        pickingTaskService.delete(id);
        return Result.success();
    }
}
