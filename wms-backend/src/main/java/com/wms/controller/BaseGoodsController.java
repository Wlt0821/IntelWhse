package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.entity.BaseGoods;
import com.wms.service.BaseGoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品管理")
@RestController
@RequestMapping("/base/goods")
@RequiredArgsConstructor
public class BaseGoodsController {

    private final BaseGoodsService baseGoodsService;

    @Operation(summary = "分页查询商品列表")
    @GetMapping("/page")
    public Result<Page<BaseGoods>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String goodsName,
            @RequestParam(required = false) Integer status) {
        Page<BaseGoods> page = baseGoodsService.page(pageNum, pageSize, goodsName, status);
        return Result.success(page);
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<BaseGoods> getById(@PathVariable Long id) {
        return Result.success(baseGoodsService.getById(id));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    @OperLog("新增商品")
    public Result<Void> save(@RequestBody BaseGoods goods) {
        baseGoodsService.save(goods);
        return Result.success();
    }

    @Operation(summary = "修改商品")
    @PutMapping
    @OperLog("修改商品")
    public Result<Void> update(@RequestBody BaseGoods goods) {
        baseGoodsService.update(goods);
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    @OperLog("删除商品")
    public Result<Void> delete(@PathVariable Long id) {
        baseGoodsService.delete(id);
        return Result.success();
    }
}
