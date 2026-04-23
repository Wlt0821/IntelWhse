package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.InventoryRecord;
import com.wms.mapper.InventoryRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryRecordService {

    private final InventoryRecordMapper inventoryRecordMapper;

    public Page<InventoryRecord> page(Integer pageNum, Integer pageSize, String batchId, String deviceName) {
        Page<InventoryRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(batchId)) {
            wrapper.like(InventoryRecord::getBatchId, batchId);
        }
        if (StringUtils.hasText(deviceName)) {
            wrapper.like(InventoryRecord::getDeviceName, deviceName);
        }
        wrapper.orderByDesc(InventoryRecord::getTimestamp);
        return inventoryRecordMapper.selectPage(page, wrapper);
    }

    public List<InventoryRecord> list(String batchId, String deviceName) {
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(batchId)) {
            wrapper.like(InventoryRecord::getBatchId, batchId);
        }
        if (StringUtils.hasText(deviceName)) {
            wrapper.like(InventoryRecord::getDeviceName, deviceName);
        }
        wrapper.orderByDesc(InventoryRecord::getTimestamp);
        return inventoryRecordMapper.selectList(wrapper);
    }

    public InventoryRecord getById(Long id) {
        return inventoryRecordMapper.selectById(id);
    }

    public InventoryRecord getByBatchId(String batchId) {
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryRecord::getBatchId, batchId);
        return inventoryRecordMapper.selectOne(wrapper);
    }

    public void save(InventoryRecord record) {
        inventoryRecordMapper.insert(record);
    }

    public void update(InventoryRecord record) {
        inventoryRecordMapper.updateById(record);
    }

    public void delete(Long id) {
        inventoryRecordMapper.deleteById(id);
    }

    public InventoryRecord getLatestRecord() {
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InventoryRecord::getTimestamp);
        wrapper.last("LIMIT 1");
        return inventoryRecordMapper.selectOne(wrapper);
    }
}
