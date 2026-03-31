package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_inbound_task")
public class InboundTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskNo;

    private Long planId;

    private String taskType;

    private String status;

    private Long operatorId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
