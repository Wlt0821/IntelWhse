package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.TransferPlan;
import com.wms.mapper.TransferPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TransferPlanService {

    private final TransferPlanMapper transferPlanMapper;

    public Page<TransferPlan> page(Integer pageNum, Integer pageSize, String planNo, String status) {
        Page<TransferPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TransferPlan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(planNo)) {
            wrapper.like(TransferPlan::getPlanNo, planNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(TransferPlan::getStatus, status);
        }
        wrapper.orderByDesc(TransferPlan::getCreateTime);
        return transferPlanMapper.selectPage(page, wrapper);
    }

    public TransferPlan getById(Long id) {
        return transferPlanMapper.selectById(id);
    }

    public void save(TransferPlan plan) {
        if (!StringUtils.hasText(plan.getPlanNo())) {
            plan.setPlanNo("TF" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(plan.getStatus())) {
            plan.setStatus("DRAFT");
        }
        transferPlanMapper.insert(plan);
    }

    public void update(TransferPlan plan) {
        transferPlanMapper.updateById(plan);
    }

    public void delete(Long id) {
        transferPlanMapper.deleteById(id);
    }
}
