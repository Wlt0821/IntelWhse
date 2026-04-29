package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.dto.InboundPlanVO;
import com.wms.entity.BaseSupplier;
import com.wms.entity.InboundPlan;
import com.wms.mapper.BaseSupplierMapper;
import com.wms.mapper.InboundPlanMapper;
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
public class InboundPlanService {

    private final InboundPlanMapper inboundPlanMapper;
    private final BaseSupplierMapper baseSupplierMapper;

    public Page<InboundPlanVO> page(Integer pageNum, Integer pageSize, String planNo, String status) {
        Page<InboundPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InboundPlan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(planNo)) {
            wrapper.like(InboundPlan::getPlanNo, planNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(InboundPlan::getStatus, status);
        }
        wrapper.orderByDesc(InboundPlan::getCreateTime);
        Page<InboundPlan> planPage = inboundPlanMapper.selectPage(page, wrapper);

        Page<InboundPlanVO> voPage = new Page<>(planPage.getCurrent(), planPage.getSize(), planPage.getTotal());

        if (planPage.getRecords().isEmpty()) {
            return voPage;
        }

        List<Long> supplierIds = planPage.getRecords().stream()
                .map(InboundPlan::getSupplierId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> supplierNameMap = baseSupplierMapper.selectBatchIds(supplierIds).stream()
                .collect(Collectors.toMap(BaseSupplier::getId, BaseSupplier::getSupplierName, (a, b) -> a));

        List<InboundPlanVO> voList = new ArrayList<>();
        for (InboundPlan plan : planPage.getRecords()) {
            InboundPlanVO vo = new InboundPlanVO();
            BeanUtils.copyProperties(plan, vo);
            vo.setSupplierName(supplierNameMap.getOrDefault(plan.getSupplierId(), "-"));
            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
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
