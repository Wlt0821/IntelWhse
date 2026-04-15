import random
from datetime import datetime, timedelta
import pymysql
from typing import List, Dict, Any

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'root',
    'database': 'db_wms_db',
    'charset': 'utf8mb4'
}

# 商品数据（牛奶和零食）
GOODS_DATA = [
    # 牛奶类
    {'code': 'ML001', 'name': '特仑苏纯牛奶250ml*12', 'type': '牛奶', 'unit': '箱', 'price': 65.00, 'base_sales': 30},
    {'code': 'ML002', 'name': '伊利金典纯牛奶250ml*12', 'type': '牛奶', 'unit': '箱', 'price': 58.00, 'base_sales': 28},
    {'code': 'ML003', 'name': '蒙牛纯牛奶250ml*24', 'type': '牛奶', 'unit': '箱', 'price': 72.00, 'base_sales': 35},
    {'code': 'ML004', 'name': '光明鲜牛奶950ml', 'type': '牛奶', 'unit': '盒', 'price': 18.90, 'base_sales': 40, 'perishable': True},
    {'code': 'ML005', 'name': '安佳全脂牛奶1L*12', 'type': '牛奶', 'unit': '箱', 'price': 128.00, 'base_sales': 15},
    {'code': 'ML006', 'name': '德亚脱脂牛奶1L*12', 'type': '牛奶', 'unit': '箱', 'price': 115.00, 'base_sales': 12},
    {'code': 'ML007', 'name': '旺仔牛奶245ml*12', 'type': '牛奶', 'unit': '箱', 'price': 48.00, 'base_sales': 25, 'holiday_popular': True},
    {'code': 'ML008', 'name': '养乐多100ml*5', 'type': '牛奶', 'unit': '排', 'price': 12.50, 'base_sales': 50, 'perishable': True},
    
    # 零食类
    {'code': 'LS001', 'name': '乐事薯片原味75g', 'type': '零食', 'unit': '袋', 'price': 7.90, 'base_sales': 45, 'weekend_popular': True},
    {'code': 'LS002', 'name': '乐事薯片黄瓜味75g', 'type': '零食', 'unit': '袋', 'price': 7.90, 'base_sales': 35, 'weekend_popular': True},
    {'code': 'LS003', 'name': '可比克薯片番茄味60g', 'type': '零食', 'unit': '袋', 'price': 5.50, 'base_sales': 30},
    {'code': 'LS004', 'name': '奥利奥原味夹心饼干116g', 'type': '零食', 'unit': '盒', 'price': 8.90, 'base_sales': 38},
    {'code': 'LS005', 'name': '奥利奥巧克力味夹心饼干116g', 'type': '零食', 'unit': '盒', 'price': 8.90, 'base_sales': 32},
    {'code': 'LS006', 'name': '趣多多巧克力曲奇95g', 'type': '零食', 'unit': '袋', 'price': 9.90, 'base_sales': 28},
    {'code': 'LS007', 'name': '康师傅红烧牛肉面5包', 'type': '零食', 'unit': '袋', 'price': 12.50, 'base_sales': 25},
    {'code': 'LS008', 'name': '统一老坛酸菜牛肉面5包', 'type': '零食', 'unit': '袋', 'price': 13.50, 'base_sales': 22},
    {'code': 'LS009', 'name': '旺旺雪饼84g', 'type': '零食', 'unit': '袋', 'price': 6.50, 'base_sales': 40, 'holiday_popular': True},
    {'code': 'LS010', 'name': '旺旺仙贝52g', 'type': '零食', 'unit': '袋', 'price': 5.50, 'base_sales': 35, 'holiday_popular': True},
    {'code': 'LS011', 'name': '上好佳鲜虾片40g', 'type': '零食', 'unit': '袋', 'price': 4.50, 'base_sales': 28},
    {'code': 'LS012', 'name': '三只松鼠夏威夷果160g', 'type': '零食', 'unit': '袋', 'price': 29.90, 'base_sales': 15, 'seasonal': 'winter'},
]

# 节假日列表（2025年）
HOLIDAYS_2025 = [
    '2025-01-01',  # 元旦
    '2025-01-28', '2025-01-29', '2025-01-30', '2025-01-31',  # 春节
    '2025-02-01', '2025-02-02', '2025-02-03', '2025-02-04',
    '2025-04-04', '2025-04-05', '2025-04-06',  # 清明
    '2025-05-01', '2025-05-02', '2025-05-03', '2025-05-04', '2025-05-05',  # 五一
    '2025-06-22',  # 端午
    '2025-10-01', '2025-10-02', '2025-10-03', '2025-10-04', '2025-10-05',
    '2025-10-06', '2025-10-07', '2025-10-08',  # 国庆
]

# 促销日期（每月15号和最后一天）
PROMO_DAYS = [15, 28, 29, 30, 31]

# 周末销量系数
WEEKEND_MULTIPLIER = 1.8
# 节假日销量系数
HOLIDAY_MULTIPLIER = 2.5
# 促销日销量系数
PROMO_MULTIPLIER = 1.5

# 状态权重配置
ORDER_STATUS_WEIGHTS = {
    'DRAFT': 0.05,      # 草稿
    'CONFIRMED': 0.10,   # 已确认
    'PROCESSING': 0.15,  # 处理中
    'COMPLETED': 0.60,   # 已完成
    'CANCELLED': 0.10,   # 已取消
}

INBOUND_STATUS_WEIGHTS = {
    'DRAFT': 0.08,       # 草稿
    'PENDING': 0.12,     # 待审核
    'APPROVED': 0.15,    # 已审核
    'PROCESSING': 0.20,  # 处理中
    'COMPLETED': 0.40,   # 已完成
    'CANCELLED': 0.05,   # 已取消
}

OUTBOUND_STATUS_WEIGHTS = {
    'DRAFT': 0.08,       # 草稿
    'PENDING': 0.12,     # 待审核
    'APPROVED': 0.15,    # 已审核
    'PROCESSING': 0.20,  # 处理中
    'COMPLETED': 0.40,   # 已完成
    'CANCELLED': 0.05,   # 已取消
}

REPLENISHMENT_STATUS_WEIGHTS = {
    'PENDING': 0.20,      # 待处理
    'APPROVED': 0.20,     # 已审批
    'PROCESSING': 0.30,    # 处理中
    'COMPLETED': 0.30,     # 已完成
}

TASK_STATUS_WEIGHTS = {
    'PENDING': 0.25,      # 待执行
    'PROCESSING': 0.35,   # 执行中
    'COMPLETED': 0.40,     # 已完成
}

PACKING_STATUS_WEIGHTS = {
    'DRAFT': 0.20,         # 草稿
    'PACKED': 0.50,        # 已装箱
    'SHIPPED': 0.30,       # 已出库
}

TRANSFER_STATUS_WEIGHTS = {
    'DRAFT': 0.15,         # 草稿
    'PENDING': 0.20,       # 待审核
    'APPROVED': 0.25,      # 已审核
    'PROCESSING': 0.25,    # 处理中
    'COMPLETED': 0.15,     # 已完成
}

CHECK_STATUS_WEIGHTS = {
    'DRAFT': 0.15,         # 草稿
    'PENDING': 0.20,       # 待盘点
    'PROCESSING': 0.30,    # 盘点中
    'COMPLETED': 0.35,     # 已完成
}

class DataGenerator:
    def __init__(self):
        self.conn = None
        self.goods_ids = {}
        self.goods_map = {}
        self.location_ids = []
        self.supplier_id = None
        self.customer_ids = []
        self.user_ids = []
        self.container_ids = []
        self.inventory_data = {}
        self.order_counter = 0
        self.inbound_counter = 0
        self.picking_counter = 0
        self.replenishment_counter = 0
        self.inbound_task_counter = 0
        self.picking_task_counter = 0
        self.packing_counter = 0
        self.transfer_counter = 0
        self.check_counter = 0
        
    def connect(self):
        self.conn = pymysql.connect(**DB_CONFIG)
        print("数据库连接成功")
        
    def disconnect(self):
        if self.conn:
            self.conn.close()
            print("数据库连接已关闭")
            
    def clear_old_data(self):
        """清除旧数据"""
        with self.conn.cursor() as cursor:
            cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
            cursor.execute("DELETE FROM wms_check_detail")
            cursor.execute("DELETE FROM wms_check_plan")
            cursor.execute("DELETE FROM wms_transfer_task")
            cursor.execute("DELETE FROM wms_transfer_plan")
            cursor.execute("DELETE FROM wms_replenishment_task")
            cursor.execute("DELETE FROM wms_replenishment_plan")
            cursor.execute("DELETE FROM wms_packing")
            cursor.execute("DELETE FROM wms_picking_task")
            cursor.execute("DELETE FROM wms_outbound_plan_detail")
            cursor.execute("DELETE FROM wms_outbound_plan")
            cursor.execute("DELETE FROM wms_inbound_task")
            cursor.execute("DELETE FROM wms_inbound_plan_detail")
            cursor.execute("DELETE FROM wms_inbound_plan")
            cursor.execute("DELETE FROM wms_picking_plan")
            cursor.execute("DELETE FROM wms_customer_order_detail")
            cursor.execute("DELETE FROM wms_customer_order")
            cursor.execute("DELETE FROM wms_inventory")
            cursor.execute("DELETE FROM base_location")
            cursor.execute("DELETE FROM base_zone")
            cursor.execute("DELETE FROM base_goods WHERE goods_code NOT LIKE 'ML%' AND goods_code NOT LIKE 'LS%'")
            cursor.execute("DELETE FROM base_goods WHERE goods_code LIKE 'ML%' OR goods_code LIKE 'LS%'")
            cursor.execute("SET FOREIGN_KEY_CHECKS = 1")
        self.conn.commit()
        print("旧数据已清除")
        
    def get_random_status(self, weight_dict):
        """根据权重随机获取状态"""
        statuses = list(weight_dict.keys())
        weights = list(weight_dict.values())
        return random.choices(statuses, weights=weights, k=1)[0]
        
    def create_basic_data(self):
        """创建基础数据"""
        with self.conn.cursor() as cursor:
            # 获取用户ID
            cursor.execute("SELECT id FROM sys_user WHERE status = 1")
            self.user_ids = [row[0] for row in cursor.fetchall()]
            
            # 获取容器
            cursor.execute("SELECT id FROM base_container LIMIT 3")
            results = cursor.fetchall()
            if results:
                self.container_ids = [row[0] for row in results]
            else:
                for i in range(3):
                    cursor.execute("""
                        INSERT INTO base_container (container_code, container_name, container_type, capacity, status)
                        VALUES (%s, %s, 'BOX', 100.00, 1)
                    """, (f'CT_FOOD_0{i+1}', f'食品容器{i+1}'))
                    self.container_ids.append(cursor.lastrowid)
            
            # 创建库区
            cursor.execute("""
                INSERT INTO base_zone (zone_code, zone_name, zone_type, status) 
                VALUES ('Z_FOOD', '食品零食库区', 'NORMAL', 1)
            """)
            zone_id = cursor.lastrowid
            
            # 创建仓位
            locations = [
                ('L_FOOD_01', '食品区-01-01', zone_id),
                ('L_FOOD_02', '食品区-01-02', zone_id),
                ('L_FOOD_03', '食品区-01-03', zone_id),
                ('L_FOOD_04', '食品区-02-01', zone_id),
                ('L_FOOD_05', '食品区-02-02', zone_id),
            ]
            for code, name, zid in locations:
                cursor.execute("""
                    INSERT INTO base_location (location_code, location_name, zone_id, parent_id, location_level, status)
                    VALUES (%s, %s, %s, 0, 1, 1)
                """, (code, name, zid))
                self.location_ids.append(cursor.lastrowid)
            
            # 创建商品
            for goods in GOODS_DATA:
                cursor.execute("""
                    INSERT INTO base_goods (goods_code, goods_name, goods_type, unit, price, status)
                    VALUES (%s, %s, %s, %s, %s, 1)
                """, (goods['code'], goods['name'], goods['type'], goods['unit'], goods['price']))
                goods_id = cursor.lastrowid
                self.goods_ids[goods['code']] = goods_id
                self.goods_map[goods_id] = goods
            
            # 确保有供应商
            cursor.execute("SELECT id FROM base_supplier LIMIT 1")
            result = cursor.fetchone()
            if result:
                self.supplier_id = result[0]
            else:
                cursor.execute("""
                    INSERT INTO base_supplier (supplier_code, supplier_name, contact_person, contact_phone, status)
                    VALUES ('S_FOOD', '食品供应商', '王经理', '13800000001', 1)
                """)
                self.supplier_id = cursor.lastrowid
            
            # 确保有客户
            cursor.execute("SELECT id FROM base_customer LIMIT 3")
            results = cursor.fetchall()
            if results:
                self.customer_ids = [r[0] for r in results]
            else:
                for i in range(3):
                    cursor.execute("""
                        INSERT INTO base_customer (customer_code, customer_name, contact_person, contact_phone, status)
                        VALUES (%s, %s, %s, %s, 1)
                    """, (f'C_FOOD_0{i+1}', f'食品客户{i+1}', f'张经理{i+1}', f'1380000000{i+1}'))
                    self.customer_ids.append(cursor.lastrowid)
            
        self.conn.commit()
        print("基础数据创建完成")
        
    def get_season_factor(self, date: datetime, goods: dict) -> float:
        """获取季节因素"""
        month = date.month
        goods_type = goods.get('type', '')
        
        # 夏季（6-8月）：牛奶销量高
        if 6 <= month <= 8:
            if goods_type == '牛奶':
                return 1.3
            elif goods_type == '零食':
                return 0.9
        
        # 冬季（12-2月）：零食销量高
        if month in [12, 1, 2]:
            if goods_type == '零食':
                return 1.2
            elif goods_type == '牛奶':
                return 0.95
        
        # 季节商品
        if goods.get('seasonal') == 'winter' and month in [11, 12, 1, 2]:
            return 1.4
        
        if goods.get('seasonal') == 'summer' and month in [6, 7, 8]:
            return 1.4
        
        return 1.0
        
    def generate_daily_sales(self, date: datetime, goods_id: int) -> int:
        """生成日销量（考虑节假日、周末、促销、季节等因素）"""
        date_str = date.strftime('%Y-%m-%d')
        goods = self.goods_map.get(goods_id, {})
        
        # 基础销量
        base_sales = goods.get('base_sales', 20)
        
        # 周末加成
        multiplier = 1.0
        if date.weekday() >= 5:
            multiplier *= WEEKEND_MULTIPLIER
            if goods.get('weekend_popular'):
                multiplier *= 1.2
            
        # 节假日加成
        if date_str in HOLIDAYS_2025:
            multiplier *= HOLIDAY_MULTIPLIER
            if goods.get('holiday_popular'):
                multiplier *= 1.3
        
        # 促销日加成
        if date.day in PROMO_DAYS:
            multiplier *= PROMO_MULTIPLIER
        
        # 季节因素
        multiplier *= self.get_season_factor(date, goods)
        
        # 易腐商品：周末销量更高
        if goods.get('perishable') and date.weekday() >= 4:
            multiplier *= 1.2
        
        # 随机波动 ±15%
        variation = random.uniform(0.85, 1.15)
        multiplier *= variation
        
        sales = int(base_sales * multiplier)
        
        return max(1, sales)
        
    def create_inventory(self):
        """创建初始库存"""
        with self.conn.cursor() as cursor:
            for goods_code, goods_id in self.goods_ids.items():
                goods = self.goods_map.get(goods_id, {})
                location_id = random.choice(self.location_ids)
                
                # 根据商品特性设置初始库存
                base_sales = goods.get('base_sales', 20)
                
                # 易腐商品库存较少
                if goods.get('perishable'):
                    base_qty = random.randint(int(base_sales * 2), int(base_sales * 4))
                    min_warning = int(base_sales * 1.5)
                    max_warning = int(base_sales * 6)
                else:
                    base_qty = random.randint(int(base_sales * 5), int(base_sales * 10))
                    min_warning = int(base_sales * 2.5)
                    max_warning = int(base_sales * 15)
                
                self.inventory_data[goods_id] = {
                    'quantity': base_qty,
                    'min_warning': min_warning,
                    'max_warning': max_warning,
                    'location_id': location_id
                }
                
                cursor.execute("""
                    INSERT INTO wms_inventory 
                    (goods_id, location_id, quantity, locked_quantity, available_quantity, 
                     min_warning_quantity, max_warning_quantity, warning_status)
                    VALUES (%s, %s, %s, 0, %s, %s, %s, 0)
                """, (goods_id, location_id, base_qty, base_qty, min_warning, max_warning))
                
        self.conn.commit()
        print("初始库存创建完成")
        
    def create_inbound_task(self, plan_id, operator_id, task_date):
        """创建入库作业"""
        self.inbound_task_counter += 1
        task_no = f"IT{task_date.strftime('%Y%m%d')}{self.inbound_task_counter:06d}"
        task_status = self.get_random_status(TASK_STATUS_WEIGHTS)
        start_time = task_date if task_status in ['PROCESSING', 'COMPLETED'] else None
        end_time = task_date if task_status == 'COMPLETED' else None
        
        with self.conn.cursor() as cursor:
            cursor.execute("""
                INSERT INTO wms_inbound_task
                (task_no, plan_id, task_type, status, operator_id, start_time, end_time, create_time)
                VALUES (%s, %s, 'INBOUND', %s, %s, %s, %s, %s)
            """, (task_no, plan_id, task_status, operator_id, start_time, end_time, task_date))
            return cursor.lastrowid
        
    def create_picking_task(self, plan_id, operator_id, task_date):
        """创建拣货作业"""
        self.picking_task_counter += 1
        task_no = f"PT{task_date.strftime('%Y%m%d')}{self.picking_task_counter:06d}"
        task_status = self.get_random_status(TASK_STATUS_WEIGHTS)
        start_time = task_date if task_status in ['PROCESSING', 'COMPLETED'] else None
        end_time = task_date if task_status == 'COMPLETED' else None
        
        with self.conn.cursor() as cursor:
            cursor.execute("""
                INSERT INTO wms_picking_task
                (task_no, plan_id, status, operator_id, start_time, end_time, create_time)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
            """, (task_no, plan_id, task_status, operator_id, start_time, end_time, task_date))
            return cursor.lastrowid
        
    def create_packing(self, plan_id, container_id, task_date):
        """创建装箱单"""
        self.packing_counter += 1
        packing_no = f"PK{task_date.strftime('%Y%m%d')}{self.packing_counter:06d}"
        packing_status = self.get_random_status(PACKING_STATUS_WEIGHTS)
        
        with self.conn.cursor() as cursor:
            cursor.execute("""
                INSERT INTO wms_packing
                (packing_no, container_id, plan_id, total_quantity, status, create_time, update_time)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
            """, (packing_no, container_id, plan_id, random.randint(20, 100), packing_status, task_date, task_date))
            return cursor.lastrowid
        
    def create_transfer(self, task_date):
        """创建移库"""
        self.transfer_counter += 1
        transfer_no = f"TP{task_date.strftime('%Y%m%d')}{self.transfer_counter:06d}"
        transfer_status = self.get_random_status(TRANSFER_STATUS_WEIGHTS)
        create_by = random.choice(self.user_ids) if self.user_ids else None
        approve_by = random.choice(self.user_ids) if self.user_ids else None
        
        # 随机选择源仓位和目标仓位
        source_loc = random.choice(self.location_ids)
        target_loc = random.choice([loc for loc in self.location_ids if loc != source_loc])
        goods_id = random.choice(list(self.goods_ids.values()))
        quantity = random.randint(10, 50)
        
        with self.conn.cursor() as cursor:
            cursor.execute("""
                INSERT INTO wms_transfer_plan
                (plan_no, source_location_id, target_location_id, goods_id, quantity, reason, 
                 status, create_by, approve_by, create_time, update_time)
                VALUES (%s, %s, %s, %s, %s, '定期移库', %s, %s, %s, %s, %s)
            """, (transfer_no, source_loc, target_loc, goods_id, quantity, 
                  transfer_status, create_by, approve_by, task_date, task_date))
            transfer_id = cursor.lastrowid
            
            # 创建移库作业
            if transfer_status in ['PROCESSING', 'COMPLETED']:
                task_no = f"TT{task_date.strftime('%Y%m%d')}{self.transfer_counter:06d}"
                task_status = 'COMPLETED' if transfer_status == 'COMPLETED' else 'PROCESSING'
                operator_id = random.choice(self.user_ids) if self.user_ids else None
                cursor.execute("""
                    INSERT INTO wms_transfer_task
                    (task_no, plan_id, status, operator_id, create_time)
                    VALUES (%s, %s, %s, %s, %s)
                """, (task_no, transfer_id, task_status, operator_id, task_date))
                
            return transfer_id
        
    def create_check(self, task_date):
        """创建盘点"""
        self.check_counter += 1
        check_no = f"CK{task_date.strftime('%Y%m%d')}{self.check_counter:06d}"
        check_status = self.get_random_status(CHECK_STATUS_WEIGHTS)
        check_type = random.choice(['FULL', 'PART'])
        create_by = random.choice(self.user_ids) if self.user_ids else None
        
        zone_id = None
        location_id = None
        goods_id = None
        
        if check_type == 'PART':
            choice = random.choice(['zone', 'location', 'goods'])
            if choice == 'zone':
                pass
            elif choice == 'location':
                location_id = random.choice(self.location_ids)
            else:
                goods_id = random.choice(list(self.goods_ids.values()))
        
        with self.conn.cursor() as cursor:
            cursor.execute("""
                INSERT INTO wms_check_plan
                (plan_no, check_type, zone_id, location_id, goods_id, status, create_by, create_time, update_time)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            """, (check_no, check_type, zone_id, location_id, goods_id, check_status, create_by, task_date, task_date))
            check_id = cursor.lastrowid
            
            # 创建盘点明细
            if check_status in ['PROCESSING', 'COMPLETED']:
                check_goods = list(self.goods_ids.values())[:3] if check_type == 'FULL' else [goods_id] if goods_id else random.sample(list(self.goods_ids.values()), 2)
                for gid in check_goods:
                    # 获取系统库存
                    inv_data = self.inventory_data.get(gid, {})
                    system_qty = inv_data.get('quantity', 0)
                    loc_id = inv_data.get('location_id', random.choice(self.location_ids))
                    
                    # 生成实际数量（有差异）
                    variation = random.randint(-3, 3)
                    actual_qty = max(0, system_qty + variation)
                    diff_qty = actual_qty - system_qty
                    
                    if diff_qty > 0:
                        diff_type = 'OVER'
                    elif diff_qty < 0:
                        diff_type = 'SHORT'
                    else:
                        diff_type = 'NORMAL'
                    
                    cursor.execute("""
                        INSERT INTO wms_check_detail
                        (plan_id, goods_id, location_id, system_quantity, actual_quantity, diff_quantity, diff_type, remark)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                    """, (check_id, gid, loc_id, system_qty, actual_qty, diff_qty, diff_type, 
                          '自动生成的盘点数据'))
                        
            return check_id
        
    def update_inventory(self, goods_id, delta, task_date):
        """更新库存"""
        if goods_id not in self.inventory_data:
            return
            
        inv_data = self.inventory_data[goods_id]
        new_qty = max(0, inv_data['quantity'] + delta)
        inv_data['quantity'] = new_qty
        
        with self.conn.cursor() as cursor:
            # 更新预警状态
            warning_status = 0
            if new_qty < inv_data['min_warning']:
                warning_status = 1
            elif new_qty > inv_data['max_warning']:
                warning_status = 2
            
            cursor.execute("""
                UPDATE wms_inventory 
                SET quantity = %s, 
                    available_quantity = %s,
                    warning_status = %s,
                    update_time = %s
                WHERE goods_id = %s
            """, (new_qty, new_qty, warning_status, task_date, goods_id))
        
    def generate_data(self, start_date: datetime, end_date: datetime):
        """生成从start_date到end_date的所有数据"""
        print(f"开始生成数据：{start_date.date()} 至 {end_date.date()}")
        
        current_date = start_date
        
        while current_date <= end_date:
            # 为每个商品生成数据
            for goods_id in list(self.goods_ids.values()):
                daily_sales = self.generate_daily_sales(current_date, goods_id)
                
                # 创建销售订单
                if daily_sales > 0:
                    customer_id = random.choice(self.customer_ids)
                    create_by = random.choice(self.user_ids) if self.user_ids else None
                    order_status = self.get_random_status(ORDER_STATUS_WEIGHTS)
                    self.order_counter += 1
                    order_no = f"SO{current_date.strftime('%Y%m%d')}{self.order_counter:06d}"
                    
                    with self.conn.cursor() as cursor:
                        # 获取商品信息
                        goods = self.goods_map.get(goods_id, {})
                        price = goods.get('price', 10.0)
                        
                        # 创建订单
                        expected_date = current_date + timedelta(days=random.randint(1, 3))
                        cursor.execute("""
                            INSERT INTO wms_customer_order 
                            (order_no, customer_id, order_type, order_date, expected_date, 
                             total_quantity, total_amount, status, create_by, create_time)
                            VALUES (%s, %s, 'SALE', %s, %s, %s, %s, %s, %s, %s)
                        """, (order_no, customer_id, current_date, expected_date, 
                              daily_sales, daily_sales * price, order_status, create_by, current_date))
                        order_id = cursor.lastrowid
                        
                        # 创建订单明细
                        cursor.execute("""
                            INSERT INTO wms_customer_order_detail
                            (order_id, goods_id, quantity, price, amount)
                            VALUES (%s, %s, %s, %s, %s)
                        """, (order_id, goods_id, daily_sales, price, daily_sales * price))
                        
                        outbound_id = None
                        outbound_status = None
                        # 只有非取消状态的订单才创建出库
                        if order_status != 'CANCELLED':
                            # 创建出库计划
                            outbound_status = self.get_random_status(OUTBOUND_STATUS_WEIGHTS)
                            outbound_no = f"OB{current_date.strftime('%Y%m%d')}{self.order_counter:06d}"
                            cursor.execute("""
                                INSERT INTO wms_outbound_plan
                                (plan_no, customer_id, order_id, plan_type, plan_date, 
                                 total_quantity, status, create_by, create_time)
                                VALUES (%s, %s, %s, 'SALE', %s, %s, %s, %s, %s)
                            """, (outbound_no, customer_id, order_id, current_date, 
                                  daily_sales, outbound_status, create_by, current_date))
                            outbound_id = cursor.lastrowid
                            
                            # 获取库存位置
                            inv_data = self.inventory_data.get(goods_id, {})
                            location_id = inv_data.get('location_id', random.choice(self.location_ids))
                                
                            # 创建出库明细
                            actual_qty = daily_sales if outbound_status in ['COMPLETED', 'PROCESSING'] else 0
                            cursor.execute("""
                                INSERT INTO wms_outbound_plan_detail
                                (plan_id, goods_id, quantity, actual_quantity, location_id)
                                VALUES (%s, %s, %s, %s, %s)
                            """, (outbound_id, goods_id, daily_sales, actual_qty, location_id))
                            
                            # 只有已完成的订单才更新库存
                            if outbound_status == 'COMPLETED':
                                self.update_inventory(goods_id, -daily_sales, current_date)
                                    
                                # 检查是否需要补货
                                inv_data = self.inventory_data.get(goods_id, {})
                                qty = inv_data.get('quantity', 0)
                                min_warn = inv_data.get('min_warning', 20)
                                
                                if qty < min_warn:
                                    # 创建补货计划
                                    goods = self.goods_map.get(goods_id, {})
                                    base_sales = goods.get('base_sales', 20)
                                    replenish_qty = random.randint(int(base_sales * 4), int(base_sales * 8))
                                    replenish_status = self.get_random_status(REPLENISHMENT_STATUS_WEIGHTS)
                                    self.replenishment_counter += 1
                                    replenish_no = f"RP{current_date.strftime('%Y%m%d')}{self.replenishment_counter:06d}"
                                    cursor.execute("""
                                        INSERT INTO wms_replenishment_plan
                                        (plan_no, goods_id, source_location_id, target_location_id, 
                                         quantity, trigger_reason, status, create_time)
                                        VALUES (%s, %s, %s, %s, %s, 'LOW_STOCK', %s, %s)
                                    """, (replenish_no, goods_id, location_id, location_id, 
                                          replenish_qty, replenish_status, current_date))
                                    replenish_id = cursor.lastrowid
                                        
                                    # 同时创建入库（补货）
                                    inbound_status = self.get_random_status(INBOUND_STATUS_WEIGHTS)
                                    self.inbound_counter += 1
                                    inbound_no = f"IB{current_date.strftime('%Y%m%d')}{self.inbound_counter:06d}"
                                    approve_by = random.choice(self.user_ids) if self.user_ids else None
                                    approve_time = current_date if inbound_status in ['APPROVED', 'PROCESSING', 'COMPLETED'] else None
                                        
                                    cursor.execute("""
                                        INSERT INTO wms_inbound_plan
                                        (plan_no, supplier_id, plan_type, plan_date, 
                                         total_quantity, status, create_by, approve_by, approve_time, create_time)
                                        VALUES (%s, %s, 'REPLENISH', %s, %s, %s, %s, %s, %s, %s)
                                    """, (inbound_no, self.supplier_id, current_date, replenish_qty, 
                                          inbound_status, create_by, approve_by, approve_time, current_date))
                                    inbound_id = cursor.lastrowid
                                        
                                    actual_in_qty = replenish_qty if inbound_status == 'COMPLETED' else 0
                                    cursor.execute("""
                                        INSERT INTO wms_inbound_plan_detail
                                        (plan_id, goods_id, quantity, actual_quantity, location_id)
                                        VALUES (%s, %s, %s, %s, %s)
                                    """, (inbound_id, goods_id, replenish_qty, actual_in_qty, location_id))
                                        
                                    # 创建入库作业
                                    if inbound_status in ['PROCESSING', 'COMPLETED']:
                                        operator_id = random.choice(self.user_ids) if self.user_ids else None
                                        self.create_inbound_task(inbound_id, operator_id, current_date)
                                        
                                    # 只有已完成的入库才更新库存
                                    if inbound_status == 'COMPLETED':
                                        self.update_inventory(goods_id, replenish_qty, current_date)
                        
                        # 创建拣选计划（如果订单已确认或处理中）
                        picking_plan_id = None
                        if order_status in ['CONFIRMED', 'PROCESSING', 'COMPLETED']:
                            self.picking_counter += 1
                            picking_no = f"PK{current_date.strftime('%Y%m%d')}{self.picking_counter:06d}"
                            picking_status = 'COMPLETED' if order_status == 'COMPLETED' else 'PROCESSING'
                            cursor.execute("""
                                INSERT INTO wms_picking_plan
                                (plan_no, order_id, plan_type, status, create_by, create_time)
                                VALUES (%s, %s, 'OUTBOUND', %s, %s, %s)
                            """, (picking_no, order_id, picking_status, create_by, current_date))
                            picking_plan_id = cursor.lastrowid
                            
                            # 创建拣货作业
                            if picking_status in ['PROCESSING', 'COMPLETED']:
                                operator_id = random.choice(self.user_ids) if self.user_ids else None
                                self.create_picking_task(picking_plan_id, operator_id, current_date)
                            
                            # 创建装箱单
                            if outbound_id and outbound_status in ['PROCESSING', 'COMPLETED']:
                                container_id = random.choice(self.container_ids)
                                self.create_packing(outbound_id, container_id, current_date)
                        
                    self.conn.commit()
                    
            # 每3天一次常规补货（只针对部分商品）
            if current_date.day % 3 == 0:
                replenish_goods = random.sample(list(self.goods_ids.values()), min(5, len(self.goods_ids)))
                for goods_id in replenish_goods:
                    goods = self.goods_map.get(goods_id, {})
                    base_sales = goods.get('base_sales', 20)
                    replenish_qty = random.randint(int(base_sales * 2), int(base_sales * 5))
                    create_by = random.choice(self.user_ids) if self.user_ids else None
                    with self.conn.cursor() as cursor:
                        # 获取库存位置
                        inv_data = self.inventory_data.get(goods_id, {})
                        location_id = inv_data.get('location_id', random.choice(self.location_ids))
                            
                        inbound_status = self.get_random_status(INBOUND_STATUS_WEIGHTS)
                        self.inbound_counter += 1
                        inbound_no = f"IB{current_date.strftime('%Y%m%d')}{self.inbound_counter:06d}"
                        approve_by = random.choice(self.user_ids) if self.user_ids else None
                        approve_time = current_date if inbound_status in ['APPROVED', 'PROCESSING', 'COMPLETED'] else None
                            
                        cursor.execute("""
                            INSERT INTO wms_inbound_plan
                            (plan_no, supplier_id, plan_type, plan_date, 
                             total_quantity, status, create_by, approve_by, approve_time, create_time)
                            VALUES (%s, %s, 'NORMAL', %s, %s, %s, %s, %s, %s, %s)
                        """, (inbound_no, self.supplier_id, current_date, replenish_qty, 
                              inbound_status, create_by, approve_by, approve_time, current_date))
                        inbound_id = cursor.lastrowid
                            
                        actual_in_qty = replenish_qty if inbound_status == 'COMPLETED' else 0
                        cursor.execute("""
                            INSERT INTO wms_inbound_plan_detail
                            (plan_id, goods_id, quantity, actual_quantity, location_id)
                            VALUES (%s, %s, %s, %s, %s)
                        """, (inbound_id, goods_id, replenish_qty, actual_in_qty, location_id))
                            
                        # 创建入库作业
                        if inbound_status in ['PROCESSING', 'COMPLETED']:
                            operator_id = random.choice(self.user_ids) if self.user_ids else None
                            self.create_inbound_task(inbound_id, operator_id, current_date)
                            
                        # 只有已完成的入库才更新库存
                        if inbound_status == 'COMPLETED':
                            self.update_inventory(goods_id, replenish_qty, current_date)
                                
                    self.conn.commit()
            
            # 每周创建一次移库
            if current_date.weekday() == 0:  # 周一
                self.create_transfer(current_date)
                self.conn.commit()
            
            # 每两周创建一次盘点
            if current_date.day % 14 == 0:
                self.create_check(current_date)
                self.conn.commit()
            
            current_date += timedelta(days=1)
            
        print(f"数据生成完成！")
        print(f"  - 销售订单: {self.order_counter} 个")
        print(f"  - 入库计划: {self.inbound_counter} 个")
        print(f"  - 入库作业: {self.inbound_task_counter} 个")
        print(f"  - 出库计划: {self.order_counter - self.order_counter // 10} 个（排除已取消）")
        print(f"  - 拣选计划: {self.picking_counter} 个")
        print(f"  - 拣货作业: {self.picking_task_counter} 个")
        print(f"  - 装箱单: {self.packing_counter} 个")
        print(f"  - 补货计划: {self.replenishment_counter} 个")
        print(f"  - 移库管理: {self.transfer_counter} 个")
        print(f"  - 盘点管理: {self.check_counter} 个")

def main():
    # 设置随机种子保证可重复性
    random.seed(42)
    
    # 时间范围：2025年1月1日至今
    start_date = datetime(2025, 1, 1)
    end_date = datetime.now()
    
    generator = DataGenerator()
    
    try:
        generator.connect()
        generator.clear_old_data()
        generator.create_basic_data()
        generator.create_inventory()
        generator.generate_data(start_date, end_date)
        print("✓ 智能补货数据生成成功！")
        
    except Exception as e:
        print(f"✗ 生成数据时出错: {e}")
        import traceback
        traceback.print_exc()
    finally:
        generator.disconnect()

if __name__ == "__main__":
    main()
