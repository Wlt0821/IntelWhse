package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.ReplenishmentPlan;
import com.wms.mapper.ReplenishmentPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ReplenishmentPlanService {

    private final ReplenishmentPlanMapper replenishmentPlanMapper;

    public Page<ReplenishmentPlan> page(Integer pageNum, Integer pageSize, String planNo, String status) {
        Page<ReplenishmentPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReplenishmentPlan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(planNo)) {
            wrapper.like(ReplenishmentPlan::getPlanNo, planNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ReplenishmentPlan::getStatus, status);
        }
        wrapper.orderByDesc(ReplenishmentPlan::getCreateTime);
        return replenishmentPlanMapper.selectPage(page, wrapper);
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
