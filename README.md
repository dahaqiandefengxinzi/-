# Shop-Lab 电商网站

一个基于 Spring Boot + Vue 3 的全栈电商网站项目，包含用户端商城和管理后台功能。

## 项目简介

Shop-Lab 是一个功能完整的电商网站，采用前后端分离架构开发。前端使用 Vue 3 + Element Plus 构建现代化的用户界面，后端使用 Spring Boot + MyBatis Plus 提供稳定的 RESTful API 服务。

## 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| Spring Security | - | 安全框架 |
| MyBatis Plus | 3.5.3.1 | ORM框架 |
| MySQL | 8.0 | 数据库 |
| Redis | - | 缓存 |
| JWT | 0.9.1 | 身份认证 |
| SpringDoc | 1.7.0 | API文档 |
| Hutool | 5.8.18 | 工具库 |
| Easy Captcha | 1.6.2 | 验证码生成 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.21 | 前端框架 |
| Vue Router | 4.3.0 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Element Plus | 2.6.1 | UI组件库 |
| Axios | 1.6.8 | HTTP客户端 |
| Vite | 5.2.0 | 构建工具 |
| Sass | 1.72.0 | CSS预处理器 |

## 功能模块

### 用户端功能

- **用户认证**：注册、登录、JWT身份认证
- **商品浏览**：商品列表、商品详情、分类筛选
- **购物车**：添加商品、修改数量、删除商品
- **订单管理**：创建订单、订单支付、订单列表、订单详情
- **收货地址**：地址管理、默认地址设置
- **搜索功能**：商品搜索

### 管理后台功能

- **仪表盘**：数据概览
- **用户管理**：用户列表、状态管理
- **商品管理**：商品增删改查、上下架
- **分类管理**：分类增删改查
- **订单管理**：订单列表、发货处理
- **Banner管理**：轮播图配置
- **验证码配置**：验证码类型、参数设置

### 验证码功能

支持多种验证码类型：

- **图形验证码**：传统字符验证码
- **滑动拼图**：拖动滑块完成验证
- **文字点选**：按顺序点击指定文字
- **计算题验证码**：数学运算验证

## 项目结构

```
shop-lab/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/shop/
│   │   │   │       ├── common/         # 通用类
│   │   │   │       ├── config/         # 配置类
│   │   │   │       ├── controller/     # 控制器
│   │   │   │       ├── dto/            # 数据传输对象
│   │   │   │       ├── entity/         # 实体类
│   │   │   │       ├── exception/      # 异常处理
│   │   │   │       ├── mapper/         # 数据访问层
│   │   │   │       ├── service/        # 业务逻辑层
│   │   │   │       └── util/           # 工具类
│   │   │   └── resources/
│   │   │       ├── db/                 # 数据库脚本
│   │   │       ├── mapper/             # MyBatis映射文件
│   │   │       └── application.yml     # 配置文件
│   │   └── test/                       # 测试代码
│   └── pom.xml                         # Maven配置
│
└── frontend/                   # 前端项目
    ├── src/
    │   ├── api/                # API接口
    │   ├── components/         # 公共组件
    │   ├── layouts/            # 布局组件
    │   ├── router/             # 路由配置
    │   ├── stores/             # 状态管理
    │   ├── styles/             # 样式文件
    │   ├── utils/              # 工具函数
    │   └── views/              # 页面组件
    │       ├── admin/          # 管理后台页面
    │       ├── auth/           # 认证页面
    │       ├── cart/           # 购物车页面
    │       ├── home/           # 首页
    │       ├── order/          # 订单页面
    │       ├── product/        # 商品页面
    │       ├── search/         # 搜索页面
    │       └── user/           # 用户中心页面
    ├── index.html              # 入口HTML
    ├── package.json            # NPM配置
    └── vite.config.js          # Vite配置
```

## 快速开始

### 环境要求

- JDK 1.8+
- Node.js 16+
- MySQL 8.0+
- Redis
- Maven 3.6+

### 数据库配置

1. 创建数据库并导入初始数据：

```sql
-- 执行 backend/src/main/resources/db/init.sql
source backend/src/main/resources/db/init.sql
```

2. 修改数据库配置（`backend/src/main/resources/application.yml`）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/shop_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

### 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:3000` 启动。

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

## API文档

启动后端服务后，访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

## 数据库设计

### 核心数据表

| 表名 | 说明 |
|------|------|
| user | 用户表 |
| category | 商品分类表 |
| product | 商品表 |
| product_detail | 商品详情表 |
| cart | 购物车表 |
| order | 订单表 |
| order_item | 订单商品表 |
| address | 收货地址表 |
| banner | 轮播图表 |
| captcha_config | 验证码配置表 |
| captcha_record | 验证码记录表 |

## 开发说明

### 后端开发规范

- 使用 MyBatis Plus 进行数据操作
- 使用 Spring Security + JWT 进行身份认证
- 统一使用 `Result` 类封装响应结果
- 遵循 RESTful API 设计规范

### 前端开发规范

- 使用 Vue 3 Composition API
- 使用 Pinia 进行状态管理
- 使用 Element Plus 组件库
- 统一使用 `request.js` 封装 HTTP 请求

## 构建部署

### 后端构建

```bash
cd backend
mvn clean package -DskipTests
```

生成的 JAR 包位于 `backend/target/shop-backend-1.0.0.jar`

### 前端构建

```bash
cd frontend
npm run build
```

生成的静态文件位于 `frontend/dist/` 目录

## 许可证

MIT License
