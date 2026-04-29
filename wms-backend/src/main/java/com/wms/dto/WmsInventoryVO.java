package com.wms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WmsInventoryVO {
    private Long id;
    private Long goodsId;
    private String goodsCode;
    private String goodsName;
    private Long locationId;
    private String locationCode;
    private String locationName;
    private BigDecimal quantity;
    private BigDecimal lockedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal minWarningQuantity;
    private BigDecimal maxWarningQuantity;
    private Integer warningStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
