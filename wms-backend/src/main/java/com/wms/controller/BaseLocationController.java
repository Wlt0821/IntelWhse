package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.BaseLocation;
import com.wms.service.BaseLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "仓位管理")
@RestController
@RequestMapping("/base/location")
@RequiredArgsConstructor
public class BaseLocationController {

    private final BaseLocationService baseLocationService;

    @Operation(summary = "分页查询仓位列表")
    @GetMapping("/page")
    public Result<Page<BaseLocation>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String locationName,
            @RequestParam(required = false) Integer status) {
        Page<BaseLocation> page = baseLocationService.page(pageNum, pageSize, locationName, status);
        return Result.success(page);
    }

    @Operation(summary = "获取仓位详情")
    @GetMapping("/{id}")
    public Result<BaseLocation> getById(@PathVariable Long id) {
        return Result.success(baseLocationService.getById(id));
    }

    @Operation(summary = "新增仓位")
    @PostMapping
    @OperLog("新增仓位")
    public Result<Void> save(@RequestBody BaseLocation location) {
        baseLocationService.save(location);
        return Result.success();
    }

    @Operation(summary = "修改仓位")
    @PutMapping
    @OperLog("修改仓位")
    public Result<Void> update(@RequestBody BaseLocation location) {
        baseLocationService.update(location);
        return Result.success();
    }

    @Operation(summary = "删除仓位")
    @DeleteMapping("/{id}")
    @OperLog("删除仓位")
    public Result<Void> delete(@PathVariable Long id) {
        baseLocationService.delete(id);
        return Result.success();
    }
}
