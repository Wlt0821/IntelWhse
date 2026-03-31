package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.CheckPlan;
import com.wms.mapper.CheckPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CheckPlanService {

    private final CheckPlanMapper checkPlanMapper;

    public Page<CheckPlan> page(Integer pageNum, Integer pageSize, String planNo, String status) {
        Page<CheckPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CheckPlan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(planNo)) {
            wrapper.like(CheckPlan::getPlanNo, planNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(CheckPlan::getStatus, status);
        }
        wrapper.orderByDesc(CheckPlan::getCreateTime);
        return checkPlanMapper.selectPage(page, wrapper);
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
