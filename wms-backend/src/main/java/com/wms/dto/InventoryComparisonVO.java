package com.wms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryComparisonVO {

    private Long inventoryId;

    private String goodsName;

    private String goodsCode;

    private String locationName;

    private String locationCode;

    private BigDecimal platformQuantity;

    private BigDecimal actualQuantity;

    private String stocktakeStatus;

    private String deviceName;

    private LocalDateTime stocktakeTime;

    private Boolean hasMismatch;
}
