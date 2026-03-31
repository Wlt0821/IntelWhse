package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wms_replenishment_plan")
public class ReplenishmentPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String planNo;

    private Long goodsId;

    private Long sourceLocationId;

    private Long targetLocationId;

    private BigDecimal quantity;

    private String triggerReason;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
