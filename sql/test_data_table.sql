-- ==========================================
-- 测试数据表
-- ==========================================

USE wms_db;

CREATE TABLE IF NOT EXISTS test_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    value DECIMAL(18, 2) NOT NULL COMMENT '数值',
    test_data TEXT COMMENT '测试数据',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试数据表';

-- 插入一些测试数据
INSERT INTO test_data (value, test_data) VALUES 
(10.5, '第一次测试'),
(15.2, '第二次测试'),
(8.9, '第三次测试');
