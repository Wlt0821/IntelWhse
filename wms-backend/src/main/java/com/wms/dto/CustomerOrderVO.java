package com.wms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerOrderVO {
    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String orderType;
    private LocalDateTime orderDate;
    private LocalDateTime expectedDate;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
