package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.dto.CustomerOrderVO;
import com.wms.entity.BaseCustomer;
import com.wms.entity.CustomerOrder;
import com.wms.mapper.BaseCustomerMapper;
import com.wms.mapper.CustomerOrderMapper;
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
public class CustomerOrderService {

    private final CustomerOrderMapper customerOrderMapper;
    private final BaseCustomerMapper baseCustomerMapper;

    public Page<CustomerOrderVO> page(Integer pageNum, Integer pageSize, String orderNo, String status) {
        Page<CustomerOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CustomerOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(CustomerOrder::getOrderNo, orderNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(CustomerOrder::getStatus, status);
        }
        wrapper.orderByDesc(CustomerOrder::getCreateTime);
        Page<CustomerOrder> orderPage = customerOrderMapper.selectPage(page, wrapper);

        Page<CustomerOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());

        if (orderPage.getRecords().isEmpty()) {
            return voPage;
        }

        List<Long> customerIds = orderPage.getRecords().stream()
                .map(CustomerOrder::getCustomerId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> customerNameMap = baseCustomerMapper.selectBatchIds(customerIds).stream()
                .collect(Collectors.toMap(BaseCustomer::getId, BaseCustomer::getCustomerName, (a, b) -> a));

        List<CustomerOrderVO> voList = new ArrayList<>();
        for (CustomerOrder order : orderPage.getRecords()) {
            CustomerOrderVO vo = new CustomerOrderVO();
            BeanUtils.copyProperties(order, vo);
            vo.setCustomerName(customerNameMap.getOrDefault(order.getCustomerId(), "-"));
            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
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
