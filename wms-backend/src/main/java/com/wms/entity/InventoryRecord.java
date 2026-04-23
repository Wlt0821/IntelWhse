package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_inventory_records")
public class InventoryRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String batchId;
    
    private String deviceName;
    
    private LocalDateTime timestamp;
    
    private Integer totalBoxes;
    
    private Integer totalQrs;
    
    private String cargoSummary;
    
    private String qrDetails;
}
