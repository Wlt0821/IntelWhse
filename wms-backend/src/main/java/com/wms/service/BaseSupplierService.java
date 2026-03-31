package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.BaseSupplier;
import com.wms.mapper.BaseSupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BaseSupplierService {

    private final BaseSupplierMapper baseSupplierMapper;

    public Page<BaseSupplier> page(Integer pageNum, Integer pageSize, String supplierName, Integer status) {
        Page<BaseSupplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseSupplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(supplierName)) {
            wrapper.like(BaseSupplier::getSupplierName, supplierName);
        }
        if (status != null) {
            wrapper.eq(BaseSupplier::getStatus, status);
        }
        wrapper.orderByDesc(BaseSupplier::getCreateTime);
        return baseSupplierMapper.selectPage(page, wrapper);
    }

    public BaseSupplier getById(Long id) {
        return baseSupplierMapper.selectById(id);
    }

    public void save(BaseSupplier supplier) {
        baseSupplierMapper.insert(supplier);
    }

    public void update(BaseSupplier supplier) {
        baseSupplierMapper.updateById(supplier);
    }

    public void delete(Long id) {
        baseSupplierMapper.deleteById(id);
    }
}
