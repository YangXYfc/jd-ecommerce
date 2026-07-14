# B2C 电商平台 (jd-ecommerce)

> 面向综合零售场景的 B2C 电商平台全栈项目，覆盖用户端、管理后台、商家后台三个前端，后端提供完整的 RESTful API。

---

## 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| ORM | MyBatis (注解模式) | 3.0.3 |
| 数据库 | MySQL | 8.0+ |
| 分页 | PageHelper | 1.4.7 |
| 认证 | JWT (jjwt) | 0.12.5 |
| 密码加密 | BCrypt (Spring Security Crypto) | — |
| API 文档 | Knife4j / OpenAPI 3 | 4.5.0 |
| 前端框架 | Vue 3 | 3.5.x |
| 构建工具 | Vite | 5.4.x |
| UI 组件 | Element Plus | 2.14.x |
| 状态管理 | Pinia | 3.0.x |
| 路由 | Vue Router | 4.6.x |
| HTTP | Axios | 1.18.x |
| 图表 | ECharts | 6.1.x (管理后台) |
| JDK | OpenJDK | 17+ |
| 构建工具 | Maven | 3.8+ |
| Node.js | — | 18+ |

---

## 项目结构

```
jd-ecommerce/
├── backend/                        # Spring Boot 后端
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd             # Maven Wrapper (无需全局安装 Maven)
│   └── src/main/
│       ├── java/com/jd/ecommerce/
│       │   ├── JdEcommerceApplication.java
│       │   ├── common/             # 统一响应(Result/PageResult)、全局异常处理
│       │   ├── config/             # WebConfig(CORS+拦截器)、SwaggerConfig、PasswordConfig
│       │   ├── controller/         # RESTful 控制器 (11个)
│       │   ├── dto/                # 数据传输对象 (14个)
│       │   ├── entity/             # 实体类 (14个)
│       │   ├── interceptor/        # JWT 拦截器
│       │   ├── mapper/             # MyBatis Mapper 接口 (14个, 注解模式)
│       │   ├── service/            # 业务逻辑层 (11个)
│       │   └── util/               # JwtUtil、RequestContextUtil
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           └── sql/
│               ├── schema.sql      # 建表脚本 (15张表)
│               └── data.sql        # 初始测试数据
├── frontend-user/                  # 用户端 Vue3 前端 (端口 5173)
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/                    # API 请求封装 (5个模块)
│       ├── components/             # 公共组件
│       ├── layouts/                # 布局组件
│       ├── router/                 # 路由配置
│       ├── stores/                 # Pinia 状态管理 (cart/user)
│       ├── views/                  # 页面视图 (15个)
│       └── utils/                  # 工具函数
├── frontend-admin/                 # 管理后台 Vue3 前端 (端口 5174)
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── router/
│       ├── stores/                 # admin store
│       ├── views/                  # 仪表盘/用户管理/商家审核/商品审核/订单/退款仲裁/日志/设置 (8个)
│       └── utils/
├── frontend-merchant/              # 商家后台 Vue3 前端 (端口 5175)
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/                    # API 请求封装
│       ├── components/             # 公共组件
│       ├── layouts/                # 布局组件
│       ├── router/                 # 路由配置
│       ├── stores/                 # merchant store
│       ├── views/                  # 店铺管理/商品管理/订单管理/退款审核/评价管理
│       └── utils/
├── docs/                           # 项目文档
│   ├── windows-setup-guide.md      # Windows 环境详细指南
│   ├── deployment-guide.md         # 部署指南 (Docker + Nginx)
│   ├── test-cases.md               # 测试用例文档 (55个用例)
│   ├── test-report.md              # 测试报告 v1.0
│   └── regression-test-report.md   # 回归测试报告 v1.1
├── scripts/                        # Windows 一键脚本
│   ├── check-env.bat               # 环境检查
│   ├── setup-env.bat               # 一键环境配置
│   ├── start-all.bat               # 一键启动全部服务
│   ├── stop-all.bat                # 一键停止全部服务
│   ├── start-backend.bat           # 单独启动后端
│   ├── start-frontend-user.bat     # 单独启动用户端
│   ├── start-frontend-admin.bat    # 单独启动管理后台
│   └── fix-frontend-deps.bat       # 修复前端依赖
├── .vscode/                        # VS Code 配置 (launch/tasks/settings/extensions)
└── README.md                       # 本文件
```

---

## 快速开始 (一键部署)

> **前置条件**: 确保已安装 JDK 17+、Node.js 18+、MySQL 8.0+。Maven 无需全局安装（项目自带 Maven Wrapper）。

### Windows 一键流程

```cmd
REM 1. 检查环境依赖
scripts\check-env.bat

REM 2. 一键配置（导入数据库 + 安装前端依赖 + 配置后端连接）
scripts\setup-env.bat

REM 3. 一键启动全部服务
scripts\start-all.bat

REM 4. 停止全部服务
scripts\stop-all.bat
```

启动后访问：

| 服务 | 地址 |
|------|------|
| 用户端 | http://localhost:5173 |
| 管理后台 | http://localhost:5174 |
| 商家后台 | http://localhost:5175 |
| 后端 API | http://localhost:8080/api |
| Swagger 文档 | http://localhost:8080/api/doc.html |

> 详细的 Windows 环境安装步骤请参考 [docs/windows-setup-guide.md](docs/windows-setup-guide.md)。
> 生产环境部署请参考 [docs/deployment-guide.md](docs/deployment-guide.md)。

---

## 手动安装指南

### 1. 环境准备

| 组件 | 最低版本 | 下载地址 |
|------|----------|----------|
| JDK | 17 | https://adoptium.net/temurin/releases/?version=17 |
| Maven | 3.8 (或使用项目自带 mvnw) | https://maven.apache.org/download.cgi |
| MySQL | 8.0 | https://dev.mysql.com/downloads/installer/ |
| Node.js | 18 | https://nodejs.org/ |

### 2. 数据库配置

```bash
# 登录 MySQL
mysql -u root -p

# 执行建表脚本
mysql> source backend/src/main/resources/sql/schema.sql;

# 执行初始数据脚本
mysql> source backend/src/main/resources/sql/data.sql;
```

修改 `backend/src/main/resources/application-dev.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jd_ecommerce?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: <你的MySQL密码>
```

### 3. 后端启动

```bash
cd backend
mvn spring-boot:run          # 或使用 Maven Wrapper: mvnw.cmd spring-boot:run
```

- API 地址: http://localhost:8080/api
- Swagger 文档: http://localhost:8080/api/doc.html

### 4. 前端启动

#### 用户端 (端口 5173)

```bash
cd frontend-user
npm install
npm run dev
```

#### 管理后台 (端口 5174)

```bash
cd frontend-admin
npm install
npm run dev
```

#### 商家后台 (端口 5175)

```bash
cd frontend-merchant
npm install
npm run dev
```

---

## 测试账号

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

---

## API 文档

项目集成 Knife4j (OpenAPI 3)，启动后端后访问：

- **Swagger UI**: http://localhost:8080/api/doc.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs

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

### 主要接口分组

| 分组 | 路径前缀 | 说明 |
|------|----------|------|
| 认证 | /api/auth | 登录、注册、商家入驻 |
| 商品 | /api/products | 商品列表、详情、SKU |
| 分类 | /api/categories | 分类列表、分类树 |
| 用户 | /api/users | 个人信息、密码修改 |
| 地址 | /api/addresses | 收货地址 CRUD |
| 购物车 | /api/cart | 加购、修改、删除、全选 |
| 订单 | /api/orders | 下单、支付、取消、确认收货 |
| 退款 | /api/refunds | 申请退款、商家审核、用户申诉、管理员仲裁 |
| 评价 | /api/reviews | 创建评价、商品评价列表、商家回复 |
| 商家 | /api/merchant | 店铺信息、商品管理、发货 |
| 管理员 | /api/admin | 仪表盘、用户管理、商家审核、商品审核、操作日志 |
| 轮播图 | /api/banners | 轮播图管理 |

---

## 核心功能概览

| 模块 | 说明 |
|------|------|
| 订单全流程 | 下单 → 模拟支付 → 商家发货 → 确认收货 → 评价；支持多商家拆单、库存乐观锁防超卖、地址快照 |
| 退款全流程 | 申请 → 商家审核 → 用户寄回 → 商家确认 → 退款完成；支持商家拒绝后用户申诉、管理员仲裁 |
| 商品管理 | 商家发布 → 管理员审核 → 上架；SKU 多规格、库存管理、搜索/分类/排序 |
| 用户系统 | 注册/登录(JWT)、商家入驻、收货地址 CRUD、个人信息管理 |
| 购物车 | 加购合并、库存校验、部分更新、三层权限防御 |
| 管理后台 | 仪表盘统计、用户管理、商家审核、商品审核、退款仲裁、操作日志 |

### 角色权限

| 角色 | 权限范围 |
|------|----------|
| 普通用户 (USER) | 浏览商品、下单、退款申请、评价、购物车、地址管理 |
| 商家 (MERCHANT) | 商品CRUD、订单发货、退款审核、店铺管理、评价回复 |
| 管理员 (ADMIN) | 全局订单查看、退款仲裁、商家审核、商品审核、用户管理、数据统计 |

---

## 常见问题 FAQ

<details>
<summary><b>端口被占用怎么办？</b></summary>

```cmd
REM 查看占用端口的进程
netstat -ano | findstr :8080

REM 终止进程
taskkill /PID <PID> /F
```

或直接运行 `scripts\stop-all.bat` 停止所有服务。
</details>

<details>
<summary><b>MySQL 连接被拒绝？</b></summary>

1. 确认 MySQL 服务正在运行：`sc query MySQL80`
2. 确认 `application-dev.yml` 中的用户名和密码正确
3. 确认数据库 `jd_ecommerce` 已创建
</details>

<details>
<summary><b>npm install 失败/缓慢？</b></summary>

```cmd
npm config set registry https://registry.npmmirror.com
npm cache clean --force
```

或运行 `scripts\fix-frontend-deps.bat` 重新安装前端依赖。
</details>

<details>
<summary><b>Maven 依赖下载失败？</b></summary>

配置阿里云镜像，编辑 `%USERPROFILE%\.m2\settings.xml`：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```
</details>

<details>
<summary><b>中文乱码问题？</b></summary>

CMD 中执行 `chcp 65001` 切换为 UTF-8 编码。确认 MySQL 字符集为 `utf8mb4`。
</details>

<details>
<summary><b>前端页面空白 / API 404？</b></summary>

1. 确认后端已启动：访问 http://localhost:8080/api/doc.html
2. 检查 `vite.config.js` 中 proxy 配置指向 `http://localhost:8080`
3. 确认数据库已导入测试数据
</details>

---

## 测试

| 文档 | 说明 |
|------|------|
| [docs/test-cases.md](docs/test-cases.md) | 测试用例文档 (55个用例, 8个模块) |
| [docs/test-report.md](docs/test-report.md) | 测试报告 v1.0 |
| [docs/regression-test-report.md](docs/regression-test-report.md) | 回归测试报告 v1.1 |

运行后端单元测试：

```bash
cd backend
mvn test
```

---

## 项目团队

| 角色 | 负责人 | 职责 |
|------|--------|------|
| 项目管理 | admin | 任务拆分与协调 |
| 数据库 | 后端大师-王拜佛 | 数据库设计与建表 |
| 后端开发 | 后端大师-王拜佛 | Spring Boot API 开发 |
| 前端开发 | 前端工程师 | 用户端/管理后台/商家后台 Vue3 开发 |
| 测试运维 | 测试运维-菜菜 | 测试用例、测试执行、回归验证、部署文档 |

## License

MIT
