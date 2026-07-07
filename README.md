# 京东风格电商平台 (jd-ecommerce)

> 一个仿京东风格的 B2C 电商平台全栈项目，覆盖用户端、管理后台、商家后台等多个端侧。

## 项目概览

| 维度 | 说明 |
|------|------|
| 项目类型 | B2C 电商平台全栈项目 |
| 后端 | Spring Boot 3.2.5 + MyBatis (注解模式) + MySQL 8.0+ |
| 前端 | Vue 3 + Vite 5 + Element Plus + Pinia + Vue Router |
| 认证 | JWT (jjwt 0.12.5) |
| API 文档 | Knife4j / OpenAPI 3 (Swagger) |
| 分页 | PageHelper |
| 密码加密 | BCrypt (Spring Security Crypto) |
| 代码规模 | 后端 81 个 Java 文件 / 前端 33 个 Vue 组件 |
| 数据库 | 15 张表 |

## 技术栈版本

| 组件 | 版本 |
|------|------|
| JDK | 17+ |
| Spring Boot | 3.2.5 |
| MyBatis Spring Boot | 3.0.3 |
| PageHelper | 1.4.7 |
| jjwt | 0.12.5 |
| Knife4j | 4.5.0 |
| MySQL | 8.0+ |
| Vue | 3.5.x |
| Vite | 5.4.x |
| Element Plus | 2.14.x |
| Pinia | 3.0.x |
| Vue Router | 4.6.x |
| Axios | 1.18.x |
| ECharts | 6.1.x (管理后台) |

## 项目结构

```
jd-ecommerce/
├── backend/                    # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/jd/ecommerce/
│       │   ├── JdEcommerceApplication.java
│       │   ├── common/         # 统一响应(Result/PageResult)、全局异常处理(BusinessException)
│       │   ├── config/         # WebConfig(CORS+拦截器)、SwaggerConfig、PasswordConfig
│       │   ├── controller/     # RESTful 控制器 (11个)
│       │   ├── dto/            # 数据传输对象 (14个)
│       │   ├── entity/         # 实体类 (14个)
│       │   ├── interceptor/    # JWT 拦截器
│       │   ├── mapper/         # MyBatis Mapper 接口 (14个, 注解模式)
│       │   ├── service/        # 业务逻辑层 (11个)
│       │   └── util/           # JwtUtil、RequestContextUtil
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           └── sql/
│               ├── schema.sql  # 建表脚本 (15张表)
│               └── data.sql    # 初始测试数据
├── frontend-user/              # 用户端 Vue3 前端 (端口 5173)
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/               # API 请求封装 (5个模块)
│       ├── components/        # 公共组件 (AppHeader/Footer/ProductCard)
│       ├── layouts/           # 布局组件
│       ├── router/            # 路由配置
│       ├── stores/            # Pinia 状态管理 (cart/user)
│       ├── views/             # 页面视图 (15个)
│       └── utils/             # 工具函数 (request/mock-data)
├── frontend-admin/             # 管理后台 Vue3 前端 (端口 5174)
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── router/
│       ├── stores/            # admin store
│       ├── views/             # 仪表盘/用户管理/商家审核/商品审核/订单/退款仲裁/日志/设置 (8个)
│       └── utils/
├── docs/                       # 项目文档
│   ├── test-cases.md           # 测试用例文档 (55个用例)
│   ├── test-report.md          # 测试报告 v1.0
│   └── regression-test-report.md  # 回归测试报告 v1.1
└── tasks.json                  # 项目任务定义
```

## 核心功能模块

### 1. 订单管理全流程（核心）

```
下单 → 模拟支付 → 商家发货 → 用户确认收货 → 评价
  ↓
取消(仅待支付状态)
```

- **多商家订单**: 下单时按商家分组，每个商家生成独立订单
- **库存管理**: 下单扣库存(乐观锁 `stock >= #{quantity}` 防超卖)，取消订单恢复库存
- **地址快照**: 下单时保存收货地址 JSON 快照，不因后续修改地址影响订单
- **事务管理**: createOrder/payOrder/cancelOrder/shipOrder/confirmReceive 均使用 `@Transactional`

**订单状态机:**

| 状态值 | 常量 | 说明 | 触发动作 |
|--------|------|------|----------|
| 0 | STATUS_PENDING_PAY | 待支付 | 创建订单 |
| 1 | STATUS_PENDING_SHIP | 待发货 | 模拟支付 |
| 2 | STATUS_SHIPPED | 已发货 | 商家发货 |
| 3 | STATUS_RECEIVED | 已收货 | 用户确认收货 |
| 4 | STATUS_REVIEWED | 已评价 | 用户评价 |
| 5 | STATUS_CANCELLED | 已取消 | 用户取消(仅待支付) |

### 2. 退款全流程（核心）

```
正常流程:  申请 → 商家审核通过 → 用户寄回 → 商家确认 → 退款完成
异常流程A: 商家拒绝 → 用户申诉 → 管理员仲裁(支持/拒绝)
异常流程B: 商家超时(48h) → 用户申诉 → 管理员仲裁
```

- **超时机制**: 商家 48 小时未审核，用户可直接申诉
- **申诉条件**: 仅 `MERCHANT_REJECTED` 或 `PENDING + 超时` 状态可申诉
- **仲裁权限**: 仅 ADMIN 角色可仲裁，仅 `APPEALED` 状态可仲裁
- **操作日志**: 仲裁操作自动记录 admin_log

**退款状态机:**

| 状态值 | 常量 | 说明 |
|--------|------|------|
| 0 | STATUS_PENDING | 待商家审核 |
| 1 | STATUS_MERCHANT_APPROVED | 商家通过 |
| 2 | STATUS_RETURNING | 用户寄回中 |
| 3 | STATUS_COMPLETED | 退款完成 |
| 4 | STATUS_MERCHANT_REJECTED | 商家拒绝 |
| 5 | STATUS_APPEALED | 用户申诉中 |
| 6 | STATUS_ADMIN_APPROVED | 管理员支持退款 |
| 7 | STATUS_ADMIN_REJECTED | 管理员拒绝退款 |

### 3. 商品管理

- 商家发布商品 → 管理员审核 → 上架 → 用户浏览购买
- SKU 多规格管理、库存管理、上下架
- 商品搜索(关键词)、分类筛选、排序(销量/价格升序/价格降序)
- 未审核商家(auditStatus≠1)不可发布商品

**商品状态:**

| 状态值 | 说明 |
|--------|------|
| 0 | 待审核 |
| 1 | 上架(在售) |
| 2 | 下架 |
| 3 | 审核拒绝 |

### 4. 用户系统

- 注册/登录(JWT 认证)
- 用户名唯一、手机号唯一+格式校验
- 商家入驻: 注册创建 User(role=MERCHANT) + Merchant(auditStatus=0) 双记录
- 收货地址 CRUD、默认地址管理
- 个人信息修改、密码修改
- 禁用用户(status=0)无法登录

### 5. 购物车

- 加购合并: 同一 SKU 重复加购自动合并数量
- 库存校验: 加购时检查 SKU 库存
- 部分更新: 支持只改数量或只改选中状态(quantity/selected 均可选)
- 数量 ≤ 0 自动删除
- 选中/取消选中、全选/取消全选
- **权限校验**: 删除/修改购物车项校验 cartId 归属当前用户(三层防御)

### 6. 管理员后台

- 仪表盘数据统计(总销售额/总订单数/总用户数/今日订单)
- 用户管理(搜索/筛选/禁用/启用)
- 商家审核(入驻审批)
- 商品审核
- 全局订单视图
- 退款仲裁(两个独立端点: admin-approve / admin-reject)
- 操作日志查询(按操作类型筛选)
- 轮播图管理

## 角色权限

| 角色 | 权限范围 |
|------|----------|
| 普通用户 (USER) | 浏览商品、下单、退款申请、评价、购物车、地址管理 |
| 商家 (MERCHANT) | 商品CRUD、订单发货、退款审核、店铺管理、评价回复 |
| 管理员 (ADMIN) | 全局订单查看、退款仲裁、商家审核、商品审核、用户管理、数据统计、轮播图管理 |

### 权限控制架构

```
请求 → JwtInterceptor (验证Token, 提取userId/role)
     → Controller (从 RequestContextUtil 获取 userId)
     → Service (校验资源归属: userId / merchantId)
     → Mapper (SQL WHERE 条件带 user_id 防御)
```

- **JWT 拦截器**: 拦截所有请求(除白名单)，验证 Bearer Token
- **角色校验**: AdminController 所有接口调用 `RequestContextUtil.requireRole("ADMIN")`
- **商家校验**: 商家接口通过 `merchantService.findByUserId(userId)` 校验，非商家返回 403
- **资源归属校验**: 订单/退款/购物车操作均校验 userId/merchantId 归属
- **公开接口白名单**: 登录/注册/商品浏览/分类/轮播图/Swagger

## Windows 快速开始

> 如果你使用 Windows 10/11，可以通过脚本一键完成环境配置和项目启动。
> 详细的手动配置步骤请参考 [docs/windows-setup-guide.md](docs/windows-setup-guide.md)。

### 一键环境配置

```cmd
REM 第 1 步：检查环境依赖（JDK / Maven / MySQL / Node.js）
scripts\check-env.bat

REM 第 2 步：一键配置（检查依赖 + 导入数据库 + 安装前端依赖）
scripts\setup-env.bat

REM 或使用 PowerShell 版本
powershell -ExecutionPolicy Bypass -File scripts\setup-env.ps1
```

### 一键启动

```cmd
REM 第 3 步：一键启动后端 + 用户端 + 管理后台（各开一个新窗口）
scripts\start-all.bat
```

启动后访问：

| 服务 | 地址 |
|------|------|
| 用户端 | http://localhost:5173 |
| 管理后台 | http://localhost:5174 |
| 后端 API | http://localhost:8080/api |
| Swagger 文档 | http://localhost:8080/api/doc.html |

### VS Code 使用指引

项目已预设 VS Code 配置（`.vscode/` 目录），推荐使用 VS Code 进行开发。

**推荐插件**（打开项目时会自动提示安装）：

| 插件 | 用途 |
|------|------|
| Extension Pack for Java | Java 语言支持 + 调试 + Maven |
| Spring Boot Extension Pack | Spring Boot 开发支持 |
| Vue - Official (Volar) | Vue 3 语法支持 |
| MySQL (Weijan Chen) | VS Code 内 MySQL 客户端 |
| Prettier | 代码格式化 |

**调试与运行**：

- 按 `F5` 启动调试，可在「运行和调试」面板选择配置：
  - **Spring Boot (backend)** — 后端调试（支持断点）
  - **Vite: 用户端 (frontend-user)** — 用户端开发服务器
  - **Vite: 管理后台 (frontend-admin)** — 管理后台开发服务器
- 按 `Ctrl+Shift+P` → `Tasks: Run Task` 可执行预设任务（MySQL 导入、Maven 启动/打包、前端 install/build）

> 完整的 Windows 环境配置指南（含 JDK / Maven / MySQL / Node.js 手动安装步骤、常见问题排查）请参考 [docs/windows-setup-guide.md](docs/windows-setup-guide.md)。

---

## 本地开发环境搭建

### 前置要求

- **JDK** 17 或以上
- **Maven** 3.8 或以上
- **MySQL** 8.0 或以上
- **Node.js** 18 或以上
- **npm** 或 **pnpm**

### 1. 数据库配置

```bash
# 登录 MySQL
mysql -u root -p

# 执行建表脚本
mysql> source backend/src/main/resources/sql/schema.sql;

# 执行初始数据脚本
mysql> source backend/src/main/resources/sql/data.sql;
```

或通过命令行导入:

```bash
mysql -u root -p < backend/src/main/resources/sql/schema.sql
mysql -u root -p < backend/src/main/resources/sql/data.sql
```

### 2. 后端配置与启动

#### 2.1 修改数据库连接

编辑 `backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jd_ecommerce?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root        # 修改为你的 MySQL 用户名
    password: root        # 修改为你的 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

#### 2.2 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动后:
- API 地址: http://localhost:8080/api
- Swagger 文档: http://localhost:8080/api/doc.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs

### 3. 前端启动

#### 3.1 用户端

```bash
cd frontend-user
npm install
npm run dev
```

用户端访问: http://localhost:5173

#### 3.2 管理后台

```bash
cd frontend-admin
npm install
npm run dev
```

管理后台访问: http://localhost:5174

### 4. 测试账号

| 角色 | 用户名 | 密码 | userId | 说明 |
|------|--------|------|--------|------|
| 管理员 | admin | 123456 | 1 | 管理后台登录 |
| 商家 | merchant1 | 123456 | 2 | 数码旗舰店(已审核) |
| 商家 | merchant2 | 123456 | 3 | 服饰优选店(已审核) |
| 普通用户 | user1 | 123456 | 4 | 张三 |
| 普通用户 | user2 | 123456 | 5 | 李四 |
| 普通用户 | user3 | 123456 | 6 | 王五 |
| 待审核商家 | merchant3 | 123456 | 7 | 待审核 |

> 所有密码均为 `123456` 的 BCrypt 加密存储。

## API 接口概览

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误/业务校验失败 |
| 401 | 未登录/Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/merchant/register | 商家入驻注册 |
| GET | /api/products/list | 商品列表(分页/搜索/分类/排序) |
| GET | /api/products/detail/{id} | 商品详情 |
| GET | /api/products/{id}/skus | 商品SKU列表 |
| GET | /api/products/category/{categoryId} | 按分类获取商品 |
| GET | /api/categories | 分类列表 |
| GET | /api/categories/tree | 分类树 |
| GET | /api/banners | 轮播图列表 |

### 用户接口（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/users/me | 当前用户信息 |
| PUT | /api/users/me | 修改个人信息 |
| PUT | /api/users/me/password | 修改密码 |
| GET | /api/addresses | 地址列表 |
| POST | /api/addresses | 添加地址 |
| PUT | /api/addresses/{id} | 修改地址 |
| DELETE | /api/addresses/{id} | 删除地址 |
| PUT | /api/addresses/{id}/default | 设为默认地址 |
| GET | /api/cart | 购物车列表 |
| POST | /api/cart | 加入购物车 |
| PUT | /api/cart/{id} | 修改购物车项(数量/选中, 均可选) |
| DELETE | /api/cart/{id} | 删除购物车项 |
| PUT | /api/cart/selectAll | 全选/取消全选 |
| POST | /api/orders | 创建订单(选中即结算) |
| GET | /api/orders | 用户订单列表(分页/状态筛选) |
| GET | /api/orders/{id} | 订单详情 |
| GET | /api/orders/{id}/items | 订单明细 |
| POST | /api/orders/{id}/pay | 模拟支付 |
| POST | /api/orders/{id}/cancel | 取消订单(仅待支付) |
| POST | /api/orders/{id}/receive | 确认收货 |
| POST | /api/refunds | 申请退款 |
| GET | /api/refunds | 用户退款列表 |
| GET | /api/refunds/{id} | 退款详情 |
| POST | /api/refunds/{id}/return-logistics | 填写寄回物流 |
| POST | /api/refunds/{id}/appeal | 申诉(商家拒绝/超时) |
| POST | /api/reviews | 创建评价(仅已收货订单) |
| GET | /api/reviews/product/{productId} | 商品评价列表 |

### 商家接口（需商家角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/merchant/info | 商家信息 |
| PUT | /api/merchant/info | 修改店铺信息 |
| GET | /api/merchant/configs | 店铺配置 |
| POST | /api/merchant/configs | 保存店铺配置 |
| GET | /api/orders/merchant | 商家订单列表 |
| POST | /api/orders/{id}/ship | 商家发货 |
| POST | /api/products | 发布商品(需审核通过) |
| PUT | /api/products/{id} | 编辑商品 |
| POST | /api/products/{id}/status | 上下架商品 |
| GET | /api/products/merchant | 商家商品列表 |
| GET | /api/refunds/merchant | 商家退款列表 |
| POST | /api/refunds/{id}/approve | 商家审核通过 |
| POST | /api/refunds/{id}/reject | 商家审核拒绝 |
| POST | /api/refunds/{id}/confirm-receive | 商家确认收货→退款完成 |
| POST | /api/reviews/{id}/reply | 商家回复评价 |

### 管理员接口（需管理员角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/dashboard | 仪表盘统计 |
| GET | /api/admin/users | 用户列表(搜索/筛选) |
| PUT | /api/admin/users/{id}/status | 禁用/启用用户 |
| GET | /api/admin/merchants | 商家列表 |
| POST | /api/admin/merchants/{id}/audit | 审核商家 |
| GET | /api/admin/products | 商品列表(按状态) |
| POST | /api/admin/products/{id}/audit | 审核商品 |
| GET | /api/admin/logs | 操作日志(按类型筛选) |
| GET | /api/refunds/admin | 管理员退款列表(待仲裁) |
| POST | /api/refunds/{id}/admin-approve | 管理员仲裁-支持退款 |
| POST | /api/refunds/{id}/admin-reject | 管理员仲裁-拒绝退款 |
| GET | /api/banners/all | 轮播图列表(含隐藏) |
| POST | /api/banners | 添加轮播图 |
| PUT | /api/banners/{id} | 修改轮播图 |
| DELETE | /api/banners/{id} | 删除轮播图 |

## 数据库设计

### ER 概览

```
user ──┬── merchant ──┬── product ── product_sku
       │              ├── shop_config
       ├── address
       ├── cart ────── product_sku
       ├── orders ──── order_item ── product_sku
       │         └── payment
       ├── refund ─── orders
       └── review ─── orders, product

admin_log ── user (admin)
banner (独立)
category (树形结构)
```

### 表清单 (15张表)

| # | 表名 | 说明 | 关键字段 |
|---|------|------|----------|
| 1 | user | 用户表 | id, username, password(BCrypt), role, status |
| 2 | merchant | 商家表 | user_id(FK), shop_name, audit_status |
| 3 | category | 商品分类表 | parent_id(树形), sort, status |
| 4 | product | 商品表 | merchant_id(FK), category_id(FK), status, sales_count |
| 5 | product_sku | 商品SKU表 | product_id(FK), price, stock, attributes(JSON) |
| 6 | address | 收货地址表 | user_id(FK), is_default |
| 7 | cart | 购物车表 | user_id(FK), product_sku_id(FK), quantity, selected |
| 8 | orders | 订单表 | order_no(UK), user_id, merchant_id, status, address_snapshot |
| 9 | order_item | 订单明细表 | order_id(FK), product_sku_id(FK), 快照字段 |
| 10 | refund | 退款表 | refund_no(UK), order_id, status, timeout_hours |
| 11 | review | 评价表 | order_id, product_id, user_id, rating, merchant_reply |
| 12 | admin_log | 管理员操作日志表 | admin_id, action, target_type, target_id |
| 13 | payment | 支付记录表(模拟) | payment_no(UK), order_id, pay_method |
| 14 | banner | 轮播图表 | sort, status |
| 15 | shop_config | 店铺设置表 | merchant_id, config_key, config_value |

## 部署文档 (Docker)

### Docker Compose 部署

#### 1. 创建 `docker-compose.yml`

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: jd-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: jd_ecommerce
      MYSQL_CHARSET: utf8mb4
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backend/src/main/resources/sql/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
      - ./backend/src/main/resources/sql/data.sql:/docker-entrypoint-initdb.d/02-data.sql
    networks:
      - jd-network

  backend:
    build: ./backend
    container_name: jd-backend
    depends_on:
      - mysql
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jd_ecommerce?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root
    ports:
      - "8080:8080"
    networks:
      - jd-network

  frontend-user:
    build: ./frontend-user
    container_name: jd-frontend-user
    depends_on:
      - backend
    ports:
      - "5173:5173"
    networks:
      - jd-network

  frontend-admin:
    build: ./frontend-admin
    container_name: jd-frontend-admin
    depends_on:
      - backend
    ports:
      - "5174:5174"
    networks:
      - jd-network

volumes:
  mysql_data:

networks:
  jd-network:
    driver: bridge
```

#### 2. 后端 Dockerfile

创建 `backend/Dockerfile`:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/jd-ecommerce-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 3. 前端 Dockerfile

创建 `frontend-user/Dockerfile` 和 `frontend-admin/Dockerfile`:

```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:18-alpine
WORKDIR /app
RUN npm install -g vite
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/vite.config.js .
EXPOSE 5173
CMD ["npx", "vite", "preview", "--host", "0.0.0.0", "--port", "5173"]
```

> 管理后台 Dockerfile 同理，端口改为 5174。

#### 4. 启动

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f backend

# 停止
docker-compose down
```

### 生产环境 Nginx 反向代理（可选）

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 用户端前端
    location / {
        proxy_pass http://127.0.0.1:5173;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 管理后台前端
    location /admin {
        proxy_pass http://127.0.0.1:5174;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 后端 API
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## 测试

### 测试文档

| 文档 | 说明 |
|------|------|
| [docs/test-cases.md](docs/test-cases.md) | 测试用例文档 (55个用例, 8个模块) |
| [docs/test-report.md](docs/test-report.md) | 测试报告 v1.0 (初次测试) |
| [docs/regression-test-report.md](docs/regression-test-report.md) | 回归测试报告 v1.1 (Bug修复后) |

### 测试结果摘要

| 指标 | v1.0 (初次) | v1.1 (回归) |
|------|-------------|-------------|
| 总用例数 | 55 | 5 (回归) |
| 通过 | 50 | 5 |
| 失败 | 4 | 0 |
| 阻塞 | 1 | 1 (环境限制) |
| 通过率 | 91% | 100% |
| 高优先级问题 | 2 | 0 |
| 中优先级问题 | 2 | 0 |

### 测试覆盖模块

| 模块 | 用例数 | P0 | P1 | P2 |
|------|--------|----|----|-----|
| 订单全流程 | 9 | 3 | 5 | 1 |
| 退款流程 | 9 | 4 | 4 | 1 |
| 权限测试 | 7 | 4 | 3 | 0 |
| 商品流程 | 5 | 1 | 4 | 0 |
| 购物车 | 8 | 2 | 5 | 1 |
| 用户系统 | 8 | 2 | 4 | 2 |
| 评价模块 | 4 | 1 | 2 | 1 |
| 管理员模块 | 5 | 2 | 2 | 1 |
| **合计** | **55** | **19** | **29** | **7** |

### 已修复 Bug 清单

| Bug | 严重级别 | 描述 | 修复方案 |
|-----|----------|------|----------|
| Bug 1 | 高 | CartController.delete 无权限校验 | 三层防御: Controller传userId → Service校验归属 → Mapper SQL带user_id |
| Bug 2 | 高 | CartController.update 无权限校验 | 同上, updateCart/updateQuantity/updateSelected均带userId |
| Bug 3 | 中 | CartUpdateRequest.quantity @NotNull | 移除@NotNull, 支持部分更新 |
| Bug 4 | 中 | OrderCreateRequest.cartIds 未使用 | 移除cartIds字段, 统一"选中即结算"设计 |
| Bug 5 | 中 | 退款仲裁路径不明确 | 确认为两个独立端点: /admin-approve 和 /admin-reject |

### 运行后端单元测试

```bash
cd backend
mvn test
```

## 开发指南

### 分支策略

- `main` - 生产分支
- `dev` - 开发分支
- `feature/*` - 功能分支

### 代码规范

- 后端遵循标准 Java 编码规范
- 前端遵循 Vue 3 Composition API 最佳实践
- API 遵循 RESTful 设计规范
- 统一响应格式: `{ code, message, data }`
- 全局异常处理: BusinessException + GlobalExceptionHandler

### 添加新接口

1. 在 `controller/` 中添加 Controller 方法
2. 在 `service/` 中实现业务逻辑
3. 在 `mapper/` 中添加数据访问方法（注解模式）
4. 在 `dto/` 中添加请求/响应 DTO（如需要）
5. 更新 Swagger 注解

### 关键设计决策

| 决策 | 说明 |
|------|------|
| 选中即结算 | 下单时自动取购物车 selected=1 的商品，无需前端传 cartIds |
| 购物车部分更新 | quantity/selected 均可选，支持灵活的部分更新 |
| 退款仲裁双端点 | /admin-approve 和 /admin-reject 两个独立端点，RESTful 语义清晰 |
| 三层权限防御 | Controller → Service → Mapper SQL 全部校验 userId |
| 地址快照 | 下单时保存地址 JSON 快照，不受后续地址修改影响 |
| 多商家拆单 | 按商家分组生成独立订单 |

## 项目团队

| 角色 | 负责人 | 职责 |
|------|--------|------|
| 项目管理 | admin | 任务拆分与协调 |
| Git仓库 | pt-git-assist | 仓库搭建与项目初始化 |
| 数据库 | 后端大师-王拜佛 | 数据库设计与建表 |
| 后端开发 | 后端大师-王拜佛 | Spring Boot API 开发 |
| 前端开发 | 前端工程师 | 用户端/管理后台/商家后台 Vue3 开发 |
| 测试运维 | 测试运维-菜菜 | 测试用例编写、测试执行、回归验证、部署文档 |

## License

MIT
