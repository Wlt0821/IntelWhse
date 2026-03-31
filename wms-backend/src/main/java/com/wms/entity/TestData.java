package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("test_data")
public class TestData {

    @TableId(type = IdType.AUTO)
    private Long id;

    private BigDecimal value;

    private String testData;

    private LocalDateTime createTime;
}
