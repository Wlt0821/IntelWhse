package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.Result;
import com.wms.entity.TestData;
import com.wms.mapper.TestDataMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "测试数据管理")
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final TestDataMapper testDataMapper;

    @Operation(summary = "保存测试数据")
    @PostMapping("/save")
    public Result<TestData> saveTestData(
            @RequestParam BigDecimal value,
            @RequestParam(required = false) String testData) {
        TestData data = new TestData();
        data.setValue(value);
        data.setTestData(testData);
        data.setCreateTime(LocalDateTime.now());
        testDataMapper.insert(data);
        return Result.success(data);
    }

    @Operation(summary = "获取所有测试数据")
    @GetMapping("/all")
    public Result<List<TestData>> getAllTestData() {
        List<TestData> list = testDataMapper.selectList(
                new LambdaQueryWrapper<TestData>().orderByDesc(TestData::getCreateTime)
        );
        return Result.success(list);
    }

    @Operation(summary = "分页获取测试数据")
    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<TestData>> getTestDataPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<TestData> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TestData> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TestData::getCreateTime);
        return Result.success(testDataMapper.selectPage(page, wrapper));
    }
}
