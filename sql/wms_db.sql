-- 创建数据库
CREATE DATABASE IF NOT EXISTS wms_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE wms_db;

-- ==========================================
-- 系统管理模块表
-- ==========================================

-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    team VARCHAR(50) COMMENT '队伍',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 菜单表
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(50) COMMENT '菜单编码',
    menu_type TINYINT DEFAULT 1 COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(50) COMMENT '菜单图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 操作日志表
CREATE TABLE sys_oper_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(50) COMMENT '操作',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    status TINYINT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    error_msg VARCHAR(500) COMMENT '错误信息',
    oper_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user_id (user_id),
    INDEX idx_oper_time (oper_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ==========================================
-- 基础数据管理模块表
-- ==========================================

-- 客户表
CREATE TABLE base_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '客户ID',
    customer_code VARCHAR(50) NOT NULL UNIQUE COMMENT '客户编码',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(255) COMMENT '地址',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_customer_code (customer_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 供应商表
CREATE TABLE base_supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '供应商ID',
    supplier_code VARCHAR(50) NOT NULL UNIQUE COMMENT '供应商编码',
    supplier_name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(255) COMMENT '地址',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_supplier_code (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 容器表
CREATE TABLE base_container (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '容器ID',
    container_code VARCHAR(50) NOT NULL UNIQUE COMMENT '容器编码',
    container_name VARCHAR(100) NOT NULL COMMENT '容器名称',
    container_type VARCHAR(50) COMMENT '容器类型',
    capacity DECIMAL(10,2) COMMENT '容量',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_container_code (container_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='容器表';

-- 商品表
CREATE TABLE base_goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    goods_code VARCHAR(50) NOT NULL UNIQUE COMMENT '商品编码',
    goods_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    goods_type VARCHAR(50) COMMENT '商品类型',
    unit VARCHAR(20) COMMENT '单位',
    price DECIMAL(10,2) COMMENT '单价',
    weight DECIMAL(10,2) COMMENT '重量',
    volume DECIMAL(10,2) COMMENT '体积',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_goods_code (goods_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 库区表
CREATE TABLE base_zone (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库区ID',
    zone_code VARCHAR(50) NOT NULL UNIQUE COMMENT '库区编码',
    zone_name VARCHAR(100) NOT NULL COMMENT '库区名称',
    zone_type VARCHAR(50) COMMENT '库区类型',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_zone_code (zone_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库区表';

-- 仓位表
CREATE TABLE base_location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仓位ID',
    location_code VARCHAR(50) NOT NULL UNIQUE COMMENT '仓位编码',
    location_name VARCHAR(100) NOT NULL COMMENT '仓位名称',
    zone_id BIGINT NOT NULL COMMENT '库区ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父仓位ID',
    location_level INT DEFAULT 1 COMMENT '层级',
    location_type VARCHAR(50) COMMENT '仓位类型',
    capacity DECIMAL(10,2) COMMENT '容量',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_location_code (location_code),
    INDEX idx_zone_id (zone_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓位表';

-- ==========================================
-- 库存表
-- ==========================================

-- 库存表
CREATE TABLE wms_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    location_id BIGINT NOT NULL COMMENT '仓位ID',
    quantity DECIMAL(10,2) DEFAULT 0 COMMENT '库存数量',
    locked_quantity DECIMAL(10,2) DEFAULT 0 COMMENT '锁定数量',
    available_quantity DECIMAL(10,2) DEFAULT 0 COMMENT '可用数量',
    min_warning_quantity DECIMAL(10,2) DEFAULT 0 COMMENT '最小预警数量',
    max_warning_quantity DECIMAL(10,2) DEFAULT 999999 COMMENT '最大预警数量',
    warning_status TINYINT DEFAULT 0 COMMENT '预警状态：0-正常，1-低于最小值，2-高于最大值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_goods_location (goods_id, location_id),
    INDEX idx_goods_id (goods_id),
    INDEX idx_location_id (location_id),
    INDEX idx_warning_status (warning_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- ==========================================
-- 订单管理模块表
-- ==========================================

-- 客户订单表
CREATE TABLE wms_customer_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    order_type VARCHAR(20) COMMENT '订单类型',
    order_date DATETIME COMMENT '订单日期',
    expected_date DATETIME COMMENT '预计日期',
    total_quantity DECIMAL(10,2) COMMENT '总数量',
    total_amount DECIMAL(12,2) COMMENT '总金额',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，CONFIRMED-已确认，PROCESSING-处理中，COMPLETED-已完成，CANCELLED-已取消',
    remark VARCHAR(500) COMMENT '备注',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_order_no (order_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户订单表';

-- 客户订单明细表
CREATE TABLE wms_customer_order_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    quantity DECIMAL(10,2) NOT NULL COMMENT '数量',
    price DECIMAL(10,2) COMMENT '单价',
    amount DECIMAL(12,2) COMMENT '金额',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_order_id (order_id),
    INDEX idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户订单明细表';

-- 拣选计划表
CREATE TABLE wms_picking_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    plan_no VARCHAR(50) NOT NULL UNIQUE COMMENT '计划编号',
    order_id BIGINT COMMENT '订单ID',
    plan_type VARCHAR(20) COMMENT '计划类型',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_plan_no (plan_no),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拣选计划表';

-- ==========================================
-- 入库管理模块表
-- ==========================================

-- 入库计划表
CREATE TABLE wms_inbound_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    plan_no VARCHAR(50) NOT NULL UNIQUE COMMENT '计划编号',
    supplier_id BIGINT COMMENT '供应商ID',
    plan_type VARCHAR(20) COMMENT '计划类型',
    plan_date DATETIME COMMENT '计划日期',
    total_quantity DECIMAL(10,2) COMMENT '总数量',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING-待审核，APPROVED-已审核，PROCESSING-处理中，COMPLETED-已完成，CANCELLED-已取消',
    remark VARCHAR(500) COMMENT '备注',
    create_by BIGINT COMMENT '创建人',
    approve_by BIGINT COMMENT '审核人',
    approve_time DATETIME COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_plan_no (plan_no),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库计划表';

-- 入库计划明细表
CREATE TABLE wms_inbound_plan_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    plan_id BIGINT NOT NULL COMMENT '计划ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    quantity DECIMAL(10,2) NOT NULL COMMENT '计划数量',
    actual_quantity DECIMAL(10,2) DEFAULT 0 COMMENT '实际数量',
    location_id BIGINT COMMENT '目标仓位',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_plan_id (plan_id),
    INDEX idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库计划明细表';

-- 入库作业表
CREATE TABLE wms_inbound_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '作业ID',
    task_no VARCHAR(50) NOT NULL UNIQUE COMMENT '作业编号',
    plan_id BIGINT COMMENT '计划ID',
    task_type VARCHAR(20) COMMENT '作业类型',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待执行，PROCESSING-执行中，COMPLETED-已完成',
    operator_id BIGINT COMMENT '操作人',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_no (task_no),
    INDEX idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库作业表';

-- ==========================================
-- 出库管理模块表
-- ==========================================

-- 出库计划表
CREATE TABLE wms_outbound_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    plan_no VARCHAR(50) NOT NULL UNIQUE COMMENT '计划编号',
    customer_id BIGINT COMMENT '客户ID',
    order_id BIGINT COMMENT '订单ID',
    plan_type VARCHAR(20) COMMENT '计划类型',
    plan_date DATETIME COMMENT '计划日期',
    total_quantity DECIMAL(10,2) COMMENT '总数量',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING-待审核，APPROVED-已审核，PROCESSING-处理中，COMPLETED-已完成，CANCELLED-已取消',
    remark VARCHAR(500) COMMENT '备注',
    create_by BIGINT COMMENT '创建人',
    approve_by BIGINT COMMENT '审核人',
    approve_time DATETIME COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_plan_no (plan_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库计划表';

-- 出库计划明细表
CREATE TABLE wms_outbound_plan_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    plan_id BIGINT NOT NULL COMMENT '计划ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    quantity DECIMAL(10,2) NOT NULL COMMENT '计划数量',
    actual_quantity DECIMAL(10,2) DEFAULT 0 COMMENT '实际数量',
    location_id BIGINT COMMENT '出库仓位',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_plan_id (plan_id),
    INDEX idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库计划明细表';

-- 拣货计划表
CREATE TABLE wms_picking_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_no VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编号',
    plan_id BIGINT COMMENT '计划ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待拣货，PROCESSING-拣货中，COMPLETED-已完成',
    operator_id BIGINT COMMENT '操作人',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_no (task_no),
    INDEX idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拣货任务表';

-- 装箱单表
CREATE TABLE wms_packing (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '装箱ID',
    packing_no VARCHAR(50) NOT NULL UNIQUE COMMENT '装箱编号',
    container_id BIGINT COMMENT '容器ID',
    plan_id BIGINT COMMENT '计划ID',
    total_quantity DECIMAL(10,2) COMMENT '总数量',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PACKED-已装箱，SHIPPED-已出库',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_packing_no (packing_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装箱单表';

-- ==========================================
-- 补货管理模块表
-- ==========================================

-- 补货计划表
CREATE TABLE wms_replenishment_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    plan_no VARCHAR(50) NOT NULL UNIQUE COMMENT '计划编号',
    goods_id BIGINT COMMENT '商品ID',
    source_location_id BIGINT COMMENT '源仓位',
    target_location_id BIGINT COMMENT '目标仓位',
    quantity DECIMAL(10,2) COMMENT '补货数量',
    trigger_reason VARCHAR(100) COMMENT '触发原因',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，APPROVED-已审批，PROCESSING-处理中，COMPLETED-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_plan_no (plan_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补货计划表';

-- 补货作业表
CREATE TABLE wms_replenishment_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_no VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编号',
    plan_id BIGINT COMMENT '计划ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待执行，PROCESSING-执行中，COMPLETED-已完成',
    operator_id BIGINT COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_no (task_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补货作业表';

-- ==========================================
-- 移库管理模块表
-- ==========================================

-- 移库计划表
CREATE TABLE wms_transfer_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    plan_no VARCHAR(50) NOT NULL UNIQUE COMMENT '计划编号',
    source_location_id BIGINT NOT NULL COMMENT '源仓位',
    target_location_id BIGINT NOT NULL COMMENT '目标仓位',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    quantity DECIMAL(10,2) NOT NULL COMMENT '移库数量',
    reason VARCHAR(200) COMMENT '移库原因',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING-待审核，APPROVED-已审核，PROCESSING-处理中，COMPLETED-已完成',
    create_by BIGINT COMMENT '创建人',
    approve_by BIGINT COMMENT '审核人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_plan_no (plan_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移库计划表';

-- 移库作业表
CREATE TABLE wms_transfer_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_no VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编号',
    plan_id BIGINT COMMENT '计划ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待执行，PROCESSING-执行中，COMPLETED-已完成',
    operator_id BIGINT COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_no (task_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移库作业表';

-- ==========================================
-- 盘点管理模块表
-- ==========================================

-- 盘点计划表
CREATE TABLE wms_check_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    plan_no VARCHAR(50) NOT NULL UNIQUE COMMENT '计划编号',
    check_type VARCHAR(20) COMMENT '盘点类型：FULL-全盘，PART-部分',
    zone_id BIGINT COMMENT '库区ID',
    location_id BIGINT COMMENT '仓位ID',
    goods_id BIGINT COMMENT '商品ID',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING-待盘点，PROCESSING-盘点中，COMPLETED-已完成',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_plan_no (plan_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点计划表';

-- 盘点明细表
CREATE TABLE wms_check_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    plan_id BIGINT NOT NULL COMMENT '计划ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    location_id BIGINT NOT NULL COMMENT '仓位ID',
    system_quantity DECIMAL(10,2) COMMENT '系统数量',
    actual_quantity DECIMAL(10,2) COMMENT '实际数量',
    diff_quantity DECIMAL(10,2) COMMENT '差异数量',
    diff_type VARCHAR(20) COMMENT '差异类型：OVER-盘盈，SHORT-盘亏，NORMAL-正常',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细表';

-- ==========================================
-- 初始化数据
-- ==========================================

-- 初始化用户：admin/123456
INSERT INTO sys_user (username, password, real_name, phone, status, team) VALUES 
('admin', '123456', '系统管理员', '13800138000', 1, '01'),
('01', '123456', '信息录入员', '13800138001', 1, '01');

-- 初始化角色
INSERT INTO sys_role (role_name, role_code, description, status) VALUES 
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1),
('信息录入员', 'DATA_ENTRY', '基础数据录入', 1);

-- 初始化用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES 
(1, 1), (2, 2);

-- 初始化菜单
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
(3, '订单处理', 'ORDER_PROCESS', 2, '/order/process', 'Order/Process/index', 'Operation', 2, 1),
(3, '拣选计划', 'PICKING_PLAN', 2, '/order/picking', 'Order/Picking/index', 'List', 3, 1),
(4, '入库计划', 'INBOUND_PLAN', 2, '/inbound/plan', 'Inbound/Plan/index', 'DocumentAdd', 1, 1),
(4, '入库作业', 'INBOUND_TASK', 2, '/inbound/task', 'Inbound/Task/index', 'Van', 2, 1),
(4, '入库计划单列表', 'INBOUND_LIST', 2, '/inbound/list', 'Inbound/List/index', 'Document', 3, 1),
(5, '拣货计划', 'PICKING_TASK', 2, '/outbound/picking', 'Outbound/Picking/index', 'Select', 1, 1),
(5, '拣货作业', 'PICKING_WORK', 2, '/outbound/work', 'Outbound/Work/index', 'Van', 2, 1),
(5, '装箱单管理', 'PACKING', 2, '/outbound/packing', 'Outbound/Packing/index', 'Box', 3, 1),
(10, '用户管理', 'USER', 2, '/system/user', 'System/User/index', 'User', 1, 1),
(10, '角色管理', 'ROLE', 2, '/system/role', 'System/Role/index', 'UserFilled', 2, 1),
(10, '菜单管理', 'MENU', 2, '/system/menu', 'System/Menu/index', 'Menu', 3, 1),
(10, '操作日志', 'OPER_LOG', 2, '/system/log', 'System/Log/index', 'Document', 4, 1);

-- 初始化基础数据
INSERT INTO base_customer (customer_code, customer_name, contact_person, contact_phone, status) VALUES 
('C001', '北京客户A', '张经理', '13800000001', 1),
('C002', '上海客户B', '李经理', '13800000002', 1),
('C003', '广州客户C', '王经理', '13800000003', 1),
('C004', '深圳客户D', '赵经理', '13800000004', 1);

INSERT INTO base_supplier (supplier_code, supplier_name, contact_person, contact_phone, status) VALUES 
('S001', '北京供应商A', '刘总', '13900000001', 1);

INSERT INTO base_zone (zone_code, zone_name, zone_type, status) VALUES 
('Z001', '电子库区', 'NORMAL', 1),
('Z002', '食品库区', 'NORMAL', 1);

INSERT INTO base_location (location_code, location_name, zone_id, parent_id, location_level, status) VALUES 
('L001', '电子区-01-01', 1, 0, 1, 1),
('L002', '电子区-01-02', 1, 0, 1, 1),
('L003', '食品区-01-01', 2, 0, 1, 1);

INSERT INTO base_goods (goods_code, goods_name, goods_type, unit, price, status) VALUES 
('G001', '笔记本电脑', '电子产品', '台', 5000.00, 1),
('G002', '无线鼠标', '电子产品', '个', 100.00, 1),
('G003', '牛奶', '食品', '箱', 50.00, 1),
('G004', '面包', '食品', '袋', 10.00, 1);

INSERT INTO base_container (container_code, container_name, container_type, capacity, status) VALUES 
('CT001', '托盘A', 'PALLET', 100.00, 1),
('CT002', '纸箱B', 'BOX', 50.00, 1);
