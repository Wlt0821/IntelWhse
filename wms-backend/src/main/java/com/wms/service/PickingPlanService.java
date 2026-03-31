package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.PickingPlan;
import com.wms.mapper.PickingPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PickingPlanService {

    private final PickingPlanMapper pickingPlanMapper;

    public Page<PickingPlan> page(Integer pageNum, Integer pageSize, String planNo, String status) {
        Page<PickingPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PickingPlan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(planNo)) {
            wrapper.like(PickingPlan::getPlanNo, planNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PickingPlan::getStatus, status);
        }
        wrapper.orderByDesc(PickingPlan::getCreateTime);
        return pickingPlanMapper.selectPage(page, wrapper);
    }

    public PickingPlan getById(Long id) {
        return pickingPlanMapper.selectById(id);
    }

    public void save(PickingPlan plan) {
        if (!StringUtils.hasText(plan.getPlanNo())) {
            plan.setPlanNo("PP" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(plan.getStatus())) {
            plan.setStatus("PENDING");
        }
        pickingPlanMapper.insert(plan);
    }

    public void update(PickingPlan plan) {
        pickingPlanMapper.updateById(plan);
    }

    public void delete(Long id) {
        pickingPlanMapper.deleteById(id);
    }
}
