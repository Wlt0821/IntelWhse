package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_check_plan")
public class CheckPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String planNo;

    private String checkType;

    private Long zoneId;

    private Long locationId;

    private Long goodsId;

    private String status;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
