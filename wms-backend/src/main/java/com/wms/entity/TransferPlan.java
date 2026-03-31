package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wms_transfer_plan")
public class TransferPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String planNo;

    private Long sourceLocationId;

    private Long targetLocationId;

    private Long goodsId;

    private BigDecimal quantity;

    private String reason;

    private String status;

    private Long createBy;

    private Long approveBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
