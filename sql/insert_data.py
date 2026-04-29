import pymysql
import time

# 数据库连接配置
config = {
    'host': 'localhost',
    'user': 'root',
    'password': 'root',
    'database': 'db_wms_db',
    'charset': 'utf8mb4'
}

try:
    # 连接数据库
    conn = pymysql.connect(**config)
    cursor = conn.cursor()
    
    print("[OK] Database connection successful!")
    
    # 1. 插入商品 - 金典有机奶
    sql_goods1 = """
    INSERT INTO base_goods (goods_code, goods_name, goods_type, unit, price, status, remark, create_time, update_time, deleted) 
    VALUES ('ML009', '金典有机奶250ml*12', '牛奶', '箱', 68.00, 1, NULL, NOW(), NOW(), 0)
    """
    cursor.execute(sql_goods1)
    goods_id1 = cursor.lastrowid
    print(f"[OK] 金典有机奶 inserted, ID: {goods_id1}")
    
    # 2. 插入商品 - 冰红茶
    sql_goods2 = """
    INSERT INTO base_goods (goods_code, goods_name, goods_type, unit, price, status, remark, create_time, update_time, deleted) 
    VALUES ('YS001', '统一冰红茶500ml*15', '饮料', '箱', 45.00, 1, NULL, NOW(), NOW(), 0)
    """
    cursor.execute(sql_goods2)
    goods_id2 = cursor.lastrowid
    print(f"[OK] 统一冰红茶 inserted, ID: {goods_id2}")
    
    # 3. 插入仓位 H1-K1
    sql_loc1 = """
    INSERT INTO base_location (location_code, location_name, zone_id, parent_id, location_level, location_type, capacity, status, remark, create_time, update_time, deleted) 
    VALUES ('H1-K1', 'H1-K1', 12, 0, 1, NULL, NULL, 1, NULL, NOW(), NOW(), 0)
    """
    cursor.execute(sql_loc1)
    loc_id1 = cursor.lastrowid
    print(f"[OK] Location H1-K1 inserted, ID: {loc_id1}")
    
    # 4. 插入仓位 H1-K2
    sql_loc2 = """
    INSERT INTO base_location (location_code, location_name, zone_id, parent_id, location_level, location_type, capacity, status, remark, create_time, update_time, deleted) 
    VALUES ('H1-K2', 'H1-K2', 12, 0, 1, NULL, NULL, 1, NULL, NOW(), NOW(), 0)
    """
    cursor.execute(sql_loc2)
    loc_id2 = cursor.lastrowid
    print(f"[OK] Location H1-K2 inserted, ID: {loc_id2}")
    
    # 5. 创建库存记录
    sql_inv1 = """
    INSERT INTO wms_inventory (goods_id, location_id, quantity, locked_quantity, available_quantity, min_warning_quantity, max_warning_quantity, warning_status, create_time, update_time)
    VALUES (%s, %s, 100.00, 0.00, 100.00, 10.00, 200.00, 0, NOW(), NOW())
    """
    cursor.execute(sql_inv1, (goods_id1, loc_id1))
    print(f"[OK] 金典有机奶 inventory record created at H1-K1")
    
    sql_inv2 = """
    INSERT INTO wms_inventory (goods_id, location_id, quantity, locked_quantity, available_quantity, min_warning_quantity, max_warning_quantity, warning_status, create_time, update_time)
    VALUES (%s, %s, 80.00, 0.00, 80.00, 10.00, 150.00, 0, NOW(), NOW())
    """
    cursor.execute(sql_inv2, (goods_id2, loc_id2))
    print(f"[OK] 统一冰红茶 inventory record created at H1-K2")
    
    # 提交事务
    conn.commit()
    print("\n[SUCCESS] All data inserted successfully!")
    
    cursor.close()
    conn.close()
    
except Exception as e:
    print(f"[ERROR] {e}")
    if 'conn' in locals():
        conn.rollback()
