package com.wms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;
import java.util.List;

@Data
public class InventoryUploadDTO {
    
    @JsonProperty("盘点批次")
    private String batchId;
    
    @JsonProperty("盘点时间")
    private String timestamp;
    
    @JsonProperty("设备名称")
    private String deviceName;
    
    @JsonProperty("画面货物总数")
    private Integer totalBoxes;
    
    @JsonProperty("识别二维码数")
    private Integer totalQrs;
    
    @JsonProperty("货物汇总数据")
    private Map<String, Object> cargoSummary;
    
    @JsonProperty("二维码详细数据")
    private List<Map<String, Object>> qrDetails;
}
