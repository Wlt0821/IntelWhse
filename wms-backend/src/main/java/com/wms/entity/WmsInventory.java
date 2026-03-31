package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wms_inventory")
public class WmsInventory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goodsId;

    private Long locationId;

    private BigDecimal quantity;

    private BigDecimal lockedQuantity;

    private BigDecimal availableQuantity;

    private BigDecimal minWarningQuantity;

    private BigDecimal maxWarningQuantity;

    private Integer warningStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
