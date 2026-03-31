package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.Packing;
import com.wms.service.PackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "装箱单管理")
@RestController
@RequestMapping("/outbound/packing")
@RequiredArgsConstructor
public class PackingController {

    private final PackingService packingService;

    @Operation(summary = "分页查询装箱单列表")
    @GetMapping("/page")
    public Result<Page<Packing>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String packingNo,
            @RequestParam(required = false) String status) {
        Page<Packing> page = packingService.page(pageNum, pageSize, packingNo, status);
        return Result.success(page);
    }

    @Operation(summary = "获取装箱单详情")
    @GetMapping("/{id}")
    public Result<Packing> getById(@PathVariable Long id) {
        return Result.success(packingService.getById(id));
    }

    @Operation(summary = "新增装箱单")
    @PostMapping
    @OperLog("新增装箱单")
    public Result<Void> save(@RequestBody Packing packing) {
        packingService.save(packing);
        return Result.success();
    }

    @Operation(summary = "修改装箱单")
    @PutMapping
    @OperLog("修改装箱单")
    public Result<Void> update(@RequestBody Packing packing) {
        packingService.update(packing);
        return Result.success();
    }

    @Operation(summary = "删除装箱单")
    @DeleteMapping("/{id}")
    @OperLog("删除装箱单")
    public Result<Void> delete(@PathVariable Long id) {
        packingService.delete(id);
        return Result.success();
    }
}
