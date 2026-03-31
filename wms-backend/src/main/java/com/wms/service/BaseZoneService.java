package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.BaseZone;
import com.wms.mapper.BaseZoneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BaseZoneService {

    private final BaseZoneMapper baseZoneMapper;

    public Page<BaseZone> page(Integer pageNum, Integer pageSize, String zoneName, Integer status) {
        Page<BaseZone> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseZone> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(zoneName)) {
            wrapper.like(BaseZone::getZoneName, zoneName);
        }
        if (status != null) {
            wrapper.eq(BaseZone::getStatus, status);
        }
        wrapper.orderByDesc(BaseZone::getCreateTime);
        return baseZoneMapper.selectPage(page, wrapper);
    }

    public BaseZone getById(Long id) {
        return baseZoneMapper.selectById(id);
    }

    public void save(BaseZone zone) {
        baseZoneMapper.insert(zone);
    }

    public void update(BaseZone zone) {
        baseZoneMapper.updateById(zone);
    }

    public void delete(Long id) {
        baseZoneMapper.deleteById(id);
    }
}
