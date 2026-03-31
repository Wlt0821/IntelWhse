package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wms_packing")
public class Packing {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String packingNo;

    private Long containerId;

    private Long planId;

    private BigDecimal totalQuantity;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
