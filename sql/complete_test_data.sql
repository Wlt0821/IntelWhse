-- ==========================================
-- 完整测试数据 - 所有表不少于10条记录
-- ==========================================

USE wms_db;

-- 清空所有表数据（按依赖关系顺序）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sys_oper_log;
TRUNCATE TABLE sys_role_menu;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_menu;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE wms_check_detail;
TRUNCATE TABLE wms_check_plan;
TRUNCATE TABLE wms_replenishment_task;
TRUNCATE TABLE wms_replenishment_plan;
TRUNCATE TABLE wms_transfer_task;
TRUNCATE TABLE wms_transfer_plan;
TRUNCATE TABLE wms_packing;
TRUNCATE TABLE wms_picking_task;
TRUNCATE TABLE wms_outbound_plan_detail;
TRUNCATE TABLE wms_outbound_plan;
TRUNCATE TABLE wms_inbound_task;
TRUNCATE TABLE wms_inbound_plan_detail;
TRUNCATE TABLE wms_inbound_plan;
TRUNCATE TABLE wms_picking_plan;
TRUNCATE TABLE wms_customer_order_detail;
TRUNCATE TABLE wms_customer_order;
TRUNCATE TABLE wms_inventory;
TRUNCATE TABLE base_location;
TRUNCATE TABLE base_zone;
TRUNCATE TABLE base_container;
TRUNCATE TABLE base_goods;
TRUNCATE TABLE base_supplier;
TRUNCATE TABLE base_customer;
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 系统管理模块数据
-- ==========================================

-- 用户表 (10条)
INSERT INTO sys_user (username, password, real_name, phone, email, status, team) VALUES 
('admin', '123456', '系统管理员', '13800138000', 'admin@wms.com', 1, '管理组'),
('user01', '123456', '张三', '13800138001', 'zhangsan@wms.com', 1, '入库组'),
('user02', '123456', '李四', '13800138002', 'lisi@wms.com', 1, '入库组'),
('user03', '123456', '王五', '13800138003', 'wangwu@wms.com', 1, '出库组'),
('user04', '123456', '赵六', '13800138004', 'zhaoliu@wms.com', 1, '出库组'),
('user05', '123456', '钱七', '13800138005', 'qianqi@wms.com', 1, '盘点组'),
('user06', '123456', '孙八', '13800138006', 'sunba@wms.com', 1, '盘点组'),
('user07', '123456', '周九', '13800138007', 'zhoujiu@wms.com', 1, '补货组'),
('user08', '123456', '吴十', '13800138008', 'wushi@wms.com', 1, '补货组'),
('user09', '123456', '郑十一', '13800138009', 'zheng11@wms.com', 1, '移库组');

-- 角色表 (5条)
INSERT INTO sys_role (role_name, role_code, description, status) VALUES 
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1),
('信息录入员', 'DATA_ENTRY', '基础数据录入', 1),
('入库管理员', 'INBOUND_ADMIN', '入库管理权限', 1),
('出库管理员', 'OUTBOUND_ADMIN', '出库管理权限', 1),
('库存管理员', 'INVENTORY_ADMIN', '库存管理权限', 1);

-- 用户角色关联 (10条)
INSERT INTO sys_user_role (user_id, role_id) VALUES 
(1, 1),
(2, 2), (2, 3),
(3, 2), (3, 3),
(4, 2), (4, 4),
(5, 2), (5, 4),
(6, 2), (6, 5),
(7, 2), (7, 5),
(8, 2), (8, 5),
(9, 2), (9, 5),
(10, 2), (10, 5);

-- 菜单表 (15条)
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, status) VALUES 
(0, '首页', 'HOME', 1, '/home', 'Home/index', 'House', 1, 1),
(0, '基础数据', 'BASE_DATA', 1, '/base', NULL, 'Document', 2, 1),
(0, '订单管理', 'ORDER', 1, '/order', NULL, 'ShoppingCart', 3, 1),
(0, '入库管理', 'INBOUND', 1, '/inbound', NULL, 'UploadFilled', 4, 1),
(0, '出库管理', 'OUTBOUND', 1, '/outbound', NULL, 'Download', 5, 1),
(0, '补货管理', 'REPLENISH', 1, '/replenish', NULL, 'RefreshLeft', 6, 1),
(0, '移库管理', 'TRANSFER', 1, '/transfer', NULL, 'Right', 7, 1),
(0, '盘点管理', 'CHECK', 1, '/check', NULL, 'ListCheck', 8, 1),
(0, '数据查询', 'QUERY', 1, '/query', NULL, 'DataAnalysis', 9, 1),
(0, '系统管理', 'SYSTEM', 1, '/system', NULL, 'Setting', 10, 1),
(2, '客户设置', 'CUSTOMER', 2, '/base/customer', 'Base/Customer/index', 'User', 1, 1),
(2, '供应商设置', 'SUPPLIER', 2, '/base/supplier', 'Base/Supplier/index', 'OfficeBuilding', 2, 1),
(2, '容器设置', 'CONTAINER', 2, '/base/container', 'Base/Container/index', 'Box', 3, 1),
(2, '商品设置', 'GOODS', 2, '/base/goods', 'Base/Goods/index', 'Goods', 4, 1),
(2, '库区设置', 'ZONE', 2, '/base/zone', 'Base/Zone/index', 'Office', 5, 1),
(2, '仓位设置', 'LOCATION', 2, '/base/location', 'Base/Location/index', 'Location', 6, 1),
(3, '客户订单', 'CUSTOMER_ORDER', 2, '/order/customer', 'Order/CustomerOrder/index', 'Tickets', 1, 1),
(4, '入库计划', 'INBOUND_PLAN', 2, '/inbound/plan', 'Inbound/Plan/index', 'DocumentAdd', 1, 1),
(4, '入库作业', 'INBOUND_TASK', 2, '/inbound/task', 'Inbound/Task/index', 'Van', 2, 1),
(5, '拣货作业', 'PICKING_TASK', 2, '/outbound/picking', 'Outbound/Picking/index', 'Select', 1, 1),
(5, '装箱单管理', 'PACKING', 2, '/outbound/packing', 'Outbound/Packing/index', 'Box', 2, 1),
(10, '用户管理', 'USER', 2, '/system/user', 'System/User/index', 'User', 1, 1),
(10, '角色管理', 'ROLE', 2, '/system/role', 'System/Role/index', 'UserFilled', 2, 1);

-- 角色菜单关联 (30条)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18), (1, 19), (1, 20),
(1, 21), (1, 22), (1, 23),
(2, 1), (2, 2), (2, 9), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15), (2, 16),
(3, 1), (3, 4), (3, 18), (3, 19),
(4, 1), (4, 3), (4, 5), (4, 17), (4, 20), (4, 21),
(5, 1), (5, 6), (5, 7), (5, 8), (5, 9);

-- ==========================================
-- 基础数据管理模块数据
-- ==========================================

-- 客户表 (10条)
INSERT INTO base_customer (customer_code, customer_name, contact_person, contact_phone, address, email, status) VALUES 
('C001', '北京电子科技有限公司', '张经理', '13800000001', '北京市海淀区中关村大街1号', 'zhang@bjtech.com', 1),
('C002', '上海贸易集团', '李总监', '13800000002', '上海市浦东新区陆家嘴金融中心', 'li@shtrade.com', 1),
('C003', '广州零售连锁', '王店长', '13800000003', '广州市天河区天河路385号', 'wang@gzretail.com', 1),
('C004', '深圳物流配送', '赵主管', '13800000004', '深圳市南山区科技园南区', 'zhao@szlogistics.com', 1),
('C005', '杭州电商平台', '钱总', '13800000005', '杭州市余杭区文一西路969号', 'qian@hzecommerce.com', 1),
('C006', '成都食品批发', '孙经理', '13800000006', '成都市锦江区春熙路1号', 'sun@foodcd.com', 1),
('C007', '武汉数码商城', '周总', '13800000007', '武汉市江汉区中山大道888号', 'zhou@whdigital.com', 1),
('C008', '南京办公用品', '吴经理', '13800000008', '南京市鼓楼区汉中路2号', 'wu@njoffice.com', 1),
('C009', '西安服装公司', '郑总', '13800000009', '西安市雁塔区长安南路1号', 'zheng@xianfashion.com', 1),
('C010', '重庆家居建材', '王经理', '13800000010', '重庆市渝中区解放碑步行街88号', 'wang@cqhome.com', 1);

-- 供应商表 (10条)
INSERT INTO base_supplier (supplier_code, supplier_name, contact_person, contact_phone, address, email, status) VALUES 
('S001', '北京电子元器件厂', '刘总', '13900000001', '北京市亦庄经济开发区', 'liu@bjelec.com', 1),
('S002', '上海食品加工厂', '陈总', '13900000002', '上海市嘉定区安亭镇', 'chen@shfood.com', 1),
('S003', '广州服装制造厂', '周总', '13900000003', '广州市白云区人和镇', 'zhou@gzfashion.com', 1),
('S004', '深圳数码科技', '吴总', '13900000004', '深圳市宝安区沙井街道', 'wu@szdigital.com', 1),
('S005', '杭州家居用品', '郑总', '13900000005', '杭州市萧山区宁围街道', 'zheng@hzhome.com', 1),
('S006', '成都玩具生产', '孙总', '13900000006', '成都市温江区涌泉街道', 'sun@cdtoy.com', 1),
('S007', '武汉文体用品', '钱总', '13900000007', '武汉市东西湖区吴家山', 'qian@whsports.com', 1),
('S008', '南京化工原料', '李总', '13900000008', '南京市六合区化工园', 'li@njchem.com', 1),
('S009', '西安机械制造', '赵总', '13900000009', '西安市高陵区泾渭新城', 'zhao@xianmachine.com', 1),
('S010', '重庆包装材料', '王总', '13900000010', '重庆市江津区德感街道', 'wang@cqpack.com', 1);

-- 商品表 (20条)
INSERT INTO base_goods (goods_code, goods_name, goods_type, unit, price, weight, volume, status) VALUES 
('G001', '笔记本电脑', '电子产品', '台', 5999.00, 2.5, 0.05, 1),
('G002', '无线鼠标', '电子产品', '个', 129.00, 0.1, 0.005, 1),
('G003', '机械键盘', '电子产品', '个', 399.00, 1.2, 0.015, 1),
('G004', '27寸显示器', '电子产品', '台', 1899.00, 5.0, 0.08, 1),
('G005', '移动硬盘1TB', '电子产品', '个', 499.00, 0.3, 0.01, 1),
('G006', '蓝牙耳机', '电子产品', '副', 299.00, 0.08, 0.003, 1),
('G007', '平板电脑', '电子产品', '台', 3299.00, 0.8, 0.02, 1),
('G008', '智能手表', '电子产品', '块', 1599.00, 0.15, 0.004, 1),
('G009', '牛奶整箱', '食品', '箱', 65.00, 5.0, 0.03, 1),
('G010', '面包礼盒', '食品', '盒', 45.00, 2.0, 0.02, 1),
('G011', '饼干套装', '食品', '袋', 28.00, 1.5, 0.015, 1),
('G012', '巧克力礼盒', '食品', '盒', 88.00, 0.8, 0.01, 1),
('G013', '矿泉水整箱', '食品', '箱', 32.00, 12.0, 0.05, 1),
('G014', '果汁饮料', '食品', '瓶', 8.5, 1.0, 0.008, 1),
('G015', '休闲零食大礼包', '食品', '包', 128.00, 2.5, 0.025, 1),
('G016', 'T恤衫', '服装', '件', 89.00, 0.3, 0.01, 1),
('G017', '牛仔裤', '服装', '条', 199.00, 0.8, 0.015, 1),
('G018', '运动鞋', '服装', '双', 399.00, 1.2, 0.02, 1),
('G019', '羽绒服', '服装', '件', 699.00, 1.5, 0.03, 1),
('G020', '背包', '服装', '个', 259.00, 0.6, 0.025, 1);

-- 库区表 (5条)
INSERT INTO base_zone (zone_code, zone_name, zone_type, status) VALUES 
('Z001', '电子库区', 'NORMAL', 1),
('Z002', '食品库区', 'NORMAL', 1),
('Z003', '服装库区', 'NORMAL', 1),
('Z004', '冷藏库区', 'COLD', 1),
('Z005', '危险品库区', 'DANGER', 1);

-- 仓位表 (15条)
INSERT INTO base_location (location_code, location_name, zone_id, parent_id, location_level, capacity, status) VALUES 
('L001', '电子区-01-01', 1, 0, 1, 500.00, 1),
('L002', '电子区-01-02', 1, 0, 1, 500.00, 1),
('L003', '电子区-02-01', 1, 0, 1, 500.00, 1),
('L004', '电子区-02-02', 1, 0, 1, 500.00, 1),
('L005', '食品区-01-01', 2, 0, 1, 800.00, 1),
('L006', '食品区-01-02', 2, 0, 1, 800.00, 1),
('L007', '食品区-02-01', 2, 0, 1, 800.00, 1),
('L008', '服装区-01-01', 3, 0, 1, 600.00, 1),
('L009', '服装区-01-02', 3, 0, 1, 600.00, 1),
('L010', '服装区-02-01', 3, 0, 1, 600.00, 1),
('L011', '冷藏区-01-01', 4, 0, 1, 300.00, 1),
('L012', '冷藏区-01-02', 4, 0, 1, 300.00, 1),
('L013', '危险品区-01-01', 5, 0, 1, 200.00, 1),
('L014', '电子区-03-01', 1, 0, 1, 500.00, 1),
('L015', '食品区-03-01', 2, 0, 1, 800.00, 1);

-- 容器表 (10条)
INSERT INTO base_container (container_code, container_name, container_type, capacity, status) VALUES 
('CT001', '托盘A', 'PALLET', 100.00, 1),
('CT002', '托盘B', 'PALLET', 100.00, 1),
('CT003', '纸箱1号', 'BOX', 50.00, 1),
('CT004', '纸箱2号', 'BOX', 30.00, 1),
('CT005', '周转箱A', 'CRATE', 80.00, 1),
('CT006', '周转箱B', 'CRATE', 80.00, 1),
('CT007', '塑料桶', 'BARREL', 200.00, 1),
('CT008', '铁桶', 'BARREL', 200.00, 1),
('CT009', '集装箱1', 'CONTAINER', 1000.00, 1),
('CT010', '集装箱2', 'CONTAINER', 1000.00, 1);

-- ==========================================
-- 库存表 (20条，带预警值)
-- ==========================================

INSERT INTO wms_inventory (goods_id, location_id, quantity, locked_quantity, available_quantity, min_warning_quantity, max_warning_quantity, warning_status) VALUES 
(1, 1, 50, 5, 45, 20, 100, 0),
(2, 1, 200, 20, 180, 50, 300, 0),
(3, 2, 80, 8, 72, 30, 150, 0),
(4, 2, 25, 3, 22, 10, 80, 1),
(5, 3, 120, 12, 108, 40, 200, 0),
(6, 3, 15, 2, 13, 20, 100, 1),
(7, 4, 60, 6, 54, 25, 120, 0),
(8, 4, 180, 18, 162, 60, 250, 0),
(9, 5, 300, 30, 270, 100, 500, 0),
(10, 5, 450, 45, 405, 150, 600, 0),
(11, 6, 520, 52, 468, 180, 700, 2),
(12, 6, 280, 28, 252, 100, 450, 0),
(13, 7, 180, 18, 162, 60, 300, 0),
(14, 7, 35, 4, 31, 50, 200, 1),
(15, 8, 95, 10, 85, 30, 150, 0),
(16, 8, 120, 12, 108, 40, 200, 0),
(17, 9, 75, 8, 67, 25, 120, 0),
(18, 9, 210, 21, 189, 70, 350, 0),
(19, 10, 45, 5, 40, 20, 100, 0),
(20, 10, 140, 14, 126, 50, 230, 0);

-- ==========================================
-- 订单管理模块数据
-- ==========================================

-- 客户订单表 (10条)
INSERT INTO wms_customer_order (order_no, customer_id, order_type, order_date, expected_date, total_quantity, total_amount, status, create_by) VALUES 
('ORD20240301001', 1, 'NORMAL', '2024-03-01 10:00:00', '2024-03-10 18:00:00', 50, 299950.00, 'COMPLETED', 1),
('ORD20240301002', 2, 'URGENT', '2024-03-02 09:00:00', '2024-03-05 18:00:00', 30, 41970.00, 'PROCESSING', 1),
('ORD20240302001', 3, 'NORMAL', '2024-03-03 14:00:00', '2024-03-12 18:00:00', 80, 8640.00, 'CONFIRMED', 1),
('ORD20240302002', 4, 'RESERVED', '2024-03-04 11:00:00', '2024-03-20 18:00:00', 25, 47475.00, 'DRAFT', 1),
('ORD20240303001', 5, 'URGENT', '2024-03-05 08:30:00', '2024-03-08 18:00:00', 100, 8140.00, 'PROCESSING', 1),
('ORD20240303002', 6, 'NORMAL', '2024-03-06 15:00:00', '2024-03-15 18:00:00', 45, 3960.00, 'COMPLETED', 1),
('ORD20240304001', 7, 'NORMAL', '2024-03-07 10:30:00', '2024-03-17 18:00:00', 60, 23940.00, 'CONFIRMED', 1),
('ORD20240304002', 8, 'URGENT', '2024-03-08 09:00:00', '2024-03-11 18:00:00', 35, 13965.00, 'PROCESSING', 1),
('ORD20240305001', 9, 'NORMAL', '2024-03-09 14:30:00', '2024-03-19 18:00:00', 55, 31405.00, 'DRAFT', 1),
('ORD20240305002', 10, 'RESERVED', '2024-03-10 11:00:00', '2024-03-25 18:00:00', 70, 18130.00, 'CANCELLED', 1);

-- 客户订单明细表 (30条)
INSERT INTO wms_customer_order_detail (order_id, goods_id, quantity, price, amount) VALUES 
(1, 1, 5, 5999.00, 29995.00),
(1, 2, 20, 129.00, 2580.00),
(1, 3, 25, 399.00, 9975.00),
(2, 2, 30, 129.00, 3870.00),
(2, 5, 20, 499.00, 9980.00),
(2, 6, 50, 299.00, 14950.00),
(3, 9, 40, 65.00, 2600.00),
(3, 10, 25, 45.00, 1125.00),
(3, 11, 15, 28.00, 420.00),
(4, 4, 10, 1899.00, 18990.00),
(4, 7, 15, 3299.00, 49485.00),
(5, 9, 50, 65.00, 3250.00),
(5, 13, 30, 32.00, 960.00),
(5, 14, 20, 8.5, 170.00),
(6, 10, 45, 45.00, 2025.00),
(6, 11, 30, 28.00, 840.00),
(6, 12, 10, 88.00, 880.00),
(7, 16, 60, 89.00, 5340.00),
(7, 17, 40, 199.00, 7960.00),
(7, 18, 30, 399.00, 11970.00),
(8, 19, 35, 699.00, 24465.00),
(8, 20, 25, 259.00, 6475.00),
(8, 16, 20, 89.00, 1780.00),
(9, 4, 15, 1899.00, 28485.00),
(9, 8, 40, 1599.00, 63960.00),
(10, 17, 35, 199.00, 6965.00),
(10, 18, 20, 399.00, 7980.00),
(10, 19, 15, 699.00, 10485.00);

-- 拣选计划表 (10条)
INSERT INTO wms_picking_plan (plan_no, order_id, plan_type, status, create_by) VALUES 
('PK20240301001', 1, 'NORMAL', 'COMPLETED', 1),
('PK20240301002', 2, 'URGENT', 'PROCESSING', 1),
('PK20240302001', 3, 'NORMAL', 'PENDING', 1),
('PK20240302002', 5, 'URGENT', 'PROCESSING', 1),
('PK20240303001', 6, 'NORMAL', 'COMPLETED', 1),
('PK20240303002', 7, 'NORMAL', 'PENDING', 1),
('PK20240304001', 8, 'URGENT', 'PROCESSING', 1),
('PK20240304002', 9, 'NORMAL', 'PENDING', 1);

-- ==========================================
-- 入库管理模块数据
-- ==========================================

-- 入库计划表 (10条)
INSERT INTO wms_inbound_plan (plan_no, supplier_id, plan_type, plan_date, total_quantity, status, create_by) VALUES 
('IBP20240301001', 1, 'PURCHASE', '2024-03-01 09:00:00', 100, 'COMPLETED', 1),
('IBP20240301002', 2, 'PURCHASE', '2024-03-02 10:00:00', 80, 'PROCESSING', 1),
('IBP20240302001', 3, 'RETURN', '2024-03-03 14:00:00', 30, 'APPROVED', 1),
('IBP20240302002', 4, 'PURCHASE', '2024-03-04 08:30:00', 120, 'DRAFT', 1),
('IBP20240303001', 5, 'TRANSFER', '2024-03-05 11:00:00', 60, 'PENDING', 1),
('IBP20240303002', 6, 'PURCHASE', '2024-03-06 15:00:00', 90, 'COMPLETED', 1),
('IBP20240304001', 7, 'PURCHASE', '2024-03-07 09:30:00', 70, 'APPROVED', 1),
('IBP20240304002', 8, 'RETURN', '2024-03-08 10:00:00', 40, 'PROCESSING', 1),
('IBP20240305001', 9, 'PURCHASE', '2024-03-09 14:00:00', 85, 'DRAFT', 1),
('IBP20240305002', 10, 'PURCHASE', '2024-03-10 11:30:00', 110, 'CANCELLED', 1);

-- 入库计划明细表 (30条)
INSERT INTO wms_inbound_plan_detail (plan_id, goods_id, quantity, actual_quantity, location_id) VALUES 
(1, 1, 50, 50, 1),
(1, 2, 30, 30, 1),
(1, 3, 20, 20, 2),
(2, 4, 25, 0, 2),
(2, 5, 35, 0, 3),
(2, 6, 20, 0, 3),
(3, 9, 30, 0, 5),
(4, 7, 40, 0, 4),
(4, 8, 50, 0, 4),
(4, 16, 30, 0, 8),
(5, 17, 60, 0, 9),
(6, 10, 45, 45, 5),
(6, 11, 45, 45, 6),
(7, 18, 35, 0, 10),
(7, 19, 35, 0, 10),
(8, 12, 40, 0, 6),
(9, 20, 45, 0, 9),
(9, 14, 40, 0, 7),
(10, 13, 55, 0, 7),
(10, 15, 55, 0, 5);

-- 入库作业表 (10条)
INSERT INTO wms_inbound_task (task_no, plan_id, task_type, status, operator_id, start_time, end_time) VALUES 
('IBT20240301001', 1, 'UNLOAD', 'COMPLETED', 2, '2024-03-01 10:00:00', '2024-03-01 12:30:00'),
('IBT20240301002', 2, 'UNLOAD', 'PROCESSING', 3, '2024-03-02 11:00:00', NULL),
('IBT20240302001', 6, 'UNLOAD', 'COMPLETED', 2, '2024-03-06 16:00:00', '2024-03-06 18:30:00'),
('IBT20240302002', 7, 'UNLOAD', 'PENDING', 3, NULL, NULL),
('IBT20240303001', 8, 'UNLOAD', 'PROCESSING', 2, '2024-03-08 11:00:00', NULL);

-- ==========================================
-- 出库管理模块数据
-- ==========================================

-- 出库计划表 (10条)
INSERT INTO wms_outbound_plan (plan_no, customer_id, order_id, plan_type, plan_date, total_quantity, status, create_by) VALUES 
('OBP20240301001', 1, 1, 'NORMAL', '2024-03-01 14:00:00', 50, 'COMPLETED', 1),
('OBP20240301002', 2, 2, 'URGENT', '2024-03-02 10:00:00', 30, 'PROCESSING', 1),
('OBP20240302001', 3, 3, 'NORMAL', '2024-03-03 15:00:00', 80, 'APPROVED', 1),
('OBP20240302002', 5, 5, 'URGENT', '2024-03-05 09:00:00', 100, 'DRAFT', 1),
('OBP20240303001', 6, 6, 'NORMAL', '2024-03-06 14:00:00', 45, 'COMPLETED', 1),
('OBP20240303002', 7, 7, 'NORMAL', '2024-03-07 10:00:00', 60, 'APPROVED', 1),
('OBP20240304001', 8, 8, 'URGENT', '2024-03-08 11:00:00', 35, 'PROCESSING', 1),
('OBP20240304002', 9, 9, 'NORMAL', '2024-03-09 15:00:00', 55, 'DRAFT', 1),
('OBP20240305001', 4, NULL, 'NORMAL', '2024-03-10 10:00:00', 25, 'PENDING', 1),
('OBP20240305002', 10, 10, 'RESERVED', '2024-03-11 14:00:00', 70, 'CANCELLED', 1);

-- 出库计划明细表 (25条)
INSERT INTO wms_outbound_plan_detail (plan_id, goods_id, quantity, actual_quantity, location_id) VALUES 
(1, 1, 5, 5, 1),
(1, 2, 20, 20, 1),
(1, 3, 25, 25, 2),
(2, 2, 30, 0, 1),
(2, 5, 20, 0, 3),
(2, 6, 50, 0, 3),
(3, 9, 40, 0, 5),
(3, 10, 25, 0, 5),
(3, 11, 15, 0, 6),
(5, 10, 45, 45, 5),
(5, 11, 30, 30, 6),
(6, 16, 60, 0, 8),
(6, 17, 40, 0, 8),
(7, 19, 35, 0, 10),
(7, 20, 25, 0, 9),
(8, 4, 15, 0, 2),
(8, 8, 40, 0, 4),
(9, 7, 15, 0, 4),
(9, 14, 10, 0, 7),
(10, 17, 35, 0, 9),
(10, 18, 20, 0, 10);

-- 拣货任务表 (10条)
INSERT INTO wms_picking_task (task_no, plan_id, status, operator_id, start_time, end_time) VALUES 
('PKT20240301001', 1, 'COMPLETED', 4, '2024-03-01 15:00:00', '2024-03-01 17:30:00'),
('PKT20240301002', 2, 'PROCESSING', 5, '2024-03-02 11:00:00', NULL),
('PKT20240302001', 3, 'PENDING', 4, NULL, NULL),
('PKT20240302002', 5, 'COMPLETED', 5, '2024-03-06 15:00:00', '2024-03-06 17:00:00'),
('PKT20240303001', 6, 'PENDING', 4, NULL, NULL),
('PKT20240303002', 7, 'PROCESSING', 5, '2024-03-08 12:00:00', NULL),
('PKT20240304001', 8, 'PENDING', 4, NULL, NULL),
('PKT20240304002', 9, 'PENDING', 5, NULL, NULL);

-- 装箱单表 (10条)
INSERT INTO wms_packing (packing_no, container_id, plan_id, total_quantity, status) VALUES 
('PCK20240301001', 1, 1, 50, 'SHIPPED'),
('PCK20240301002', 3, 2, 30, 'PACKED'),
('PCK20240302001', 5, 3, 80, 'DRAFT'),
('PCK20240302002', 2, 5, 45, 'SHIPPED'),
('PCK20240303001', 4, 6, 60, 'PACKED'),
('PCK20240303002', 6, 7, 35, 'DRAFT'),
('PCK20240304001', 3, 8, 55, 'DRAFT'),
('PCK20240304002', 1, 9, 25, 'DRAFT'),
('PCK20240305001', 5, 10, 70, 'DRAFT');

-- ==========================================
-- 补货管理模块数据
-- ==========================================

-- 补货计划表 (10条)
INSERT INTO wms_replenishment_plan (plan_no, goods_id, source_location_id, target_location_id, quantity, trigger_reason, status) VALUES 
('RPL20240301001', 2, 2, 1, 100, 'LOW_STOCK', 'COMPLETED'),
('RPL20240301002', 9, 6, 5, 150, 'LOW_STOCK', 'PROCESSING'),
('RPL20240302001', 16, 9, 8, 80, 'MANUAL', 'APPROVED'),
('RPL20240302002', 3, 3, 1, 60, 'LOW_STOCK', 'PENDING'),
('RPL20240303001', 10, 7, 5, 120, 'MANUAL', 'COMPLETED'),
('RPL20240303002', 17, 10, 8, 90, 'LOW_STOCK', 'APPROVED'),
('RPL20240304001', 4, 4, 2, 40, 'MANUAL', 'PENDING'),
('RPL20240304002', 11, 6, 7, 70, 'LOW_STOCK', 'PROCESSING'),
('RPL20240305001', 18, 10, 9, 50, 'MANUAL', 'PENDING'),
('RPL20240305002', 12, 7, 6, 65, 'LOW_STOCK', 'PENDING');

-- 补货作业表 (10条)
INSERT INTO wms_replenishment_task (task_no, plan_id, status, operator_id) VALUES 
('RPLT20240301001', 1, 'COMPLETED', 8),
('RPLT20240301002', 2, 'PROCESSING', 9),
('RPLT20240302001', 3, 'PENDING', 8),
('RPLT20240302002', 4, 'PENDING', 9),
('RPLT20240303001', 5, 'COMPLETED', 8),
('RPLT20240303002', 6, 'PENDING', 9),
('RPLT20240304001', 7, 'PENDING', 8),
('RPLT20240304002', 8, 'PROCESSING', 9),
('RPLT20240305001', 9, 'PENDING', 8),
('RPLT20240305002', 10, 'PENDING', 9);

-- ==========================================
-- 移库管理模块数据
-- ==========================================

-- 移库计划表 (10条)
INSERT INTO wms_transfer_plan (plan_no, source_location_id, target_location_id, goods_id, quantity, reason, status, create_by) VALUES 
('TRF20240301001', 1, 2, 1, 30, 'OPTIMIZATION', 'COMPLETED', 1),
('TRF20240301002', 5, 6, 9, 100, 'SPACE_ADJUST', 'PROCESSING', 1),
('TRF20240302001', 8, 9, 16, 50, 'OPTIMIZATION', 'APPROVED', 1),
('TRF20240302002', 2, 3, 3, 40, 'SPACE_ADJUST', 'DRAFT', 1),
('TRF20240303001', 5, 7, 10, 80, 'OPTIMIZATION', 'COMPLETED', 1),
('TRF20240303002', 9, 10, 17, 60, 'SPACE_ADJUST', 'PENDING', 1),
('TRF20240304001', 2, 4, 4, 25, 'OPTIMIZATION', 'PENDING', 1),
('TRF20240304002', 6, 7, 11, 45, 'SPACE_ADJUST', 'PROCESSING', 1),
('TRF20240305001', 8, 10, 18, 35, 'OPTIMIZATION', 'DRAFT', 1),
('TRF20240305002', 1, 3, 2, 70, 'SPACE_ADJUST', 'CANCELLED', 1);

-- 移库作业表 (10条)
INSERT INTO wms_transfer_task (task_no, plan_id, status, operator_id) VALUES 
('TRFT20240301001', 1, 'COMPLETED', 10),
('TRFT20240301002', 2, 'PROCESSING', 10),
('TRFT20240302001', 3, 'PENDING', 10),
('TRFT20240302002', 4, 'PENDING', 10),
('TRFT20240303001', 5, 'COMPLETED', 10),
('TRFT20240303002', 6, 'PENDING', 10),
('TRFT20240304001', 7, 'PENDING', 10),
('TRFT20240304002', 8, 'PROCESSING', 10),
('TRFT20240305001', 9, 'PENDING', 10),
('TRFT20240305002', 10, 'PENDING', 10);

-- ==========================================
-- 盘点管理模块数据
-- ==========================================

-- 盘点计划表 (10条)
INSERT INTO wms_check_plan (plan_no, check_type, zone_id, location_id, goods_id, status, create_by) VALUES 
('CKP20240301001', 'FULL', NULL, NULL, NULL, 'COMPLETED', 1),
('CKP20240301002', 'PART', 1, NULL, NULL, 'PROCESSING', 1),
('CKP20240302001', 'PART', 2, NULL, NULL, 'PENDING', 1),
('CKP20240302002', 'PART', NULL, 1, NULL, 'DRAFT', 1),
('CKP20240303001', 'FULL', NULL, NULL, NULL, 'COMPLETED', 1),
('CKP20240303002', 'PART', 3, NULL, NULL, 'PENDING', 1),
('CKP20240304001', 'PART', NULL, 5, NULL, 'PROCESSING', 1),
('CKP20240304002', 'PART', NULL, NULL, 1, 'DRAFT', 1),
('CKP20240305001', 'FULL', NULL, NULL, NULL, 'PENDING', 1),
('CKP20240305002', 'PART', 2, 6, NULL, 'CANCELLED', 1);

-- 盘点明细表 (30条)
INSERT INTO wms_check_detail (plan_id, goods_id, location_id, system_quantity, actual_quantity, diff_quantity, diff_type) VALUES 
(1, 1, 1, 50, 52, 2, 'OVER'),
(1, 2, 1, 200, 195, -5, 'SHORT'),
(1, 3, 2, 80, 80, 0, 'NORMAL'),
(1, 4, 2, 25, 25, 0, 'NORMAL'),
(1, 5, 3, 120, 118, -2, 'SHORT'),
(2, 1, 1, 50, 50, 0, 'NORMAL'),
(2, 3, 2, 80, 0, 0, 'NORMAL'),
(2, 5, 3, 120, 0, 0, 'NORMAL'),
(2, 7, 4, 60, 0, 0, 'NORMAL'),
(5, 9, 5, 300, 305, 5, 'OVER'),
(5, 10, 5, 450, 450, 0, 'NORMAL'),
(5, 11, 6, 520, 520, 0, 'NORMAL'),
(5, 12, 6, 280, 275, -5, 'SHORT'),
(5, 16, 8, 95, 95, 0, 'NORMAL'),
(5, 17, 9, 120, 120, 0, 'NORMAL'),
(5, 18, 9, 75, 78, 3, 'OVER');

-- ==========================================
-- 操作日志数据
-- ==========================================

-- 操作日志表 (20条)
INSERT INTO sys_oper_log (user_id, username, operation, method, params, ip, status, oper_time) VALUES 
(1, 'admin', '用户登录', 'POST', '/api/auth/login', '192.168.1.1', 1, '2024-03-01 08:30:00'),
(1, 'admin', '创建入库计划', 'POST', '/api/inbound/plan', '192.168.1.1', 1, '2024-03-01 09:00:00'),
(2, 'user01', '用户登录', 'POST', '/api/auth/login', '192.168.1.2', 1, '2024-03-01 08:35:00'),
(2, 'user01', '执行入库作业', 'PUT', '/api/inbound/task', '192.168.1.2', 1, '2024-03-01 10:00:00'),
(1, 'admin', '创建客户订单', 'POST', '/api/order/customer', '192.168.1.1', 1, '2024-03-01 10:30:00'),
(4, 'user03', '用户登录', 'POST', '/api/auth/login', '192.168.1.4', 1, '2024-03-01 13:00:00'),
(4, 'user03', '创建拣货任务', 'POST', '/api/outbound/picking', '192.168.1.4', 1, '2024-03-01 14:00:00'),
(1, 'admin', '查询库存', 'GET', '/api/home/inventory/page', '192.168.1.1', 1, '2024-03-01 15:00:00'),
(5, 'user04', '用户登录', 'POST', '/api/auth/login', '192.168.1.5', 1, '2024-03-02 08:30:00'),
(5, 'user04', '执行拣货作业', 'PUT', '/api/outbound/picking', '192.168.1.5', 1, '2024-03-02 09:00:00'),
(1, 'admin', '创建补货计划', 'POST', '/api/replenish/plan', '192.168.1.1', 1, '2024-03-02 10:00:00'),
(3, 'user02', '用户登录', 'POST', '/api/auth/login', '192.168.1.3', 1, '2024-03-02 08:35:00'),
(3, 'user02', '执行入库作业', 'PUT', '/api/inbound/task', '192.168.1.3', 1, '2024-03-02 11:00:00'),
(1, 'admin', '创建移库计划', 'POST', '/api/transfer/plan', '192.168.1.1', 1, '2024-03-02 14:00:00'),
(6, 'user05', '用户登录', 'POST', '/api/auth/login', '192.168.1.6', 1, '2024-03-03 08:30:00'),
(6, 'user05', '创建盘点计划', 'POST', '/api/check/plan', '192.168.1.6', 1, '2024-03-03 09:00:00'),
(1, 'admin', '查询数据统计', 'GET', '/api/home/statistics', '192.168.1.1', 1, '2024-03-03 10:00:00'),
(7, 'user06', '用户登录', 'POST', '/api/auth/login', '192.168.1.7', 1, '2024-03-03 08:35:00'),
(7, 'user06', '执行盘点作业', 'PUT', '/api/check/plan', '192.168.1.7', 1, '2024-03-03 10:00:00'),
(1, 'admin', '导出库存报表', 'GET', '/api/query/data-query', '192.168.1.1', 1, '2024-03-03 15:00:00');

-- ==========================================
-- 数据导入完成
-- ==========================================

SELECT '测试数据导入完成！' AS message;
SELECT COUNT(*) AS user_count FROM sys_user;
SELECT COUNT(*) AS customer_count FROM base_customer;
SELECT COUNT(*) AS supplier_count FROM base_supplier;
SELECT COUNT(*) AS goods_count FROM base_goods;
SELECT COUNT(*) AS inventory_count FROM wms_inventory;
SELECT COUNT(*) AS order_count FROM wms_customer_order;
