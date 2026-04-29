package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.dto.ReplenishmentPlanVO;
import com.wms.entity.BaseGoods;
import com.wms.entity.BaseLocation;
import com.wms.entity.ReplenishmentPlan;
import com.wms.mapper.BaseGoodsMapper;
import com.wms.mapper.BaseLocationMapper;
import com.wms.mapper.ReplenishmentPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplenishmentPlanService {

    private final ReplenishmentPlanMapper replenishmentPlanMapper;
    private final BaseGoodsMapper baseGoodsMapper;
    private final BaseLocationMapper baseLocationMapper;

    public Page<ReplenishmentPlanVO> page(Integer pageNum, Integer pageSize, String planNo, String status) {
        Page<ReplenishmentPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReplenishmentPlan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(planNo)) {
            wrapper.like(ReplenishmentPlan::getPlanNo, planNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ReplenishmentPlan::getStatus, status);
        }
        wrapper.orderByDesc(ReplenishmentPlan::getCreateTime);
        Page<ReplenishmentPlan> planPage = replenishmentPlanMapper.selectPage(page, wrapper);

        Page<ReplenishmentPlanVO> voPage = new Page<>(planPage.getCurrent(), planPage.getSize(), planPage.getTotal());
        
        if (planPage.getRecords().isEmpty()) {
            return voPage;
        }

        List<Long> goodsIds = planPage.getRecords().stream().map(ReplenishmentPlan::getGoodsId).distinct().collect(Collectors.toList());
        List<Long> locationIds = planPage.getRecords().stream()
                .flatMap(p -> List.of(p.getSourceLocationId(), p.getTargetLocationId()).stream())
                .filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, String> goodsNameMap = baseGoodsMapper.selectBatchIds(goodsIds).stream()
                .collect(Collectors.toMap(BaseGoods::getId, BaseGoods::getGoodsName, (a, b) -> a));
        Map<Long, String> locationNameMap = baseLocationMapper.selectBatchIds(locationIds).stream()
                .collect(Collectors.toMap(BaseLocation::getId, BaseLocation::getLocationName, (a, b) -> a));

        List<ReplenishmentPlanVO> voList = new ArrayList<>();
        for (ReplenishmentPlan plan : planPage.getRecords()) {
            ReplenishmentPlanVO vo = new ReplenishmentPlanVO();
            BeanUtils.copyProperties(plan, vo);
            vo.setGoodsName(goodsNameMap.getOrDefault(plan.getGoodsId(), "-"));
            vo.setSourceLocationName(locationNameMap.getOrDefault(plan.getSourceLocationId(), "-"));
            vo.setTargetLocationName(locationNameMap.getOrDefault(plan.getTargetLocationId(), "-"));
            voList.add(vo);
        }
        
        voPage.setRecords(voList);
        return voPage;
    }

    public ReplenishmentPlan getById(Long id) {
        return replenishmentPlanMapper.selectById(id);
    }

    public void save(ReplenishmentPlan plan) {
        if (!StringUtils.hasText(plan.getPlanNo())) {
            plan.setPlanNo("RP" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(plan.getStatus())) {
            plan.setStatus("PENDING");
        }
        replenishmentPlanMapper.insert(plan);
    }

    public void update(ReplenishmentPlan plan) {
        replenishmentPlanMapper.updateById(plan);
    }

    public void delete(Long id) {
        replenishmentPlanMapper.deleteById(id);
    }
}
