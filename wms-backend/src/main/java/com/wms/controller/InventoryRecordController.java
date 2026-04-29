package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.annotation.OperLog;
import com.wms.common.Result;
import com.wms.dto.InventoryComparisonVO;
import com.wms.dto.InventoryUploadDTO;
import com.wms.entity.InventoryRecord;
import com.wms.service.InventoryRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Tag(name = "实时盘点数据管理")
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryRecordController {

    private final InventoryRecordService inventoryRecordService;

    @Operation(summary = "获取库存对比数据（分页）")
    @GetMapping("/comparison/page")
    public Result<Page<InventoryComparisonVO>> getComparisonPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<InventoryComparisonVO> page = inventoryRecordService.getInventoryComparisonPage(pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取库存对比数据（全部）")
    @GetMapping("/comparison")
    public Result<List<InventoryComparisonVO>> getComparison() {
        List<InventoryComparisonVO> list = inventoryRecordService.getInventoryComparison();
        return Result.success(list);
    }

    @Operation(summary = "分页查询盘点记录列表")
    @GetMapping("/page")
    public Result<Page<InventoryRecord>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) String deviceName) {
        Page<InventoryRecord> page = inventoryRecordService.page(pageNum, pageSize, batchId, deviceName);
        return Result.success(page);
    }

    @Operation(summary = "查询盘点记录列表")
    @GetMapping("/list")
    public Result<List<InventoryRecord>> list(
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) String deviceName) {
        List<InventoryRecord> list = inventoryRecordService.list(batchId, deviceName);
        return Result.success(list);
    }

    @Operation(summary = "获取最新盘点记录")
    @GetMapping("/latest")
    public Result<InventoryRecord> getLatest() {
        InventoryRecord record = inventoryRecordService.getLatestRecord();
        return Result.success(record);
    }

    @Operation(summary = "获取盘点记录详情")
    @GetMapping("/{id}")
    public Result<InventoryRecord> getById(@PathVariable Long id) {
        return Result.success(inventoryRecordService.getById(id));
    }

    @Operation(summary = "根据批次号获取盘点记录")
    @GetMapping("/batch/{batchId}")
    public Result<InventoryRecord> getByBatchId(@PathVariable String batchId) {
        InventoryRecord record = inventoryRecordService.getByBatchId(batchId);
        return Result.success(record);
    }

    @Operation(summary = "接收AGX端盘点数据上传")
    @PostMapping("/upload")
    @OperLog("接收盘点数据")
    public Result<String> uploadInventory(@RequestBody InventoryRecord record) {
        inventoryRecordService.save(record);
        return Result.success("盘点数据上传成功");
    }

    @Operation(summary = "新增盘点记录")
    @PostMapping
    @OperLog("新增盘点记录")
    public Result<Void> save(@RequestBody InventoryRecord record) {
        inventoryRecordService.save(record);
        return Result.success();
    }

    @Operation(summary = "修改盘点记录")
    @PutMapping
    @OperLog("修改盘点记录")
    public Result<Void> update(@RequestBody InventoryRecord record) {
        inventoryRecordService.update(record);
        return Result.success();
    }

    @Operation(summary = "删除盘点记录")
    @DeleteMapping("/{id}")
    @OperLog("删除盘点记录")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryRecordService.delete(id);
        return Result.success();
    }

    @Operation(summary = "接收AGX端盘点数据上传（中文键值）")
    @PostMapping("/records/add")
    @OperLog("接收盘点数据")
    public Result<String> addInventory(@RequestBody InventoryUploadDTO uploadDTO) {
        try {
            InventoryRecord record = new InventoryRecord();
            record.setBatchId(uploadDTO.getBatchId());
            record.setDeviceName(uploadDTO.getDeviceName());
            
            if (uploadDTO.getTimestamp() != null) {
                record.setTimestamp(LocalDateTime.parse(uploadDTO.getTimestamp(), DateTimeFormatter.ISO_DATE_TIME));
            }
            
            record.setTotalBoxes(uploadDTO.getTotalBoxes());
            record.setTotalQrs(uploadDTO.getTotalQrs());
            
            ObjectMapper objectMapper = new ObjectMapper();
            if (uploadDTO.getCargoSummary() != null) {
                record.setCargoSummary(objectMapper.writeValueAsString(uploadDTO.getCargoSummary()));
            }
            if (uploadDTO.getQrDetails() != null) {
                record.setQrDetails(objectMapper.writeValueAsString(uploadDTO.getQrDetails()));
            }
            
            inventoryRecordService.save(record);
            return Result.success("盘点数据上传成功");
        } catch (Exception e) {
            return Result.error("盘点数据上传失败：" + e.getMessage());
        }
    }
}
