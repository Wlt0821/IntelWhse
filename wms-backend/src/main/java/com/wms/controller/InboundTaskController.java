package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.InboundTask;
import com.wms.service.InboundTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "入库作业管理")
@RestController
@RequestMapping("/inbound/task")
@RequiredArgsConstructor
public class InboundTaskController {

    private final InboundTaskService inboundTaskService;

    @Operation(summary = "分页查询入库作业列表")
    @GetMapping("/page")
    public Result<Page<InboundTask>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String taskNo,
            @RequestParam(required = false) String status) {
        Page<InboundTask> page = inboundTaskService.page(pageNum, pageSize, taskNo, status);
        return Result.success(page);
    }

    @Operation(summary = "获取入库作业详情")
    @GetMapping("/{id}")
    public Result<InboundTask> getById(@PathVariable Long id) {
        return Result.success(inboundTaskService.getById(id));
    }

    @Operation(summary = "新增入库作业")
    @PostMapping
    @OperLog("新增入库作业")
    public Result<Void> save(@RequestBody InboundTask task) {
        inboundTaskService.save(task);
        return Result.success();
    }

    @Operation(summary = "修改入库作业")
    @PutMapping
    @OperLog("修改入库作业")
    public Result<Void> update(@RequestBody InboundTask task) {
        inboundTaskService.update(task);
        return Result.success();
    }

    @Operation(summary = "删除入库作业")
    @DeleteMapping("/{id}")
    @OperLog("删除入库作业")
    public Result<Void> delete(@PathVariable Long id) {
        inboundTaskService.delete(id);
        return Result.success();
    }
}
