package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.dto.InventoryComparisonVO;
import com.wms.entity.*;
import com.wms.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryRecordService {

    private final InventoryRecordMapper inventoryRecordMapper;
    private final WmsInventoryMapper wmsInventoryMapper;
    private final BaseGoodsMapper baseGoodsMapper;
    private final BaseLocationMapper baseLocationMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public Page<InventoryComparisonVO> getInventoryComparisonPage(Integer pageNum, Integer pageSize) {
        List<InventoryComparisonVO> result = new ArrayList<>();

        List<WmsInventory> inventoryList = wmsInventoryMapper.selectList(new LambdaQueryWrapper<>());
        List<BaseGoods> goodsList = baseGoodsMapper.selectList(new LambdaQueryWrapper<>());
        List<BaseLocation> locationList = baseLocationMapper.selectList(new LambdaQueryWrapper<>());

        Map<Long, String> goodsNameMap = goodsList.stream()
                .collect(Collectors.toMap(BaseGoods::getId, BaseGoods::getGoodsName));
        Map<Long, String> goodsCodeMap = goodsList.stream()
                .collect(Collectors.toMap(BaseGoods::getId, BaseGoods::getGoodsCode));
        Map<Long, String> locationNameMap = locationList.stream()
                .collect(Collectors.toMap(BaseLocation::getId, BaseLocation::getLocationName));
        Map<Long, String> locationCodeMap = locationList.stream()
                .collect(Collectors.toMap(BaseLocation::getId, BaseLocation::getLocationCode));

        InventoryRecord latestRecord = getLatestRecord();
        Map<String, Integer> actualStockMap = new HashMap<>();

        if (latestRecord != null && latestRecord.getCargoSummary() != null) {
            try {
                Map<String, Object> summary = objectMapper.readValue(
                        latestRecord.getCargoSummary(),
                        new TypeReference<Map<String, Object>>() {}
                );
                for (Map.Entry<String, Object> entry : summary.entrySet()) {
                    Integer value = 0;
                    if (entry.getValue() instanceof Number) {
                        value = ((Number) entry.getValue()).intValue();
                    }
                    actualStockMap.put(entry.getKey(), value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (WmsInventory inventory : inventoryList) {
            InventoryComparisonVO vo = new InventoryComparisonVO();
            vo.setInventoryId(inventory.getId());
            vo.setGoodsName(goodsNameMap.getOrDefault(inventory.getGoodsId(), ""));
            vo.setGoodsCode(goodsCodeMap.getOrDefault(inventory.getGoodsId(), ""));
            vo.setLocationName(locationNameMap.getOrDefault(inventory.getLocationId(), ""));
            vo.setLocationCode(locationCodeMap.getOrDefault(inventory.getLocationId(), ""));
            vo.setPlatformQuantity(inventory.getQuantity());

            String key = vo.getGoodsCode() + "-" + vo.getLocationCode();
            Integer actualQty = actualStockMap.getOrDefault(key, 0);
            vo.setActualQuantity(BigDecimal.valueOf(actualQty));

            // 只要实际库存和平台库存不一致，就显示盘盈或盘亏
            BigDecimal diff = vo.getActualQuantity().subtract(vo.getPlatformQuantity());
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                vo.setStocktakeStatus("SURPLUS");
            } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                vo.setStocktakeStatus("DEFICIT");
            } else {
                vo.setStocktakeStatus("NORMAL");
            }

            vo.setHasMismatch(!vo.getStocktakeStatus().equals("NORMAL"));

            if (latestRecord != null) {
                vo.setDeviceName(latestRecord.getDeviceName());
                vo.setStocktakeTime(latestRecord.getTimestamp());
            }

            result.add(vo);
        }

        // 分页处理
        Page<InventoryComparisonVO> page = new Page<>(pageNum, pageSize);
        int total = result.size();
        page.setTotal(total);

        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        
        if (start < total) {
            page.setRecords(result.subList(start, end));
        } else {
            page.setRecords(new ArrayList<>());
        }

        return page;
    }

    public List<InventoryComparisonVO> getInventoryComparison() {
        List<InventoryComparisonVO> result = new ArrayList<>();

        List<WmsInventory> inventoryList = wmsInventoryMapper.selectList(new LambdaQueryWrapper<>());
        List<BaseGoods> goodsList = baseGoodsMapper.selectList(new LambdaQueryWrapper<>());
        List<BaseLocation> locationList = baseLocationMapper.selectList(new LambdaQueryWrapper<>());

        Map<Long, String> goodsNameMap = goodsList.stream()
                .collect(Collectors.toMap(BaseGoods::getId, BaseGoods::getGoodsName));
        Map<Long, String> goodsCodeMap = goodsList.stream()
                .collect(Collectors.toMap(BaseGoods::getId, BaseGoods::getGoodsCode));
        Map<Long, String> locationNameMap = locationList.stream()
                .collect(Collectors.toMap(BaseLocation::getId, BaseLocation::getLocationName));
        Map<Long, String> locationCodeMap = locationList.stream()
                .collect(Collectors.toMap(BaseLocation::getId, BaseLocation::getLocationCode));

        InventoryRecord latestRecord = getLatestRecord();
        Map<String, Integer> actualStockMap = new HashMap<>();
        boolean hasStocktakeData = false;

        if (latestRecord != null && latestRecord.getCargoSummary() != null) {
            try {
                Map<String, Object> summary = objectMapper.readValue(
                        latestRecord.getCargoSummary(),
                        new TypeReference<Map<String, Object>>() {}
                );
                for (Map.Entry<String, Object> entry : summary.entrySet()) {
                    Integer value = 0;
                    if (entry.getValue() instanceof Number) {
                        value = ((Number) entry.getValue()).intValue();
                    }
                    actualStockMap.put(entry.getKey(), value);
                }
                hasStocktakeData = !actualStockMap.isEmpty();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (WmsInventory inventory : inventoryList) {
            InventoryComparisonVO vo = new InventoryComparisonVO();
            vo.setInventoryId(inventory.getId());
            vo.setGoodsName(goodsNameMap.getOrDefault(inventory.getGoodsId(), ""));
            vo.setGoodsCode(goodsCodeMap.getOrDefault(inventory.getGoodsId(), ""));
            vo.setLocationName(locationNameMap.getOrDefault(inventory.getLocationId(), ""));
            vo.setLocationCode(locationCodeMap.getOrDefault(inventory.getLocationId(), ""));
            vo.setPlatformQuantity(inventory.getQuantity());

            String key = vo.getGoodsCode() + "-" + vo.getLocationCode();
            Integer actualQty = actualStockMap.getOrDefault(key, 0);
            vo.setActualQuantity(BigDecimal.valueOf(actualQty));

            // 只要实际库存和平台库存不一致，就显示盘盈或盘亏
            BigDecimal diff = vo.getActualQuantity().subtract(vo.getPlatformQuantity());
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                vo.setStocktakeStatus("SURPLUS");
            } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                vo.setStocktakeStatus("DEFICIT");
            } else {
                vo.setStocktakeStatus("NORMAL");
            }

            vo.setHasMismatch(!vo.getStocktakeStatus().equals("NORMAL"));

            if (latestRecord != null) {
                vo.setDeviceName(latestRecord.getDeviceName());
                vo.setStocktakeTime(latestRecord.getTimestamp());
            }

            result.add(vo);
        }

        return result;
    }
}
