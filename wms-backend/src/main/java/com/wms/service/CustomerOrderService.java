package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.CustomerOrder;
import com.wms.mapper.CustomerOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private final CustomerOrderMapper customerOrderMapper;

    public Page<CustomerOrder> page(Integer pageNum, Integer pageSize, String orderNo, String status) {
        Page<CustomerOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CustomerOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(CustomerOrder::getOrderNo, orderNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(CustomerOrder::getStatus, status);
        }
        wrapper.orderByDesc(CustomerOrder::getCreateTime);
        return customerOrderMapper.selectPage(page, wrapper);
    }

    public CustomerOrder getById(Long id) {
        return customerOrderMapper.selectById(id);
    }

    public void save(CustomerOrder order) {
        if (!StringUtils.hasText(order.getOrderNo())) {
            order.setOrderNo("CO" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(order.getStatus())) {
            order.setStatus("DRAFT");
        }
        customerOrderMapper.insert(order);
    }

    public void update(CustomerOrder order) {
        customerOrderMapper.updateById(order);
    }

    public void delete(Long id) {
        customerOrderMapper.deleteById(id);
    }
}
