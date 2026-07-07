# 京东风格电商平台 - 测试用例文档

> 项目: jd-ecommerce  
> 编写人: 测试运维-菜菜 (pt-dev2)  
> 日期: 2026-07-07  
> 版本: v1.0

---

## 目录

1. [测试概述](#1-测试概述)
2. [测试环境](#2-测试环境)
3. [订单全流程测试](#3-订单全流程测试)
4. [退款流程测试](#4-退款流程测试)
5. [权限测试](#5-权限测试)
6. [商品流程测试](#6-商品流程测试)
7. [购物车测试](#7-购物车测试)
8. [用户系统测试](#8-用户系统测试)
9. [评价模块测试](#9-评价模块测试)
10. [管理员模块测试](#10-管理员模块测试)

---

## 1. 测试概述

### 1.1 测试目标

验证京东风格电商平台各核心业务模块的功能正确性、状态流转合法性、权限控制有效性，确保订单全流程（含异常分支）、退款全流程（含超时/申诉/仲裁）、商品审核、购物车、权限隔离等关键功能按预期工作。

### 1.2 测试账号

| 角色 | 用户名 | 密码 | userId | 说明 |
|------|--------|------|--------|------|
| 管理员 | admin | 123456 | 1 | 拥有全部管理员权限 |
| 商家 | merchant1 | 123456 | 2 | 数码旗舰店，已审核通过 |
| 商家 | merchant2 | 123456 | 3 | 服饰优选店，已审核通过 |
| 普通用户 | user1 | 123456 | 4 | 张三，有收货地址 |
| 普通用户 | user2 | 123456 | 5 | 李四，有收货地址 |
| 普通用户 | user3 | 123456 | 6 | 王五，有收货地址 |
| 待审核商家 | merchant3 | 123456 | 7 | 待审核商家 |

### 1.3 API 基础地址

```
后端: http://localhost:8080/api
用户端前端: http://localhost:5173
管理后台前端: http://localhost:5174
```

### 1.4 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

- `code=200` 表示成功
- `code=401` 表示未登录/Token过期
- `code=403` 表示无权限
- `code=404` 表示资源不存在
- `code=400` 表示参数错误/业务校验失败
- `code=500` 表示服务器内部错误

### 1.5 订单状态常量

| 状态值 | 常量名 | 说明 |
|--------|--------|------|
| 0 | STATUS_PENDING_PAY | 待支付 |
| 1 | STATUS_PENDING_SHIP | 待发货 |
| 2 | STATUS_SHIPPED | 已发货 |
| 3 | STATUS_RECEIVED | 已收货 |
| 4 | STATUS_REVIEWED | 已评价 |
| 5 | STATUS_CANCELLED | 已取消 |

### 1.6 退款状态常量

| 状态值 | 常量名 | 说明 |
|--------|--------|------|
| 0 | STATUS_PENDING | 待商家审核 |
| 1 | STATUS_MERCHANT_APPROVED | 商家通过 |
| 2 | STATUS_RETURNING | 用户寄回中 |
| 3 | STATUS_COMPLETED | 退款完成 |
| 4 | STATUS_MERCHANT_REJECTED | 商家拒绝 |
| 5 | STATUS_APPEALED | 用户申诉中 |
| 6 | STATUS_ADMIN_APPROVED | 管理员支持退款 |
| 7 | STATUS_ADMIN_REJECTED | 管理员拒绝退款 |

### 1.7 商品状态常量

| 状态值 | 说明 |
|--------|------|
| 0 | 待审核 |
| 1 | 上架(在售) |
| 2 | 下架 |
| 3 | 审核拒绝 |

---

## 2. 测试环境

### 2.1 后端环境

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Spring Boot 3.2.5
- MyBatis (注解模式)
- PageHelper 分页插件

### 2.2 前端环境

- Node.js 18+
- npm 或 pnpm
- Vue 3 + Vite 5
- Element Plus
- Pinia + Vue Router

### 2.3 数据库准备

```bash
# 登录 MySQL
mysql -u root -p

# 执行建表脚本
source backend/src/main/resources/sql/schema.sql

# 执行初始数据脚本
source backend/src/main/resources/sql/data.sql
```

### 2.4 服务启动

```bash
# 启动后端
cd backend
mvn spring-boot:run

# 启动用户端前端
cd frontend-user
npm install
npm run dev

# 启动管理后台前端
cd frontend-admin
npm install
npm run dev
```

---

## 3. 订单全流程测试

### 3.1 正常流程: 下单→支付→发货→收货→评价

| 用例ID | TC-ORD-001 |
|--------|-----------|
| **用例名称** | 订单正常全流程 |
| **前置条件** | 用户 user1 已登录，购物车中有选中商品，有收货地址 |
| **测试步骤** | |
| Step 1 | 用户登录: `POST /api/auth/login`，body: `{"username":"user1","password":"123456"}`，获取 token |
| Step 2 | 查看购物车: `GET /api/cart`，确认有 selected=1 的商品 |
| Step 3 | 创建订单: `POST /api/orders`，body: `{"addressId":1,"remark":"测试订单"}` |
| Step 4 | 验证返回订单 status=0(待支付)，orderNo 不为空 |
| Step 5 | 验证购物车中选中商品已被清空 |
| Step 6 | 模拟支付: `POST /api/orders/{orderId}/pay` |
| Step 7 | 验证订单 status 变为 1(待发货)，payTime 不为空 |
| Step 8 | 商家登录: `POST /api/auth/login`，body: `{"username":"merchant1","password":"123456"}` |
| Step 9 | 商家发货: `POST /api/orders/{orderId}/ship`，body: `{"logisticsCompany":"顺丰速运","logisticsNo":"SF1234567890"}` |
| Step 10 | 验证订单 status 变为 2(已发货)，logisticsCompany/logisticsNo 不为空 |
| Step 11 | 用户确认收货: `POST /api/orders/{orderId}/receive` |
| Step 12 | 验证订单 status 变为 3(已收货) |
| Step 13 | 用户评价: `POST /api/reviews`，body: `{"orderId":{orderId},"productId":1,"content":"商品不错","rating":5}` |
| Step 14 | 验证订单 status 变为 4(已评价) |
| Step 15 | 验证评价记录可通过 `GET /api/reviews/product/{productId}` 查询到 |
| **预期结果** | 订单状态完整流转: 0→1→2→3→4，各环节时间字段正确填充 |
| **优先级** | P0 |

### 3.2 取消订单 - 待支付时取消

| 用例ID | TC-ORD-002 |
|--------|-----------|
| **用例名称** | 待支付状态取消订单 |
| **前置条件** | 用户 user1 已登录，已创建订单且未支付(status=0) |
| **测试步骤** | |
| Step 1 | 创建订单，获取 orderId |
| Step 2 | 取消订单: `POST /api/orders/{orderId}/cancel?reason=不想买了` |
| Step 3 | 查询订单详情: `GET /api/orders/{orderId}` |
| **预期结果** | 订单 status=5(已取消)，cancelTime 不为空，cancelReason="不想买了"；库存已恢复 |
| **优先级** | P0 |

### 3.3 取消订单 - 待发货时取消（应失败）

| 用例ID | TC-ORD-003 |
|--------|-----------|
| **用例名称** | 待发货状态取消订单（不允许） |
| **前置条件** | 订单已支付(status=1) |
| **测试步骤** | |
| Step 1 | 对已支付订单执行取消: `POST /api/orders/{orderId}/cancel` |
| **预期结果** | 返回 code=400，message 包含"只能取消待支付订单" |
| **优先级** | P1 |

### 3.4 重复支付

| 用例ID | TC-ORD-004 |
|--------|-----------|
| **用例名称** | 已支付订单重复支付（应失败） |
| **前置条件** | 订单已支付(status=1) |
| **测试步骤** | |
| Step 1 | 再次调用支付: `POST /api/orders/{orderId}/pay` |
| **预期结果** | 返回 code=400，message 包含"订单状态不允许支付" |
| **优先级** | P1 |

### 3.5 操作他人订单

| 用例ID | TC-ORD-005 |
|--------|-----------|
| **用例名称** | 用户操作他人订单（应拒绝） |
| **前置条件** | user1 的订单，user2 登录 |
| **测试步骤** | |
| Step 1 | user2 登录获取 token |
| Step 2 | user2 尝试支付 user1 的订单: `POST /api/orders/{orderId}/pay` |
| Step 3 | user2 尝试取消 user1 的订单: `POST /api/orders/{orderId}/cancel` |
| Step 4 | user2 尝试确认收货 user1 的订单: `POST /api/orders/{orderId}/receive` |
| **预期结果** | 所有操作返回 code=403，message 包含"无权操作他人订单" |
| **优先级** | P0 |

### 3.6 商家发货非自己订单

| 用例ID | TC-ORD-006 |
|--------|-----------|
| **用例名称** | 商家发货非自己的订单（应拒绝） |
| **前置条件** | merchant1 的订单，merchant2 登录 |
| **测试步骤** | |
| Step 1 | merchant2 登录获取 token |
| Step 2 | merchant2 尝试发货 merchant1 的订单: `POST /api/orders/{orderId}/ship` |
| **预期结果** | 返回 code=403，message 包含"无权操作他人订单" |
| **优先级** | P1 |

### 3.7 空购物车下单

| 用例ID | TC-ORD-007 |
|--------|-----------|
| **用例名称** | 购物车无选中商品时下单（应失败） |
| **前置条件** | 用户购物车为空或无选中商品 |
| **测试步骤** | |
| Step 1 | 调用创建订单: `POST /api/orders`，body: `{"addressId":1}` |
| **预期结果** | 返回 code=400，message 包含"购物车中没有选中商品" |
| **优先级** | P1 |

### 3.8 库存不足下单

| 用例ID | TC-ORD-008 |
|--------|-----------|
| **用例名称** | 购物车商品数量超过库存时下单（应失败） |
| **前置条件** | SKU 库存为 50，购物车数量设为 100 |
| **测试步骤** | |
| Step 1 | 加入购物车数量 100: `POST /api/cart`，body: `{"productSkuId":1,"quantity":100}` |
| Step 2 | 选中该购物车项 |
| Step 3 | 创建订单: `POST /api/orders` |
| **预期结果** | 返回 code=400，message 包含"库存不足" |
| **优先级** | P1 |

### 3.9 订单列表分页

| 用例ID | TC-ORD-009 |
|--------|-----------|
| **用例名称** | 用户订单列表分页查询 |
| **前置条件** | 用户有多个订单 |
| **测试步骤** | |
| Step 1 | 查询订单列表: `GET /api/orders?page=1&size=10` |
| Step 2 | 验证返回 PageResult 结构: list, total, pageNum, pageSize, pages |
| Step 3 | 按状态筛选: `GET /api/orders?status=1` |
| **预期结果** | 分页结构正确，按状态筛选返回对应订单 |
| **优先级** | P2 |

---

## 4. 退款流程测试

### 4.1 退款正常流程: 申请→商家审核通过→寄回→确认→退款完成

| 用例ID | TC-RFD-001 |
|--------|-----------|
| **用例名称** | 退款正常全流程 |
| **前置条件** | 用户 user1 有已支付订单(status>=1) |
| **测试步骤** | |
| Step 1 | 用户申请退款: `POST /api/refunds`，body: `{"orderId":{orderId},"reason":"商品质量问题","description":"收到商品有损坏"}` |
| Step 2 | 验证退款记录 status=0(待审核)，refundNo 不为空，amount=订单总金额 |
| Step 3 | 商家审核通过: `POST /api/refunds/{refundId}/approve`，body: `{"remark":"同意退款"}` |
| Step 4 | 验证退款 status=1(商家通过)，merchantAuditTime 不为空 |
| Step 5 | 用户填写寄回物流: `POST /api/refunds/{refundId}/return-logistics`，body: `{"logisticsCompany":"中通快递","logisticsNo":"ZT9876543210"}` |
| Step 6 | 验证退款 status=2(寄回中)，returnLogisticsCompany/returnLogisticsNo 不为空 |
| Step 7 | 商家确认收货: `POST /api/refunds/{refundId}/confirm-receive` |
| Step 8 | 验证退款 status=3(退款完成)，merchantConfirmTime/completedTime 不为空 |
| **预期结果** | 退款状态完整流转: 0→1→2→3 |
| **优先级** | P0 |

### 4.2 退款异常流程 A: 商家拒绝→用户申诉→管理员仲裁支持

| 用例ID | TC-RFD-002 |
|--------|-----------|
| **用例名称** | 商家拒绝退款→用户申诉→管理员支持退款 |
| **前置条件** | 用户有已支付订单，退款已创建(status=0) |
| **测试步骤** | |
| Step 1 | 商家拒绝退款: `POST /api/refunds/{refundId}/reject`，body: `{"remark":"商品无质量问题"}` |
| Step 2 | 验证退款 status=4(商家拒绝) |
| Step 3 | 用户申诉: `POST /api/refunds/{refundId}/appeal`，body: `{"appealReason":"商品确实有损坏，附照片证据"}` |
| Step 4 | 验证退款 status=5(申诉中)，appealTime/appealReason 不为空 |
| Step 5 | 管理员仲裁支持退款: `POST /api/refunds/{refundId}/admin-approve`，body: `{"remark":"证据充分，支持退款"}` |
| Step 6 | 验证退款 status=6(管理员支持)，adminId/adminHandleTime/adminRemark 不为空 |
| Step 7 | 用户填写寄回物流 → 商家确认收货 → 退款完成 |
| **预期结果** | 退款状态流转: 0→4→5→6→2→3 |
| **优先级** | P0 |

### 4.3 退款异常流程 B: 商家超时未审核→用户申诉→管理员介入

| 用例ID | TC-RFD-003 |
|--------|-----------|
| **用例名称** | 商家超时未审核→用户申诉→管理员强制支持退款 |
| **前置条件** | 退款已创建(status=0)，且超过 timeoutHours(48小时) |
| **测试步骤** | |
| Step 1 | 模拟退款创建时间超过 48 小时（修改数据库 refund.created_at 或调整 timeoutHours=0） |
| Step 2 | 用户申诉: `POST /api/refunds/{refundId}/appeal`，body: `{"appealReason":"商家超时未审核"}` |
| Step 3 | 验证退款 status=5(申诉中) |
| Step 4 | 管理员仲裁支持退款: `POST /api/refunds/{refundId}/admin-approve`，body: `{"remark":"商家超时，支持退款"}` |
| Step 5 | 验证退款 status=6(管理员支持) |
| **预期结果** | 超时场景下用户可申诉，管理员可强制支持退款 |
| **优先级** | P0 |

### 4.4 退款异常流程 C: 管理员拒绝退款

| 用例ID | TC-RFD-004 |
|--------|-----------|
| **用例名称** | 管理员仲裁拒绝退款 |
| **前置条件** | 退款处于申诉状态(status=5) |
| **测试步骤** | |
| Step 1 | 管理员仲裁拒绝: `POST /api/refunds/{refundId}/admin-reject`，body: `{"remark":"证据不足，拒绝退款"}` |
| Step 2 | 验证退款 status=7(管理员拒绝) |
| **预期结果** | 退款 status=7，adminRemark 记录拒绝原因 |
| **优先级** | P0 |

### 4.5 重复申请退款

| 用例ID | TC-RFD-005 |
|--------|-----------|
| **用例名称** | 同一订单重复申请退款（应失败） |
| **前置条件** | 订单已有退款记录 |
| **测试步骤** | |
| Step 1 | 再次调用创建退款: `POST /api/refunds`，body: `{"orderId":{orderId},"reason":"再次申请"}` |
| **预期结果** | 返回 code=400，message 包含"已有退款申请" |
| **优先级** | P1 |

### 4.6 不允许退款状态申请退款

| 用例ID | TC-RFD-006 |
|--------|-----------|
| **用例名称** | 待支付订单申请退款（应失败） |
| **前置条件** | 订单 status=0(待支付) |
| **测试步骤** | |
| Step 1 | 申请退款: `POST /api/refunds`，body: `{"orderId":{orderId},"reason":"测试"}` |
| **预期结果** | 返回 code=400，message 包含"当前订单状态不允许退款" |
| **优先级** | P1 |

### 4.7 商家审核非自己退款

| 用例ID | TC-RFD-007 |
|--------|-----------|
| **用例名称** | 商家审核非自己的退款（应拒绝） |
| **前置条件** | merchant1 的退款，merchant2 登录 |
| **测试步骤** | |
| Step 1 | merchant2 尝试审核 merchant1 的退款: `POST /api/refunds/{refundId}/approve` |
| **预期结果** | 返回 code=403，message 包含"无权操作" |
| **优先级** | P1 |

### 4.8 非申诉状态仲裁

| 用例ID | TC-RFD-008 |
|--------|-----------|
| **用例名称** | 对非申诉状态的退款进行仲裁（应失败） |
| **前置条件** | 退款 status=0(待审核) |
| **测试步骤** | |
| Step 1 | 管理员尝试仲裁: `POST /api/refunds/{refundId}/admin-approve` |
| **预期结果** | 返回 code=400，message 包含"只能仲裁申诉中的退款" |
| **优先级** | P1 |

### 4.9 未超时申诉

| 用例ID | TC-RFD-009 |
|--------|-----------|
| **用例名称** | 退款未超时且商家未拒绝时申诉（应失败） |
| **前置条件** | 退款 status=0(待审核)，未超过 timeoutHours |
| **测试步骤** | |
| Step 1 | 用户申诉: `POST /api/refunds/{refundId}/appeal` |
| **预期结果** | 返回 code=400，message 包含"当前退款状态不允许申诉" |
| **优先级** | P2 |

---

## 5. 权限测试

### 5.1 普通用户不能访问商家接口

| 用例ID | TC-PERM-001 |
|--------|-----------|
| **用例名称** | 普通用户访问商家订单列表（应拒绝） |
| **前置条件** | user1 已登录(role=USER) |
| **测试步骤** | |
| Step 1 | 调用商家订单列表: `GET /api/orders/merchant` |
| **预期结果** | 返回 code=403，message 包含"非商家账号" |
| **优先级** | P0 |

### 5.2 普通用户不能访问管理员接口

| 用例ID | TC-PERM-002 |
|--------|-----------|
| **用例名称** | 普通用户访问管理员仪表盘（应拒绝） |
| **前置条件** | user1 已登录(role=USER) |
| **测试步骤** | |
| Step 1 | 调用: `GET /api/admin/dashboard` |
| Step 2 | 调用: `GET /api/admin/users` |
| Step 3 | 调用: `GET /api/admin/merchants` |
| **预期结果** | 所有接口返回 code=403，message 包含"无权限访问" |
| **优先级** | P0 |

### 5.3 商家不能访问管理员接口

| 用例ID | TC-PERM-003 |
|--------|-----------|
| **用例名称** | 商家访问管理员接口（应拒绝） |
| **前置条件** | merchant1 已登录(role=MERCHANT) |
| **测试步骤** | |
| Step 1 | 调用: `GET /api/admin/dashboard` |
| Step 2 | 调用: `GET /api/admin/users` |
| Step 3 | 调用: `POST /api/admin/products/{id}/audit` |
| **预期结果** | 所有接口返回 code=403，message 包含"无权限访问" |
| **优先级** | P0 |

### 5.4 管理员可以访问所有接口

| 用例ID | TC-PERM-004 |
|--------|-----------|
| **用例名称** | 管理员访问各模块接口 |
| **前置条件** | admin 已登录(role=ADMIN) |
| **测试步骤** | |
| Step 1 | 访问仪表盘: `GET /api/admin/dashboard` |
| Step 2 | 查看用户列表: `GET /api/admin/users` |
| Step 3 | 查看商家列表: `GET /api/admin/merchants` |
| Step 4 | 查看商品列表: `GET /api/admin/products` |
| Step 5 | 查看退款列表: `GET /api/refunds/all`（管理员视角） |
| **预期结果** | 所有接口正常返回 code=200 |
| **优先级** | P0 |

### 5.5 未登录访问受保护接口

| 用例ID | TC-PERM-005 |
|--------|-----------|
| **用例名称** | 未携带 Token 访问受保护接口 |
| **前置条件** | 无 |
| **测试步骤** | |
| Step 1 | 不携带 Authorization 头访问: `GET /api/orders` |
| Step 2 | 不携带 Authorization 头访问: `GET /api/cart` |
| **预期结果** | 返回 code=401，message 包含"未登录"或"请先登录" |
| **优先级** | P0 |

### 5.6 无效 Token 访问

| 用例ID | TC-PERM-006 |
|--------|-----------|
| **用例名称** | 携带无效/过期 Token 访问 |
| **前置条件** | 无 |
| **测试步骤** | |
| Step 1 | 携带无效 Token: `Authorization: Bearer invalidtoken123` |
| Step 2 | 访问: `GET /api/orders` |
| **预期结果** | 返回 code=401，message 包含"登录已过期" |
| **优先级** | P1 |

### 5.7 公开接口无需认证

| 用例ID | TC-PERM-007 |
|--------|-----------|
| **用例名称** | 公开接口无需登录可访问 |
| **前置条件** | 无 |
| **测试步骤** | |
| Step 1 | 访问商品列表: `GET /api/products/list` |
| Step 2 | 访问商品详情: `GET /api/products/detail/1` |
| Step 3 | 访问分类列表: `GET /api/categories` |
| Step 4 | 访问轮播图: `GET /api/banners` |
| **预期结果** | 所有接口正常返回 code=200 |
| **优先级** | P1 |

---

## 6. 商品流程测试

### 6.1 商家发布商品→管理员审核→上架→用户浏览购买

| 用例ID | TC-PROD-001 |
|--------|-----------|
| **用例名称** | 商品发布到上架全流程 |
| **前置条件** | merchant1 已登录(已审核通过) |
| **测试步骤** | |
| Step 1 | 商家发布商品: `POST /api/products`，body 含商品信息+SKU列表 |
| Step 2 | 验证商品 status=0(待审核) |
| Step 3 | 管理员审核通过: `POST /api/admin/products/{id}/audit?status=1&remark=审核通过` |
| Step 4 | 验证商品 status=1(上架) |
| Step 5 | 用户浏览商品列表，能搜到该商品 |
| Step 6 | 用户查看商品详情: `GET /api/products/detail/{id}` |
| Step 7 | 用户查看 SKU 列表: `GET /api/products/{id}/skus` |
| **预期结果** | 商品从待审核→上架→可被用户浏览购买 |
| **优先级** | P0 |

### 6.2 管理员审核拒绝商品

| 用例ID | TC-PROD-002 |
|--------|-----------|
| **用例名称** | 商品审核拒绝 |
| **前置条件** | 商品 status=0(待审核) |
| **测试步骤** | |
| Step 1 | 管理员拒绝: `POST /api/admin/products/{id}/audit?status=3&remark=信息不完整` |
| Step 2 | 验证商品 status=3(审核拒绝) |
| Step 3 | 用户搜索商品列表，不应出现该商品 |
| **预期结果** | 商品被拒绝后不出现在上架列表中 |
| **优先级** | P1 |

### 6.3 商家上下架商品

| 用例ID | TC-PROD-003 |
|--------|-----------|
| **用例名称** | 商家下架/上架商品 |
| **前置条件** | 商品 status=1(上架) |
| **测试步骤** | |
| Step 1 | 商家下架: `POST /api/products/{id}/status?status=2` |
| Step 2 | 验证商品 status=2(下架) |
| Step 3 | 用户搜索商品列表，不应出现该商品 |
| Step 4 | 商家上架: `POST /api/products/{id}/status?status=1` |
| Step 5 | 验证商品 status=1(上架) |
| **预期结果** | 商家可自主上下架已审核通过的商品 |
| **优先级** | P1 |

### 6.4 未审核商家发布商品

| 用例ID | TC-PROD-004 |
|--------|-----------|
| **用例名称** | 未审核商家发布商品（应失败） |
| **前置条件** | merchant3 登录(待审核状态) |
| **测试步骤** | |
| Step 1 | merchant3 发布商品: `POST /api/products` |
| **预期结果** | 返回 code=403，message 包含"商家未通过审核" |
| **优先级** | P1 |

### 6.5 商品搜索与分类筛选

| 用例ID | TC-PROD-005 |
|--------|-----------|
| **用例名称** | 商品搜索和分类筛选 |
| **前置条件** | 数据库有商品数据 |
| **测试步骤** | |
| Step 1 | 关键词搜索: `GET /api/products/list?keyword=手机` |
| Step 2 | 验证返回商品名称包含"手机" |
| Step 3 | 分类筛选: `GET /api/products/list?categoryId=111` |
| Step 4 | 验证返回商品属于该分类 |
| Step 5 | 销量排序: `GET /api/products/list?sortBy=sales` |
| Step 6 | 价格升序: `GET /api/products/list?sortBy=price_asc` |
| **预期结果** | 搜索、分类筛选、排序功能正常 |
| **优先级** | P1 |

---

## 7. 购物车测试

### 7.1 加购

| 用例ID | TC-CART-001 |
|--------|-----------|
| **用例名称** | 加入购物车 |
| **前置条件** | user1 已登录 |
| **测试步骤** | |
| Step 1 | 加购: `POST /api/cart`，body: `{"productSkuId":1,"quantity":1}` |
| Step 2 | 查看购物车: `GET /api/cart` |
| Step 3 | 验证购物车包含该 SKU，quantity=1，selected=1 |
| **预期结果** | 商品成功加入购物车 |
| **优先级** | P0 |

### 7.2 重复加购合并数量

| 用例ID | TC-CART-002 |
|--------|-----------|
| **用例名称** | 同一 SKU 重复加购 |
| **前置条件** | 购物车已有 SKU 1，quantity=1 |
| **测试步骤** | |
| Step 1 | 再次加购同一 SKU: `POST /api/cart`，body: `{"productSkuId":1,"quantity":2}` |
| Step 2 | 查看购物车 |
| **预期结果** | 购物车中该 SKU 数量变为 3(1+2)，不产生新记录 |
| **优先级** | P1 |

### 7.3 修改数量

| 用例ID | TC-CART-003 |
|--------|-----------|
| **用例名称** | 修改购物车商品数量 |
| **前置条件** | 购物车有商品 |
| **测试步骤** | |
| Step 1 | 修改数量: `PUT /api/cart/{cartId}`，body: `{"quantity":5,"selected":1}` |
| Step 2 | 查看购物车 |
| **预期结果** | 数量更新为 5 |
| **优先级** | P0 |

### 7.4 数量设为0或负数（删除）

| 用例ID | TC-CART-004 |
|--------|-----------|
| **用例名称** | 数量设为0时自动删除 |
| **前置条件** | 购物车有商品 |
| **测试步骤** | |
| Step 1 | 修改数量为 0: `PUT /api/cart/{cartId}`，body: `{"quantity":0}` |
| Step 2 | 查看购物车 |
| **预期结果** | 该购物车项被删除 |
| **优先级** | P1 |

### 7.5 选中/取消选中

| 用例ID | TC-CART-005 |
|--------|-----------|
| **用例名称** | 选中/取消选中购物车项 |
| **前置条件** | 购物车有多个商品 |
| **测试步骤** | |
| Step 1 | 取消选中: `PUT /api/cart/{cartId}`，body: `{"selected":0}` |
| Step 2 | 全选: `PUT /api/cart/selectAll?selected=1` |
| Step 3 | 取消全选: `PUT /api/cart/selectAll?selected=0` |
| **预期结果** | 选中状态正确更新 |
| **优先级** | P1 |

### 7.6 删除购物车项

| 用例ID | TC-CART-006 |
|--------|-----------|
| **用例名称** | 删除购物车商品 |
| **前置条件** | 购物车有商品 |
| **测试步骤** | |
| Step 1 | 删除: `DELETE /api/cart/{cartId}` |
| Step 2 | 查看购物车 |
| **预期结果** | 该商品从购物车移除 |
| **优先级** | P0 |

### 7.7 加购库存不足

| 用例ID | TC-CART-007 |
|--------|-----------|
| **用例名称** | 加购数量超过库存（应失败） |
| **前置条件** | SKU 库存为 50 |
| **测试步骤** | |
| Step 1 | 加购数量 100: `POST /api/cart`，body: `{"productSkuId":{skuId},"quantity":100}` |
| **预期结果** | 返回 code=400，message 包含"库存不足" |
| **优先级** | P1 |

### 7.8 加购不存在的 SKU

| 用例ID | TC-CART-008 |
|--------|-----------|
| **用例名称** | 加购不存在的 SKU（应失败） |
| **前置条件** | 无 |
| **测试步骤** | |
| Step 1 | 加购: `POST /api/cart`，body: `{"productSkuId":99999,"quantity":1}` |
| **预期结果** | 返回 code=404，message 包含"SKU不存在" |
| **优先级** | P2 |

---

## 8. 用户系统测试

### 8.1 用户注册

| 用例ID | TC-USER-001 |
|--------|-----------|
| **用例名称** | 普通用户注册 |
| **前置条件** | 无 |
| **测试步骤** | |
| Step 1 | 注册: `POST /api/auth/register`，body: `{"username":"newuser","password":"123456","phone":"13900000001","nickname":"新用户"}` |
| Step 2 | 使用新账号登录: `POST /api/auth/login` |
| **预期结果** | 注册成功，可登录，role=USER |
| **优先级** | P0 |

### 8.2 重复用户名注册

| 用例ID | TC-USER-002 |
|--------|-----------|
| **用例名称** | 重复用户名注册（应失败） |
| **前置条件** | admin 已存在 |
| **测试步骤** | |
| Step 1 | 注册: `POST /api/auth/register`，body: `{"username":"admin","password":"123456"}` |
| **预期结果** | 返回 code=400，message 包含"用户名已存在" |
| **优先级** | P1 |

### 8.3 商家入驻注册

| 用例ID | TC-USER-003 |
|--------|-----------|
| **用例名称** | 商家入驻申请 |
| **前置条件** | 无 |
| **测试步骤** | |
| Step 1 | 商家注册: `POST /api/auth/merchant/register`，body: `{"username":"newmerchant","password":"123456","shopName":"新店铺","contactPhone":"13900000002"}` |
| Step 2 | 验证用户 role=MERCHANT，merchant 记录 auditStatus=0(待审核) |
| **预期结果** | 商家注册成功，等待管理员审核 |
| **优先级** | P0 |

### 8.4 登录密码错误

| 用例ID | TC-USER-004 |
|--------|-----------|
| **用例名称** | 密码错误登录（应失败） |
| **前置条件** | 无 |
| **测试步骤** | |
| Step 1 | 登录: `POST /api/auth/login`，body: `{"username":"user1","password":"wrongpassword"}` |
| **预期结果** | 返回 code=401，message 包含"用户名或密码错误" |
| **优先级** | P1 |

### 8.5 禁用用户登录

| 用例ID | TC-USER-005 |
|--------|-----------|
| **用例名称** | 被禁用用户登录（应失败） |
| **前置条件** | 管理员已禁用某用户(status=0) |
| **测试步骤** | |
| Step 1 | 管理员禁用用户: `PUT /api/admin/users/{id}/status?status=0` |
| Step 2 | 被禁用用户登录: `POST /api/auth/login` |
| **预期结果** | 返回 code=403，message 包含"账号已被禁用" |
| **优先级** | P1 |

### 8.6 修改个人信息

| 用例ID | TC-USER-006 |
|--------|-----------|
| **用例名称** | 修改个人资料 |
| **前置条件** | user1 已登录 |
| **测试步骤** | |
| Step 1 | 修改资料: `PUT /api/users/me`，body: `{"nickname":"张三丰","gender":1}` |
| Step 2 | 查看当前用户: `GET /api/users/me` |
| **预期结果** | 昵称和性别已更新 |
| **优先级** | P2 |

### 8.7 修改密码

| 用例ID | TC-USER-007 |
|--------|-----------|
| **用例名称** | 修改密码 |
| **前置条件** | user1 已登录 |
| **测试步骤** | |
| Step 1 | 修改密码: `PUT /api/users/me/password?oldPassword=123456&newPassword=654321` |
| Step 2 | 用旧密码登录（应失败） |
| Step 3 | 用新密码登录（应成功） |
| **预期结果** | 密码修改成功 |
| **优先级** | P1 |

### 8.8 收货地址 CRUD

| 用例ID | TC-USER-008 |
|--------|-----------|
| **用例名称** | 收货地址增删改查 |
| **前置条件** | user1 已登录 |
| **测试步骤** | |
| Step 1 | 添加地址: `POST /api/addresses` |
| Step 2 | 查看地址列表: `GET /api/addresses` |
| Step 3 | 修改地址: `PUT /api/addresses/{id}` |
| Step 4 | 设为默认: `PUT /api/addresses/{id}/default` |
| Step 5 | 删除地址: `DELETE /api/addresses/{id}` |
| **预期结果** | 地址 CRUD 全部正常 |
| **优先级** | P1 |

---

## 9. 评价模块测试

### 9.1 创建评价

| 用例ID | TC-REV-001 |
|--------|-----------|
| **用例名称** | 订单完成后创建评价 |
| **前置条件** | 订单 status=3(已收货) |
| **测试步骤** | |
| Step 1 | 创建评价: `POST /api/reviews`，body: `{"orderId":{orderId},"productId":1,"content":"很好","rating":5}` |
| Step 2 | 验证订单 status 变为 4(已评价) |
| Step 3 | 查看商品评价: `GET /api/reviews/product/1` |
| **预期结果** | 评价创建成功，订单状态更新为已评价 |
| **优先级** | P0 |

### 9.2 非已收货状态评价

| 用例ID | TC-REV-002 |
|--------|-----------|
| **用例名称** | 未收货订单评价（应失败） |
| **前置条件** | 订单 status=2(已发货) |
| **测试步骤** | |
| Step 1 | 创建评价: `POST /api/reviews` |
| **预期结果** | 返回 code=400，message 包含"只能评价已收货的订单" |
| **优先级** | P1 |

### 9.3 商家回复评价

| 用例ID | TC-REV-003 |
|--------|-----------|
| **用例名称** | 商家回复用户评价 |
| **前置条件** | 有评价记录 |
| **测试步骤** | |
| Step 1 | 商家回复: `POST /api/reviews/{id}/reply?reply=感谢您的评价` |
| Step 2 | 查看评价列表，验证 merchantReply 不为空 |
| **预期结果** | 商家回复成功 |
| **优先级** | P2 |

### 9.4 评价他人订单

| 用例ID | TC-REV-004 |
|--------|-----------|
| **用例名称** | 用户评价他人订单（应拒绝） |
| **前置条件** | user2 评价 user1 的订单 |
| **测试步骤** | |
| Step 1 | user2 创建评价 user1 的订单: `POST /api/reviews` |
| **预期结果** | 返回 code=403，message 包含"无权操作他人订单" |
| **优先级** | P1 |

---

## 10. 管理员模块测试

### 10.1 仪表盘数据统计

| 用例ID | TC-ADMIN-001 |
|--------|-----------|
| **用例名称** | 仪表盘统计数据 |
| **前置条件** | admin 已登录 |
| **测试步骤** | |
| Step 1 | 访问: `GET /api/admin/dashboard` |
| **预期结果** | 返回 totalSales, totalOrders, totalUsers, todayOrders |
| **优先级** | P1 |

### 10.2 用户管理

| 用例ID | TC-ADMIN-002 |
|--------|-----------|
| **用例名称** | 用户列表搜索和禁用 |
| **前置条件** | admin 已登录 |
| **测试步骤** | |
| Step 1 | 搜索用户: `GET /api/admin/users?keyword=user` |
| Step 2 | 按角色筛选: `GET /api/admin/users?role=USER` |
| Step 3 | 禁用用户: `PUT /api/admin/users/{id}/status?status=0` |
| Step 4 | 启用用户: `PUT /api/admin/users/{id}/status?status=1` |
| **预期结果** | 搜索、筛选、禁用/启用功能正常 |
| **优先级** | P1 |

### 10.3 商家审核

| 用例ID | TC-ADMIN-003 |
|--------|-----------|
| **用例名称** | 商家入驻审核 |
| **前置条件** | 有待审核商家(merchant3) |
| **测试步骤** | |
| Step 1 | 查看待审核商家: `GET /api/admin/merchants?auditStatus=0` |
| Step 2 | 审核通过: `POST /api/admin/merchants/{id}/audit?auditStatus=1&remark=资质合格` |
| Step 3 | 验证商家 auditStatus=1, status=1(营业中) |
| **预期结果** | 商家审核通过后可正常营业 |
| **优先级** | P0 |

### 10.4 退款仲裁

| 用例ID | TC-ADMIN-004 |
|--------|-----------|
| **用例名称** | 管理员退款仲裁 |
| **前置条件** | 有申诉中的退款(status=5) |
| **测试步骤** | |
| Step 1 | 查看退款列表: `GET /api/refunds?status=5` |
| Step 2 | 仲裁支持: `POST /api/refunds/{id}/admin-approve`，body: `{"remark":"支持退款"}` |
| Step 3 | 验证退款 status=6 |
| **预期结果** | 仲裁操作成功，adminLog 记录操作 |
| **优先级** | P0 |

### 10.5 操作日志

| 用例ID | TC-ADMIN-005 |
|--------|-----------|
| **用例名称** | 管理员操作日志查询 |
| **前置条件** | 管理员已执行过操作 |
| **测试步骤** | |
| Step 1 | 查看日志: `GET /api/admin/logs` |
| Step 2 | 按操作类型筛选: `GET /api/admin/logs?action=MERCHANT_AUDIT` |
| **预期结果** | 日志列表包含操作记录，可按类型筛选 |
| **优先级** | P2 |

---

## 附录: 测试用例统计

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
