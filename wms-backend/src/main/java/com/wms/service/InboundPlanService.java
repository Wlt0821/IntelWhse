package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.InboundPlan;
import com.wms.mapper.InboundPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class InboundPlanService {

    private final InboundPlanMapper inboundPlanMapper;

    public Page<InboundPlan> page(Integer pageNum, Integer pageSize, String planNo, String status) {
        Page<InboundPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InboundPlan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(planNo)) {
            wrapper.like(InboundPlan::getPlanNo, planNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(InboundPlan::getStatus, status);
        }
        wrapper.orderByDesc(InboundPlan::getCreateTime);
        return inboundPlanMapper.selectPage(page, wrapper);
    }

    public InboundPlan getById(Long id) {
        return inboundPlanMapper.selectById(id);
    }

    public void save(InboundPlan plan) {
        if (!StringUtils.hasText(plan.getPlanNo())) {
            plan.setPlanNo("IN" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(plan.getStatus())) {
            plan.setStatus("DRAFT");
        }
        inboundPlanMapper.insert(plan);
    }

    public void update(InboundPlan plan) {
        inboundPlanMapper.updateById(plan);
    }

    public void delete(Long id) {
        inboundPlanMapper.deleteById(id);
    }
}
