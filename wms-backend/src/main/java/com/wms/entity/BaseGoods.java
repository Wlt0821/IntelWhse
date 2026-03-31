package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("base_goods")
public class BaseGoods {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String goodsCode;

    private String goodsName;

    private String goodsType;

    private String unit;

    private BigDecimal price;

    private BigDecimal weight;

    private BigDecimal volume;

    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
