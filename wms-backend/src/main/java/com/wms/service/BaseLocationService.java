package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.BaseLocation;
import com.wms.mapper.BaseLocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BaseLocationService {

    private final BaseLocationMapper baseLocationMapper;

    public Page<BaseLocation> page(Integer pageNum, Integer pageSize, String locationName, Integer status) {
        Page<BaseLocation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseLocation> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(locationName)) {
            wrapper.like(BaseLocation::getLocationName, locationName);
        }
        if (status != null) {
            wrapper.eq(BaseLocation::getStatus, status);
        }
        wrapper.orderByDesc(BaseLocation::getCreateTime);
        return baseLocationMapper.selectPage(page, wrapper);
    }

    public BaseLocation getById(Long id) {
        return baseLocationMapper.selectById(id);
    }

    public void save(BaseLocation location) {
        baseLocationMapper.insert(location);
    }

    public void update(BaseLocation location) {
        baseLocationMapper.updateById(location);
    }

    public void delete(Long id) {
        baseLocationMapper.deleteById(id);
    }
}
