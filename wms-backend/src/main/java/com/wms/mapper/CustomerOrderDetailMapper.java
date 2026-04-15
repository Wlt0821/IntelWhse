package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.CustomerOrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CustomerOrderDetailMapper extends BaseMapper<CustomerOrderDetail> {
    
    @Select("SELECT " +
            "DATE(o.order_date) as sale_date, " +
            "d.goods_id, " +
            "g.goods_name, " +
            "g.goods_code, " +
            "SUM(d.quantity) as total_quantity, " +
            "SUM(d.amount) as total_amount " +
            "FROM wms_customer_order o " +
            "LEFT JOIN wms_customer_order_detail d ON o.id = d.order_id " +
            "LEFT JOIN base_goods g ON d.goods_id = g.id " +
            "WHERE o.status = 'COMPLETED' " +
            "GROUP BY DATE(o.order_date), d.goods_id, g.goods_name, g.goods_code " +
            "ORDER BY sale_date DESC, d.goods_id")
    List<Map<String, Object>> getDailySales();
    
    @Select("SELECT " +
            "DATE(o.order_date) as sale_date, " +
            "d.goods_id, " +
            "g.goods_name, " +
            "g.goods_code, " +
            "SUM(d.quantity) as total_quantity, " +
            "SUM(d.amount) as total_amount " +
            "FROM wms_customer_order o " +
            "LEFT JOIN wms_customer_order_detail d ON o.id = d.order_id " +
            "LEFT JOIN base_goods g ON d.goods_id = g.id " +
            "WHERE o.status = 'COMPLETED' " +
            "AND DATE(o.order_date) >= #{startDate} " +
            "AND DATE(o.order_date) <= #{endDate} " +
            "GROUP BY DATE(o.order_date), d.goods_id, g.goods_name, g.goods_code " +
            "ORDER BY sale_date DESC, d.goods_id")
    List<Map<String, Object>> getDailySalesByDateRange(String startDate, String endDate);
}
