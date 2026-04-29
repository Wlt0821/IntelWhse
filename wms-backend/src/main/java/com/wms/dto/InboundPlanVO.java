package com.wms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InboundPlanVO {
    private Long id;
    private String planNo;
    private Long supplierId;
    private String supplierName;
    private String planType;
    private LocalDateTime planDate;
    private BigDecimal totalQuantity;
    private String status;
    private String remark;
    private Long createBy;
    private Long approveBy;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
