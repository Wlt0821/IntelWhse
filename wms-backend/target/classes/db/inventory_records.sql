-- 创建无人机实时盘点数据表 (适配 MySQL)
CREATE TABLE IF NOT EXISTS `wms_inventory_records` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `batch_id` VARCHAR(50) NOT NULL COMMENT '盘点批次号',
    `device_name` VARCHAR(50) DEFAULT NULL COMMENT '设备名称',
    `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '盘点时间',
    
    `total_boxes` INT DEFAULT 0 COMMENT '画面货物总数(识别框总数)',
    `total_qrs` INT DEFAULT 0 COMMENT '识别二维码数(扫码总数)',
    
    `cargo_summary` JSON DEFAULT NULL COMMENT '货物汇总数据(SKU汇总)',
    `qr_details` JSON DEFAULT NULL COMMENT '二维码详细数据(详细坐标数据)',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_id` (`batch_id`),
    KEY `idx_batch_id` (`batch_id`),
    KEY `idx_device_name` (`device_name`),
    KEY `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='无人机智能盘点记录表';

-- 插入测试数据
INSERT INTO `wms_inventory_records` (`batch_id`, `device_name`, `timestamp`, `total_boxes`, `total_qrs`, `cargo_summary`, `qr_details`) 
VALUES 
('CP2026041700001', '无人机-001', NOW(), 100, 85, '{"ML001": 50, "ML002": 30, "SN001": 20}', '[{"qr_code": "QR001", "x": 100, "y": 200}, {"qr_code": "QR002", "x": 150, "y": 250}]'),
('CP2026041700002', '无人机-002', NOW(), 80, 72, '{"ML001": 40, "ML002": 25, "SN001": 15}', '[{"qr_code": "QR003", "x": 120, "y": 180}]'),
('CP2026041700003', '无人机-001', NOW(), 120, 110, '{"ML001": 60, "ML002": 35, "SN001": 25}', '[{"qr_code": "QR004", "x": 110, "y": 210}]');
