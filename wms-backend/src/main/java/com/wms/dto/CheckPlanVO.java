package com.wms.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckPlanVO {

    private Long id;
    private String planNo;
    private String checkType;
    private Long zoneId;
    private String zoneName;
    private Long locationId;
    private String locationName;
    private Long goodsId;
    private String goodsName;
    private String status;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
