#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
每日出库数据生成脚本
- 自动获取当前日期
- 根据日期类型（工作日/周末/节假日）生成销量
- 只生成出库相关数据（销售、出库、拣货、装箱）
- 时间为当天的随机时间，不是零点
"""

import pymysql
import random
from datetime import datetime, timedelta

# ============== 数据库配置 ==============
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'root',
    'database': 'db_wms_db',
    'charset': 'utf8mb4'
}

# ============== 节假日配置 ==============
# 2025-2026年节假日（日期格式：MM-DD）
HOLIDAYS = {
    '01-01': '元旦',
    '02-10': '春节',
    '02-11': '春节',
    '02-12': '春节',
    '02-13': '春节',
    '02-14': '春节',
    '02-15': '春节',
    '02-16': '春节',
    '02-17': '春节',
    '04-04': '清明节',
    '04-05': '清明节',
    '04-06': '清明节',
    '05-01': '劳动节',
    '05-02': '劳动节',
    '05-03': '劳动节',
    '05-04': '劳动节',
    '05-05': '劳动节',
    '06-19': '端午节',
    '06-20': '端午节',
    '06-21': '端午节',
    '10-01': '国庆节',
    '10-02': '国庆节',
    '10-03': '国庆节',
    '10-04': '国庆节',
    '10-05': '国庆节',
    '10-06': '国庆节',
    '10-07': '国庆节',
}

# ============== 商品配置 ==============
GOODS_CONFIG = [
    # 牛奶类
    {'name': '特仑苏纯牛奶250ml*12', 'code': 'ML001', 'price': 65.0, 'base_sales': 30, 'category': 'milk'},
    {'name': '伊利金典纯牛奶250ml*12', 'code': 'ML002', 'price': 58.0, 'base_sales': 28, 'category': 'milk'},
    {'name': '蒙牛纯牛奶250ml*24', 'code': 'ML003', 'price': 72.0, 'base_sales': 25, 'category': 'milk'},
    {'name': '光明鲜牛奶950ml', 'code': 'ML004', 'price': 18.5, 'base_sales': 35, 'category': 'milk', 'perishable': True},
    {'name': '安佳全脂牛奶1L*12', 'code': 'ML005', 'price': 128.0, 'base_sales': 15, 'category': 'milk'},
    {'name': '德亚低脂牛奶200ml*30', 'code': 'ML006', 'price': 99.0, 'base_sales': 12, 'category': 'milk'},
    {'name': '旺仔牛奶245ml*12', 'code': 'ML007', 'price': 52.0, 'base_sales': 22, 'category': 'milk', 'weekend_boost': True, 'holiday_boost': True},
    {'name': '养乐多100ml*5', 'code': 'ML008', 'price': 12.5, 'base_sales': 45, 'category': 'milk', 'perishable': True},
    
    # 零食类
    {'name': '乐事薯片原味75g', 'code': 'LS001', 'price': 9.9, 'base_sales': 50, 'category': 'snack', 'weekend_boost': True},
    {'name': '乐事薯片黄瓜味75g', 'code': 'LS002', 'price': 9.9, 'base_sales': 42, 'category': 'snack', 'weekend_boost': True},
    {'name': '可比克薯片番茄味60g', 'code': 'LS003', 'price': 6.5, 'base_sales': 38, 'category': 'snack'},
    {'name': '奥利奥巧克力味95g', 'code': 'LS004', 'price': 11.5, 'base_sales': 32, 'category': 'snack'},
    {'name': '趣多多巧克力曲奇96g', 'code': 'LS005', 'price': 10.5, 'base_sales': 28, 'category': 'snack'},
    {'name': '旺旺雪饼84g', 'code': 'LS006', 'price': 8.5, 'base_sales': 40, 'category': 'snack', 'weekend_boost': True, 'holiday_boost': True},
    {'name': '旺旺仙贝52g', 'code': 'LS007', 'price': 7.5, 'base_sales': 35, 'category': 'snack', 'weekend_boost': True, 'holiday_boost': True},
    {'name': '上好佳鲜虾片40g', 'code': 'LS008', 'price': 5.5, 'base_sales': 45, 'category': 'snack', 'weekend_boost': True},
    {'name': '三只松鼠夏威夷果160g', 'code': 'LS009', 'price': 29.9, 'base_sales': 18, 'category': 'snack', 'winter_boost': True},
    {'name': '三只松鼠碧根果120g', 'code': 'LS010', 'price': 25.9, 'base_sales': 15, 'category': 'snack', 'winter_boost': True},
    {'name': '良品铺子猪肉脯100g', 'code': 'LS011', 'price': 32.0, 'base_sales': 22, 'category': 'snack'},
    {'name': '百草味牛肉干80g', 'code': 'LS012', 'price': 35.0, 'base_sales': 20, 'category': 'snack'},
]

# ============== 状态权重配置 ==============
ORDER_STATUS_WEIGHTS = {
    'DRAFT': 5,
    'CONFIRMED': 10,
    'PROCESSING': 15,
    'COMPLETED': 60,
    'CANCELLED': 10
}

OUTBOUND_STATUS_WEIGHTS = {
    'DRAFT': 8,
    'PENDING': 12,
    'APPROVED': 15,
    'PROCESSING': 20,
    'COMPLETED': 40,
    'CANCELLED': 5
}

PICKING_STATUS_WEIGHTS = {
    'PENDING': 25,
    'PROCESSING': 35,
    'COMPLETED': 40
}

class DailyOutboundDataGenerator:
    def __init__(self):
        self.conn = None
        self.goods_ids = {}
        self.goods_map = {}
        self.location_ids = []
        self.customer_ids = []
        self.user_ids = []
        self.container_ids = []
        self.inventory_data = {}
        self.order_counter = 0
        self.picking_counter = 0
        self.picking_task_counter = 0
        self.packing_counter = 0
        
    def connect(self):
        """连接数据库"""
        self.conn = pymysql.connect(**DB_CONFIG)
        print("数据库连接成功")
        
    def disconnect(self):
        """断开数据库连接"""
        if self.conn:
            self.conn.close()
            print("数据库连接已关闭")
            
    def get_random_status(self, weight_map):
        """根据权重随机获取状态"""
        items = list(weight_map.items())
        weights = [w for _, w in items]
        return random.choices([s for s, _ in items], weights=weights, k=1)[0]
        
    def get_random_time(self, base_date):
        """获取当天的随机时间（不是零点）"""
        hour = random.randint(6, 22)  # 6点到22点之间
        minute = random.randint(0, 59)
        second = random.randint(0, 59)
        return base_date.replace(hour=hour, minute=minute, second=second, microsecond=0)
        
    def load_basic_data(self, current_date):
        """加载基础数据"""
        with self.conn.cursor() as cursor:
            # 加载商品
            cursor.execute("SELECT id, goods_code, goods_name, price FROM base_goods")
            for row in cursor.fetchall():
                self.goods_ids[row[1]] = row[0]
                self.goods_map[row[0]] = {
                    'id': row[0],
                    'code': row[1],
                    'name': row[2],
                    'price': float(row[3]) if row[3] else 0
                }
                
            # 加载仓位
            cursor.execute("SELECT id FROM base_location")
            self.location_ids = [row[0] for row in cursor.fetchall()]
            
            # 加载客户
            cursor.execute("SELECT id FROM base_customer")
            self.customer_ids = [row[0] for row in cursor.fetchall()]
            
            # 加载用户
            cursor.execute("SELECT id FROM sys_user")
            self.user_ids = [row[0] for row in cursor.fetchall()]
            
            # 加载容器
            cursor.execute("SELECT id FROM base_container")
            self.container_ids = [row[0] for row in cursor.fetchall()]
            
            # 加载库存
            cursor.execute("SELECT id, goods_id, location_id, quantity, min_warning_quantity, max_warning_quantity FROM wms_inventory")
            for row in cursor.fetchall():
                self.inventory_data[row[1]] = {
                    'id': row[0],
                    'goods_id': row[1],
                    'location_id': row[2],
                    'quantity': float(row[3]) if row[3] else 0,
                    'min_warning': float(row[4]) if row[4] else 20,
                    'max_warning': float(row[5]) if row[5] else 200
                }
            
            # 查询今天已有的最大订单编号
            date_prefix = current_date.strftime('%Y%m%d')
            
            # 销售订单编号
            cursor.execute("""
                SELECT MAX(order_no) FROM wms_customer_order 
                WHERE order_no LIKE %s
            """, (f'SO{date_prefix}%',))
            result = cursor.fetchone()
            if result and result[0]:
                max_no = result[0]
                counter = int(max_no[-6:]) if len(max_no) >= 14 else 0
                self.order_counter = counter
                print(f"检测到今天已有订单，从编号 {self.order_counter + 1} 开始")
            else:
                self.order_counter = 0
            
            # 出库计划编号
            cursor.execute("""
                SELECT MAX(plan_no) FROM wms_outbound_plan 
                WHERE plan_no LIKE %s
            """, (f'OB{date_prefix}%',))
            result = cursor.fetchone()
            if result and result[0]:
                max_no = result[0]
                counter = int(max_no[-6:]) if len(max_no) >= 14 else 0
                self.order_counter = max(self.order_counter, counter)
            else:
                pass
            
            # 拣选计划编号
            cursor.execute("""
                SELECT MAX(plan_no) FROM wms_picking_plan 
                WHERE plan_no LIKE %s
            """, (f'PK{date_prefix}%',))
            result = cursor.fetchone()
            if result and result[0]:
                max_no = result[0]
                counter = int(max_no[-6:]) if len(max_no) >= 14 else 0
                self.picking_counter = counter
            else:
                self.picking_counter = 0
            
            # 拣货作业编号
            cursor.execute("""
                SELECT MAX(task_no) FROM wms_picking_task 
                WHERE task_no LIKE %s
            """, (f'PT{date_prefix}%',))
            result = cursor.fetchone()
            if result and result[0]:
                max_no = result[0]
                counter = int(max_no[-6:]) if len(max_no) >= 14 else 0
                self.picking_task_counter = counter
            else:
                self.picking_task_counter = 0
            
            # 装箱单编号
            cursor.execute("""
                SELECT MAX(packing_no) FROM wms_packing 
                WHERE packing_no LIKE %s
            """, (f'PK{date_prefix}%',))
            result = cursor.fetchone()
            if result and result[0]:
                max_no = result[0]
                counter = int(max_no[-6:]) if len(max_no) >= 14 else 0
                self.packing_counter = counter
            else:
                self.packing_counter = 0
                
        print("基础数据加载完成")
        
    def generate_daily_sales(self, current_date, goods_config):
        """生成每日销量"""
        month = current_date.month
        weekday = current_date.weekday()  # 0=周一, 6=周日
        date_str = current_date.strftime('%m-%d')
        
        # 基础销量
        sales = goods_config['base_sales']
        
        # 周末加成
        if weekday >= 5:
            sales *= 1.8
            if goods_config.get('weekend_boost'):
                sales *= 1.2
                
        # 节假日加成
        if date_str in HOLIDAYS:
            sales *= 2.5
            if goods_config.get('holiday_boost'):
                sales *= 1.3
                
        # 季节因素
        if 6 <= month <= 8:  # 夏季
            if goods_config['category'] == 'milk':
                sales *= 1.3
            elif goods_config['category'] == 'snack':
                sales *= 0.9
        elif 12 <= month <= 2:  # 冬季
            if goods_config.get('winter_boost'):
                sales *= 1.4
            elif goods_config['category'] == 'milk':
                sales *= 0.95
            elif goods_config['category'] == 'snack':
                sales *= 1.2
                
        # 促销日（每月15号和月底）
        day = current_date.day
        if day == 15 or day >= 28:
            sales *= 1.5
            
        # 易腐商品（鲜牛奶、养乐多）在周末销量更高
        if goods_config.get('perishable') and weekday >= 5:
            sales *= 1.2
            
        # 随机波动
        sales *= random.uniform(0.85, 1.15)
        
        return max(1, int(sales))
        
    def update_inventory(self, goods_id, quantity_change, change_date):
        """更新库存"""
        if goods_id not in self.inventory_data:
            return
            
        inv_data = self.inventory_data[goods_id]
        new_qty = inv_data['quantity'] + quantity_change
        new_qty = max(0, new_qty)
        inv_data['quantity'] = new_qty
        
        warning_status = 0
        if new_qty < inv_data['min_warning']:
            warning_status = 1
        elif new_qty > inv_data['max_warning']:
            warning_status = 2
            
        with self.conn.cursor() as cursor:
            cursor.execute("""
                UPDATE wms_inventory 
                SET quantity = %s, warning_status = %s, update_time = %s
                WHERE id = %s
            """, (new_qty, warning_status, change_date, inv_data['id']))
        self.conn.commit()
        
    def create_customer_order(self, goods_config, current_date):
        """创建客户订单"""
        goods_id = self.goods_ids.get(goods_config['code'])
        if not goods_id:
            return None, None, None, None
            
        self.order_counter += 1
        order_no = f"SO{current_date.strftime('%Y%m%d')}{self.order_counter:06d}"
        order_status = self.get_random_status(ORDER_STATUS_WEIGHTS)
        customer_id = random.choice(self.customer_ids) if self.customer_ids else None
        create_by = random.choice(self.user_ids) if self.user_ids else None
        daily_sales = self.generate_daily_sales(current_date, goods_config)
        
        # 获取当天的随机时间
        order_time = self.get_random_time(current_date)
        
        with self.conn.cursor() as cursor:
            price = goods_config['price']
            expected_date = order_time + timedelta(days=random.randint(1, 3))
            
            cursor.execute("""
                INSERT INTO wms_customer_order 
                (order_no, customer_id, order_type, order_date, expected_date, 
                 total_quantity, total_amount, status, create_by, create_time)
                VALUES (%s, %s, 'SALE', %s, %s, %s, %s, %s, %s, %s)
            """, (order_no, customer_id, order_time, expected_date, 
                  daily_sales, daily_sales * price, order_status, create_by, order_time))
            order_id = cursor.lastrowid
            
            # 创建订单明细
            cursor.execute("""
                INSERT INTO wms_customer_order_detail
                (order_id, goods_id, quantity, price, amount)
                VALUES (%s, %s, %s, %s, %s)
            """, (order_id, goods_id, daily_sales, price, daily_sales * price))
            
        self.conn.commit()
        return order_id, goods_id, daily_sales, order_status, order_time
        
    def create_outbound_plan(self, order_id, customer_id, goods_id, quantity, current_date):
        """创建出库计划"""
        outbound_status = self.get_random_status(OUTBOUND_STATUS_WEIGHTS)
        outbound_no = f"OB{current_date.strftime('%Y%m%d')}{self.order_counter:06d}"
        create_by = random.choice(self.user_ids) if self.user_ids else None
        
        inv_data = self.inventory_data.get(goods_id, {})
        location_id = inv_data.get('location_id', random.choice(self.location_ids))
        
        # 获取当天的随机时间（在订单时间之后）
        plan_time = self.get_random_time(current_date)
        
        with self.conn.cursor() as cursor:
            cursor.execute("""
                INSERT INTO wms_outbound_plan
                (plan_no, customer_id, order_id, plan_type, plan_date, 
                 total_quantity, status, create_by, create_time)
                VALUES (%s, %s, %s, 'SALE', %s, %s, %s, %s, %s)
            """, (outbound_no, customer_id, order_id, plan_time, 
                  quantity, outbound_status, create_by, plan_time))
            outbound_id = cursor.lastrowid
            
            actual_qty = quantity if outbound_status in ['COMPLETED', 'PROCESSING'] else 0
            cursor.execute("""
                INSERT INTO wms_outbound_plan_detail
                (plan_id, goods_id, quantity, actual_quantity, location_id)
                VALUES (%s, %s, %s, %s, %s)
            """, (outbound_id, goods_id, quantity, actual_qty, location_id))
            
        self.conn.commit()
        return outbound_id, outbound_status, location_id, plan_time
        
    def create_picking_plan(self, order_id, current_date):
        """创建拣选计划"""
        self.picking_counter += 1
        picking_no = f"PK{current_date.strftime('%Y%m%d')}{self.picking_counter:06d}"
        picking_status = 'COMPLETED'
        create_by = random.choice(self.user_ids) if self.user_ids else None
        
        # 获取当天的随机时间
        plan_time = self.get_random_time(current_date)
        
        with self.conn.cursor() as cursor:
            cursor.execute("""
                INSERT INTO wms_picking_plan
                (plan_no, order_id, plan_type, status, create_by, create_time)
                VALUES (%s, %s, 'OUTBOUND', %s, %s, %s)
            """, (picking_no, order_id, picking_status, create_by, plan_time))
            picking_plan_id = cursor.lastrowid
            
        self.conn.commit()
        return picking_plan_id, plan_time
        
    def create_picking_task(self, plan_id, operator_id, task_date):
        """创建拣货作业"""
        self.picking_task_counter += 1
        task_no = f"PT{task_date.strftime('%Y%m%d')}{self.picking_task_counter:06d}"
        task_status = self.get_random_status(PICKING_STATUS_WEIGHTS)
        start_time = self.get_random_time(task_date) if task_status in ['PROCESSING', 'COMPLETED'] else None
        end_time = self.get_random_time(task_date) if task_status == 'COMPLETED' else None
        
        with self.conn.cursor() as cursor:
            cursor.execute("""
                INSERT INTO wms_picking_task
                (task_no, plan_id, status, operator_id, start_time, end_time, create_time)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
            """, (task_no, plan_id, task_status, operator_id, start_time, end_time, self.get_random_time(task_date)))
            return cursor.lastrowid
        
    def create_packing(self, plan_id, container_id, task_date):
        """创建装箱单"""
        self.packing_counter += 1
        packing_no = f"PK{task_date.strftime('%Y%m%d')}{self.packing_counter:06d}"
        packing_status = 'SHIPPED'
        
        with self.conn.cursor() as cursor:
            cursor.execute("""
                INSERT INTO wms_packing
                (packing_no, container_id, plan_id, total_quantity, status, create_time, update_time)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
            """, (packing_no, container_id, plan_id, random.randint(20, 100), packing_status, 
                  self.get_random_time(task_date), self.get_random_time(task_date)))
            return cursor.lastrowid
        
    def generate_daily_data(self, current_date):
        """生成当天的数据"""
        print(f"正在生成 {current_date.date()} 的出库数据...")
        
        created_orders = 0
        
        for goods_config in GOODS_CONFIG:
            # 创建销售订单
            order_id, goods_id, quantity, order_status, order_time = self.create_customer_order(goods_config, current_date)
            if not order_id:
                continue
                
            created_orders += 1
            
            # 只有非取消状态的订单才创建出库
            if order_status != 'CANCELLED':
                customer_id = random.choice(self.customer_ids) if self.customer_ids else None
                outbound_id, outbound_status, location_id, plan_time = self.create_outbound_plan(
                    order_id, customer_id, goods_id, quantity, current_date)
                
                # 只有已完成的订单才更新库存
                if outbound_status == 'COMPLETED':
                    self.update_inventory(goods_id, -quantity, plan_time)
                
                # 创建拣选计划
                if order_status in ['CONFIRMED', 'PROCESSING', 'COMPLETED']:
                    picking_plan_id, picking_time = self.create_picking_plan(order_id, current_date)
                    
                    operator_id = random.choice(self.user_ids) if self.user_ids else None
                    self.create_picking_task(picking_plan_id, operator_id, current_date)
                    
                    if outbound_status in ['PROCESSING', 'COMPLETED']:
                        container_id = random.choice(self.container_ids)
                        self.create_packing(outbound_id, container_id, current_date)
        
        return created_orders
        
    def run(self):
        """运行每日数据生成"""
        try:
            self.connect()
            
            # 获取当前日期
            current_date = datetime.now()
            
            self.load_basic_data(current_date)
            
            print(f"开始生成数据：{current_date.date()}")
            
            orders_count = self.generate_daily_data(current_date)
            
            print(f"数据生成完成！")
            print(f"  - 销售订单: {orders_count} 个")
            print("✓ 每日出库数据生成成功！")
            
        except Exception as e:
            print(f"✗ 生成数据时出错: {e}")
            import traceback
            traceback.print_exc()
        finally:
            self.disconnect()

if __name__ == "__main__":
    # 设置随机种子
    random.seed(datetime.now().timestamp())
    
    generator = DailyOutboundDataGenerator()
    generator.run()
