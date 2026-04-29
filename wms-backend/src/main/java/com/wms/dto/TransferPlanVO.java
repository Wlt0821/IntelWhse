package com.wms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransferPlanVO {

    private Long id;
    private String planNo;
    private Long sourceLocationId;
    private String sourceLocationName;
    private Long targetLocationId;
    private String targetLocationName;
    private Long goodsId;
    private String goodsName;
    private BigDecimal quantity;
    private String reason;
    private String status;
    private Long createBy;
    private Long approveBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
