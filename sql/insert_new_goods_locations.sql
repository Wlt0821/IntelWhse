-- 新增商品和仓位
USE db_wms_db;

SET NAMES utf8mb4;

-- 1. 新增商品
-- 金典有机奶
INSERT INTO base_goods (goods_code, goods_name, goods_type, unit, price, status, remark, create_time, update_time, deleted) 
VALUES ('ML009', '金典有机奶250ml*12', '牛奶', '箱', 68.00, 1, NULL, NOW(), NOW(), 0);

-- 冰红茶
INSERT INTO base_goods (goods_code, goods_name, goods_type, unit, price, status, remark, create_time, update_time, deleted) 
VALUES ('YS001', '统一冰红茶500ml*15', '饮料', '箱', 45.00, 1, NULL, NOW(), NOW(), 0);

-- 2. 新增仓位
-- H1-K1
INSERT INTO base_location (location_code, location_name, zone_id, parent_id, location_level, location_type, capacity, status, remark, create_time, update_time, deleted) 
VALUES ('H1-K1', 'H1-K1', 12, 0, 1, NULL, NULL, 1, NULL, NOW(), NOW(), 0);

-- H1-K2
INSERT INTO base_location (location_code, location_name, zone_id, parent_id, location_level, location_type, capacity, status, remark, create_time, update_time, deleted) 
VALUES ('H1-K2', 'H1-K2', 12, 0, 1, NULL, NULL, 1, NULL, NOW(), NOW(), 0);

