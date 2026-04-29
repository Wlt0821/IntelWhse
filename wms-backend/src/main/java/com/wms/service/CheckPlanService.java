package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.dto.CheckPlanVO;
import com.wms.entity.BaseGoods;
import com.wms.entity.BaseLocation;
import com.wms.entity.BaseZone;
import com.wms.entity.CheckPlan;
import com.wms.mapper.BaseGoodsMapper;
import com.wms.mapper.BaseLocationMapper;
import com.wms.mapper.BaseZoneMapper;
import com.wms.mapper.CheckPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckPlanService {

    private final CheckPlanMapper checkPlanMapper;
    private final BaseGoodsMapper baseGoodsMapper;
    private final BaseLocationMapper baseLocationMapper;
    private final BaseZoneMapper baseZoneMapper;

    public Page<CheckPlanVO> page(Integer pageNum, Integer pageSize, String planNo, String status) {
        Page<CheckPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CheckPlan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(planNo)) {
            wrapper.like(CheckPlan::getPlanNo, planNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(CheckPlan::getStatus, status);
        }
        wrapper.orderByDesc(CheckPlan::getCreateTime);
        Page<CheckPlan> planPage = checkPlanMapper.selectPage(page, wrapper);

        Page<CheckPlanVO> voPage = new Page<>(planPage.getCurrent(), planPage.getSize(), planPage.getTotal());

        if (planPage.getRecords().isEmpty()) {
            return voPage;
        }

        List<Long> goodsIds = planPage.getRecords().stream().map(CheckPlan::getGoodsId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> locationIds = planPage.getRecords().stream().map(CheckPlan::getLocationId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> zoneIds = planPage.getRecords().stream().map(CheckPlan::getZoneId).filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, String> goodsNameMap = goodsIds.isEmpty() ? new HashMap<>() : baseGoodsMapper.selectBatchIds(goodsIds).stream()
                .collect(Collectors.toMap(BaseGoods::getId, BaseGoods::getGoodsName, (a, b) -> a));
        Map<Long, String> locationNameMap = locationIds.isEmpty() ? new HashMap<>() : baseLocationMapper.selectBatchIds(locationIds).stream()
                .collect(Collectors.toMap(BaseLocation::getId, BaseLocation::getLocationName, (a, b) -> a));
        Map<Long, String> zoneNameMap = zoneIds.isEmpty() ? new HashMap<>() : baseZoneMapper.selectBatchIds(zoneIds).stream()
                .collect(Collectors.toMap(BaseZone::getId, BaseZone::getZoneName, (a, b) -> a));

        List<CheckPlanVO> voList = new ArrayList<>();
        for (CheckPlan plan : planPage.getRecords()) {
            CheckPlanVO vo = new CheckPlanVO();
            BeanUtils.copyProperties(plan, vo);
            Long goodsId = plan.getGoodsId();
            Long locationId = plan.getLocationId();
            Long zoneId = plan.getZoneId();
            vo.setGoodsName(goodsId != null && goodsNameMap.containsKey(goodsId) ? goodsNameMap.get(goodsId) : "-");
            vo.setLocationName(locationId != null && locationNameMap.containsKey(locationId) ? locationNameMap.get(locationId) : "-");
            vo.setZoneName(zoneId != null && zoneNameMap.containsKey(zoneId) ? zoneNameMap.get(zoneId) : "-");
            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }

    public CheckPlan getById(Long id) {
        return checkPlanMapper.selectById(id);
    }

    public void save(CheckPlan plan) {
        if (!StringUtils.hasText(plan.getPlanNo())) {
            plan.setPlanNo("CP" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(plan.getStatus())) {
            plan.setStatus("DRAFT");
        }
        checkPlanMapper.insert(plan);
    }

    public void update(CheckPlan plan) {
        checkPlanMapper.updateById(plan);
    }

    public void delete(Long id) {
        checkPlanMapper.deleteById(id);
    }
}
