package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.BaseContainer;
import com.wms.mapper.BaseContainerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BaseContainerService {

    private final BaseContainerMapper baseContainerMapper;

    public Page<BaseContainer> page(Integer pageNum, Integer pageSize, String containerName, Integer status) {
        Page<BaseContainer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseContainer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(containerName)) {
            wrapper.like(BaseContainer::getContainerName, containerName);
        }
        if (status != null) {
            wrapper.eq(BaseContainer::getStatus, status);
        }
        wrapper.orderByDesc(BaseContainer::getCreateTime);
        return baseContainerMapper.selectPage(page, wrapper);
    }

    public BaseContainer getById(Long id) {
        return baseContainerMapper.selectById(id);
    }

    public void save(BaseContainer container) {
        baseContainerMapper.insert(container);
    }

    public void update(BaseContainer container) {
        baseContainerMapper.updateById(container);
    }

    public void delete(Long id) {
        baseContainerMapper.deleteById(id);
    }
}
