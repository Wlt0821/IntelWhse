package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.BaseCustomer;
import com.wms.mapper.BaseCustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BaseCustomerService {

    private final BaseCustomerMapper baseCustomerMapper;

    public Page<BaseCustomer> page(Integer pageNum, Integer pageSize, String customerName, Integer status) {
        Page<BaseCustomer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseCustomer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(customerName)) {
            wrapper.like(BaseCustomer::getCustomerName, customerName);
        }
        if (status != null) {
            wrapper.eq(BaseCustomer::getStatus, status);
        }
        wrapper.orderByDesc(BaseCustomer::getCreateTime);
        return baseCustomerMapper.selectPage(page, wrapper);
    }

    public BaseCustomer getById(Long id) {
        return baseCustomerMapper.selectById(id);
    }

    public void save(BaseCustomer customer) {
        baseCustomerMapper.insert(customer);
    }

    public void update(BaseCustomer customer) {
        baseCustomerMapper.updateById(customer);
    }

    public void delete(Long id) {
        baseCustomerMapper.deleteById(id);
    }
}
