package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.BaseContainer;
import com.wms.service.BaseContainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "容器管理")
@RestController
@RequestMapping("/base/container")
@RequiredArgsConstructor
public class BaseContainerController {

    private final BaseContainerService baseContainerService;

    @Operation(summary = "分页查询容器列表")
    @GetMapping("/page")
    public Result<Page<BaseContainer>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String containerName,
            @RequestParam(required = false) Integer status) {
        Page<BaseContainer> page = baseContainerService.page(pageNum, pageSize, containerName, status);
        return Result.success(page);
    }

    @Operation(summary = "获取容器详情")
    @GetMapping("/{id}")
    public Result<BaseContainer> getById(@PathVariable Long id) {
        return Result.success(baseContainerService.getById(id));
    }

    @Operation(summary = "新增容器")
    @PostMapping
    @OperLog("新增容器")
    public Result<Void> save(@RequestBody BaseContainer container) {
        baseContainerService.save(container);
        return Result.success();
    }

    @Operation(summary = "修改容器")
    @PutMapping
    @OperLog("修改容器")
    public Result<Void> update(@RequestBody BaseContainer container) {
        baseContainerService.update(container);
        return Result.success();
    }

    @Operation(summary = "删除容器")
    @DeleteMapping("/{id}")
    @OperLog("删除容器")
    public Result<Void> delete(@PathVariable Long id) {
        baseContainerService.delete(id);
        return Result.success();
    }
}
