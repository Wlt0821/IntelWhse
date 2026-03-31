package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.BaseZone;
import com.wms.service.BaseZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "库区管理")
@RestController
@RequestMapping("/base/zone")
@RequiredArgsConstructor
public class BaseZoneController {

    private final BaseZoneService baseZoneService;

    @Operation(summary = "分页查询库区列表")
    @GetMapping("/page")
    public Result<Page<BaseZone>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String zoneName,
            @RequestParam(required = false) Integer status) {
        Page<BaseZone> page = baseZoneService.page(pageNum, pageSize, zoneName, status);
        return Result.success(page);
    }

    @Operation(summary = "获取库区详情")
    @GetMapping("/{id}")
    public Result<BaseZone> getById(@PathVariable Long id) {
        return Result.success(baseZoneService.getById(id));
    }

    @Operation(summary = "新增库区")
    @PostMapping
    @OperLog("新增库区")
    public Result<Void> save(@RequestBody BaseZone zone) {
        baseZoneService.save(zone);
        return Result.success();
    }

    @Operation(summary = "修改库区")
    @PutMapping
    @OperLog("修改库区")
    public Result<Void> update(@RequestBody BaseZone zone) {
        baseZoneService.update(zone);
        return Result.success();
    }

    @Operation(summary = "删除库区")
    @DeleteMapping("/{id}")
    @OperLog("删除库区")
    public Result<Void> delete(@PathVariable Long id) {
        baseZoneService.delete(id);
        return Result.success();
    }
}
