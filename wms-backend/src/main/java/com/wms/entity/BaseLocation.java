package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("base_location")
public class BaseLocation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String locationCode;

    private String locationName;

    private Long zoneId;

    private Long parentId;

    private Integer locationLevel;

    private String locationType;

    private BigDecimal capacity;

    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
