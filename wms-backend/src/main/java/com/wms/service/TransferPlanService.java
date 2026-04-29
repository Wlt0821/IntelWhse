package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.dto.TransferPlanVO;
import com.wms.entity.BaseGoods;
import com.wms.entity.BaseLocation;
import com.wms.entity.TransferPlan;
import com.wms.mapper.BaseGoodsMapper;
import com.wms.mapper.BaseLocationMapper;
import com.wms.mapper.TransferPlanMapper;
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
public class TransferPlanService {

    private final TransferPlanMapper transferPlanMapper;
    private final BaseGoodsMapper baseGoodsMapper;
    private final BaseLocationMapper baseLocationMapper;

    public Page<TransferPlanVO> page(Integer pageNum, Integer pageSize, String planNo, String status) {
        Page<TransferPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TransferPlan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(planNo)) {
            wrapper.like(TransferPlan::getPlanNo, planNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(TransferPlan::getStatus, status);
        }
        wrapper.orderByDesc(TransferPlan::getCreateTime);
        Page<TransferPlan> planPage = transferPlanMapper.selectPage(page, wrapper);

        Page<TransferPlanVO> voPage = new Page<>(planPage.getCurrent(), planPage.getSize(), planPage.getTotal());

        if (planPage.getRecords().isEmpty()) {
            return voPage;
        }

        List<Long> goodsIds = planPage.getRecords().stream().map(TransferPlan::getGoodsId).distinct().collect(Collectors.toList());
        List<Long> locationIds = planPage.getRecords().stream()
                .flatMap(p -> List.of(p.getSourceLocationId(), p.getTargetLocationId()).stream())
                .filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, String> goodsNameMap = baseGoodsMapper.selectBatchIds(goodsIds).stream()
                .collect(Collectors.toMap(BaseGoods::getId, BaseGoods::getGoodsName, (a, b) -> a));
        Map<Long, String> locationNameMap = baseLocationMapper.selectBatchIds(locationIds).stream()
                .collect(Collectors.toMap(BaseLocation::getId, BaseLocation::getLocationName, (a, b) -> a));

        List<TransferPlanVO> voList = new ArrayList<>();
        for (TransferPlan plan : planPage.getRecords()) {
            TransferPlanVO vo = new TransferPlanVO();
            BeanUtils.copyProperties(plan, vo);
            vo.setGoodsName(goodsNameMap.getOrDefault(plan.getGoodsId(), "-"));
            vo.setSourceLocationName(locationNameMap.getOrDefault(plan.getSourceLocationId(), "-"));
            vo.setTargetLocationName(locationNameMap.getOrDefault(plan.getTargetLocationId(), "-"));
            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
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
