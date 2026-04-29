package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.*;
import com.wms.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "首页数据")
@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final BaseCustomerMapper baseCustomerMapper;
    private final BaseSupplierMapper baseSupplierMapper;
    private final BaseGoodsMapper baseGoodsMapper;
    private final BaseLocationMapper baseLocationMapper;
    private final InboundPlanMapper inboundPlanMapper;
    private final CustomerOrderMapper customerOrderMapper;
    private final CustomerOrderDetailMapper customerOrderDetailMapper;
    private final WmsInventoryMapper wmsInventoryMapper;
    private final InboundTaskMapper inboundTaskMapper;
    private final PickingTaskMapper pickingTaskMapper;
    private final PackingMapper packingMapper;
    private final TransferPlanMapper transferPlanMapper;
    private final CheckPlanMapper checkPlanMapper;

    @Operation(summary = "获取首页统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> data = new HashMap<>();
        
        Long customerCount = baseCustomerMapper.selectCount(new LambdaQueryWrapper<>());
        Long supplierCount = baseSupplierMapper.selectCount(new LambdaQueryWrapper<>());
        
        // 业务数据：订单 + 入库
        Long orderCount = customerOrderMapper.selectCount(new LambdaQueryWrapper<>());
        Long inboundCount = inboundPlanMapper.selectCount(new LambdaQueryWrapper<>());
        Long businessDataCount = orderCount + inboundCount;
        
        // 作业数据：入库作业 + 拣货作业 + 装箱单 + 移库 + 盘点
        Long inboundTaskCount = inboundTaskMapper.selectCount(new LambdaQueryWrapper<>());
        Long pickingTaskCount = pickingTaskMapper.selectCount(new LambdaQueryWrapper<>());
        Long packingCount = packingMapper.selectCount(new LambdaQueryWrapper<>());
        Long transferCount = transferPlanMapper.selectCount(new LambdaQueryWrapper<>());
        Long checkCount = checkPlanMapper.selectCount(new LambdaQueryWrapper<>());
        Long workDataCount = inboundTaskCount + pickingTaskCount + packingCount + transferCount + checkCount;
        
        data.put("customerCount", customerCount);
        data.put("supplierCount", supplierCount);
        data.put("businessDataCount", businessDataCount);
        data.put("workDataCount", workDataCount);
        
        return Result.success(data);
    }

    @Operation(summary = "获取商品库存图表数据")
    @GetMapping("/inventory-chart")
    public Result<List<Map<String, Object>>> getInventoryChart() {
        List<BaseGoods> goodsList = baseGoodsMapper.selectList(new LambdaQueryWrapper<>());
        List<WmsInventory> inventoryList = wmsInventoryMapper.selectList(new LambdaQueryWrapper<>());
        
        Map<Long, BigDecimal> inventoryMap = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        WmsInventory::getGoodsId,
                        Collectors.reducing(BigDecimal.ZERO, WmsInventory::getQuantity, BigDecimal::add)
                ));
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (BaseGoods goods : goodsList) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", goods.getGoodsName());
            BigDecimal quantity = inventoryMap.getOrDefault(goods.getId(), BigDecimal.ZERO);
            item.put("value", quantity.intValue());
            result.add(item);
        }
        
        return Result.success(result);
    }

    @Operation(summary = "获取入库计划状态数据")
    @GetMapping("/inbound-chart")
    public Result<List<Map<String, Object>>> getInboundChart() {
        List<InboundPlan> plans = inboundPlanMapper.selectList(new LambdaQueryWrapper<>());
        
        Map<String, Long> statusMap = plans.stream()
                .collect(Collectors.groupingBy(InboundPlan::getStatus, Collectors.counting()));
        
        List<Map<String, Object>> result = new ArrayList<>();
        String[] statuses = {"COMPLETED", "PROCESSING", "APPROVED", "PENDING", "DRAFT"};
        String[] labels = {"已完成", "处理中", "已审核", "待审核", "草稿"};
        
        long total = plans.size();
        if (total > 0) {
            for (int i = 0; i < statuses.length; i++) {
                Long count = statusMap.getOrDefault(statuses[i], 0L);
                if (count > 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", labels[i]);
                    item.put("value", count);
                    item.put("percent", (int) (count * 100 / total));
                    result.add(item);
                }
            }
        }
        
        return Result.success(result);
    }

    @Operation(summary = "获取订单状态数据")
    @GetMapping("/order-chart")
    public Result<List<Map<String, Object>>> getOrderChart() {
        List<CustomerOrder> orders = customerOrderMapper.selectList(new LambdaQueryWrapper<>());
        
        Map<String, Long> statusMap = orders.stream()
                .collect(Collectors.groupingBy(CustomerOrder::getStatus, Collectors.counting()));
        
        List<Map<String, Object>> result = new ArrayList<>();
        String[] statuses = {"DRAFT", "CONFIRMED", "PROCESSING", "COMPLETED", "CANCELLED"};
        String[] labels = {"草稿", "已确认", "处理中", "已完成", "已取消"};
        
        long total = orders.size();
        if (total > 0) {
            for (int i = 0; i < statuses.length; i++) {
                Long count = statusMap.getOrDefault(statuses[i], 0L);
                if (count > 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", labels[i]);
                    item.put("value", count);
                    item.put("percent", (int) (count * 100 / total));
                    result.add(item);
                }
            }
        }
        
        return Result.success(result);
    }

    @Operation(summary = "获取库存列表")
    @GetMapping("/inventory/page")
    public Result<Page<Map<String, Object>>> getInventoryPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String goodsCode,
            @RequestParam(required = false) String locationCode) {
        
        Page<WmsInventory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WmsInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(WmsInventory::getCreateTime);
        
        Page<WmsInventory> inventoryPage = wmsInventoryMapper.selectPage(page, wrapper);
        
        List<BaseGoods> goodsList = baseGoodsMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, String> goodsNameMap = goodsList.stream()
                .collect(Collectors.toMap(BaseGoods::getId, BaseGoods::getGoodsName));
        Map<Long, String> goodsCodeMap = goodsList.stream()
                .collect(Collectors.toMap(BaseGoods::getId, BaseGoods::getGoodsCode));
        
        List<BaseLocation> locationList = baseLocationMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, String> locationNameMap = locationList.stream()
                .collect(Collectors.toMap(BaseLocation::getId, BaseLocation::getLocationName));
        Map<Long, String> locationCodeMap = locationList.stream()
                .collect(Collectors.toMap(BaseLocation::getId, BaseLocation::getLocationCode));
        
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize, inventoryPage.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        
        for (WmsInventory inventory : inventoryPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", inventory.getId());
            item.put("goodsId", inventory.getGoodsId());
            item.put("goodsName", goodsNameMap.getOrDefault(inventory.getGoodsId(), ""));
            item.put("goodsCode", goodsCodeMap.getOrDefault(inventory.getGoodsId(), ""));
            item.put("locationId", inventory.getLocationId());
            item.put("locationName", locationNameMap.getOrDefault(inventory.getLocationId(), ""));
            item.put("locationCode", locationCodeMap.getOrDefault(inventory.getLocationId(), ""));
            item.put("quantity", inventory.getQuantity());
            item.put("lockedQuantity", inventory.getLockedQuantity());
            item.put("availableQuantity", inventory.getAvailableQuantity());
            item.put("minWarningQuantity", inventory.getMinWarningQuantity());
            item.put("maxWarningQuantity", inventory.getMaxWarningQuantity());
            
            Integer warningStatus = 0;
            if (inventory.getMinWarningQuantity() != null && inventory.getQuantity().compareTo(inventory.getMinWarningQuantity()) < 0) {
                warningStatus = 1;
            } else if (inventory.getMaxWarningQuantity() != null && inventory.getQuantity().compareTo(inventory.getMaxWarningQuantity()) > 0) {
                warningStatus = 2;
            }
            item.put("warningStatus", warningStatus);
            
            item.put("createTime", inventory.getCreateTime());
            records.add(item);
        }
        
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    @Operation(summary = "获取所有库存数据")
    @GetMapping("/inventory/all")
    public Result<List<Map<String, Object>>> getAllInventory() {
        List<WmsInventory> inventoryList = wmsInventoryMapper.selectList(new LambdaQueryWrapper<>());
        
        List<BaseGoods> goodsList = baseGoodsMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, String> goodsNameMap = goodsList.stream()
                .collect(Collectors.toMap(BaseGoods::getId, BaseGoods::getGoodsName));
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (WmsInventory inventory : inventoryList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", inventory.getId());
            item.put("goodsId", inventory.getGoodsId());
            item.put("goodsName", goodsNameMap.getOrDefault(inventory.getGoodsId(), ""));
            item.put("locationId", inventory.getLocationId());
            item.put("quantity", inventory.getQuantity());
            item.put("lockedQuantity", inventory.getLockedQuantity());
            item.put("availableQuantity", inventory.getAvailableQuantity());
            item.put("minWarningQuantity", inventory.getMinWarningQuantity());
            item.put("maxWarningQuantity", inventory.getMaxWarningQuantity());
            
            Integer warningStatus = 0;
            if (inventory.getMinWarningQuantity() != null && inventory.getQuantity().compareTo(inventory.getMinWarningQuantity()) < 0) {
                warningStatus = 1;
            } else if (inventory.getMaxWarningQuantity() != null && inventory.getQuantity().compareTo(inventory.getMaxWarningQuantity()) > 0) {
                warningStatus = 2;
            }
            item.put("warningStatus", warningStatus);
            
            item.put("createTime", inventory.getCreateTime());
            result.add(item);
        }
        
        return Result.success(result);
    }
    
    @Operation(summary = "获取商品每日销量（全部数据）")
    @GetMapping("/daily-sales/all")
    public Result<List<Map<String, Object>>> getDailySalesAll() {
        List<Map<String, Object>> result = customerOrderDetailMapper.getDailySales();
        return Result.success(result);
    }
    
    @Operation(summary = "获取商品每日销量（按日期范围）")
    @GetMapping("/daily-sales/range")
    public Result<List<Map<String, Object>>> getDailySalesByRange(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        if (startDate == null || endDate == null) {
            List<Map<String, Object>> result = customerOrderDetailMapper.getDailySales();
            return Result.success(result);
        }
        
        List<Map<String, Object>> result = customerOrderDetailMapper.getDailySalesByDateRange(startDate, endDate);
        return Result.success(result);
    }
    
    @Operation(summary = "获取商品每日销量（分页）")
    @GetMapping("/daily-sales/page")
    public Result<Page<Map<String, Object>>> getDailySalesPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<Map<String, Object>> allData;
        if (startDate == null || endDate == null) {
            allData = customerOrderDetailMapper.getDailySales();
        } else {
            allData = customerOrderDetailMapper.getDailySalesByDateRange(startDate, endDate);
        }
        
        int total = allData.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        
        List<Map<String, Object>> pageData;
        if (start >= total) {
            pageData = new ArrayList<>();
        } else {
            pageData = allData.subList(start, end);
        }
        
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize, total);
        resultPage.setRecords(pageData);
        
        return Result.success(resultPage);
    }
}
