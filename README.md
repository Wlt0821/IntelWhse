# 智慧物流仓储实训软件（WMS）

## 项目简介

基于 SpringBoot 3.x + Vue3 + Element Plus 的智能仓储管理平台，包含完整的仓储业务流程管理功能。

## 技术栈

### 后端
- SpringBoot 3.2.0
- SpringMVC
- MyBatis-Plus 3.5.5
- MySQL 8.x
- JWT 0.12.3
- Hutool 5.8.24
- Knife4j 4.4.0

### 前端
- Vue 3.4.x
- Vue Router 4.2.x
- Pinia 2.1.x
- Element Plus 2.5.x
- Axios 1.6.x
- Vite 5.0.x

## 项目结构

```
IntelligentWarehousing/
├── wms-backend/                 # 后端项目
│   ├── src/main/java/com/wms/
│   │   ├── annotation/           # 自定义注解
│   │   ├── common/               # 公共类
│   │   ├── config/               # 配置类
│   │   ├── controller/           # 控制器
│   │   ├── dto/                  # 数据传输对象
│   │   ├── entity/               # 实体类
│   │   ├── exception/            # 异常处理
│   │   ├── interceptor/          # 拦截器
│   │   ├── mapper/               # Mapper接口
│   │   ├── service/              # 业务逻辑
│   │   ├── utils/                # 工具类
│   │   ├── vo/                   # 视图对象
│   │   └── WmsApplication.java   # 启动类
│   └── src/main/resources/
│       └── application.yml       # 配置文件
├── wms-frontend/                 # 前端项目
│   ├── src/
│   │   ├── api/                  # API接口
│   │   ├── components/           # 公共组件
│   │   ├── router/               # 路由配置
│   │   ├── store/                # 状态管理
│   │   ├── utils/                # 工具函数
│   │   ├── views/                # 页面组件
│   │   ├── App.vue                # 根组件
│   │   └── main.js                # 入口文件
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── sql/                           # 数据库脚本
│   └── wms_db.sql
└── docs/                          # 文档目录
```

## 功能模块

### 1. 系统管理
- 用户登录/登出
- 用户管理
- 角色管理
- 菜单管理
- 操作日志

### 2. 基础数据管理
- 客户设置
- 供应商设置
- 容器设置
- 商品设置
- 库区设置
- 仓位设置（树形结构）

### 3. 订单管理
- 客户订单
- 订单处理
- 拣选计划

### 4. 入库管理
- 入库计划
- 入库作业
- 入库计划单列表

### 5. 出库管理
- 拣货计划
- 拣货作业
- 装箱单管理

### 6. 补货管理
- 补货计划
- 补货作业

### 7. 移库管理
- 移库计划
- 移库作业

### 8. 盘点管理
- 库存盘点
- 盘盈盘亏处理

### 9. 数据查询
- 多维度数据查询
- 基础统计报表

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 后端启动

1. 初始化数据库
```bash
# 创建数据库并执行脚本
mysql -u root -p < sql/wms_db.sql
```

2. 修改配置文件
编辑 `wms-backend/src/main/resources/application.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wms_db?...
    username: root
    password: your_password
```

3. 启动后端
```bash
cd wms-backend
mvn clean install
mvn spring-boot:run
```

后端服务地址：http://localhost:8080

API文档地址：http://localhost:8080/doc.html

### 前端启动

1. 安装依赖
```bash
cd wms-frontend
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

前端访问地址：http://localhost:3000

### 默认账号

- 用户名：admin / 密码：123456
- 用户名：01 / 密码：123456

## 核心特性

### 后端特性
- 统一响应结果封装（code、msg、data）
- 全局异常处理
- JWT登录认证
- RBAC权限控制
- 操作日志记录
- MyBatis-Plus分页插件
- 数据库事务控制
- 参数校验（@Valid）

### 前端特性
- Vue3 Composition API
- Element Plus UI组件库
- 路由权限控制
- Axios请求拦截
- 响应式布局

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发规范
- 统一使用Lombok简化代码
- 关键业务逻辑添加注释

### Git提交规范
- feat：新功能
- fix：修复bug
- docs：文档更新
- style：代码格式调整
- refactor：重构
- test：测试相关
- chore：构建/工具相关

## 部署说明

### 后端部署
```bash
cd wms-backend
mvn clean package
java -jar target/wms-backend-1.0.0.jar
```

### 前端部署
```bash
cd wms-frontend
npm run build
# 将 dist 目录部署到 Nginx 或其他 Web 服务器
```

## 许可证

MIT License
