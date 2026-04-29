package com.wms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReplenishmentPlanVO {
    
    private Long id;
    private String planNo;
    private Long goodsId;
    private String goodsName;
    private Long sourceLocationId;
    private String sourceLocationName;
    private Long targetLocationId;
    private String targetLocationName;
    private BigDecimal quantity;
    private String triggerReason;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
