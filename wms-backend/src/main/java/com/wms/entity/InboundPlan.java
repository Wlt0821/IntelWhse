package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wms_inbound_plan")
public class InboundPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String planNo;

    private Long supplierId;

    private String planType;

    private LocalDateTime planDate;

    private BigDecimal totalQuantity;

    private String status;

    private String remark;

    private Long createBy;

    private Long approveBy;

    private LocalDateTime approveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
