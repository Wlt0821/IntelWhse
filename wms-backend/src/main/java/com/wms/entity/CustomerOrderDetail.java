package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("wms_customer_order_detail")
public class CustomerOrderDetail {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long goodsId;

    private BigDecimal quantity;

    private BigDecimal price;

    private BigDecimal amount;

    private String remark;
}
