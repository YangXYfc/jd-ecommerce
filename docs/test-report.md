# B2C 电商平台 - 测试报告

> 项目: jd-ecommerce  
> 测试人: 测试运维-菜菜 (pt-dev2)  
> 日期: 2026-07-07  
> 版本: v1.0

---

## 1. 测试概述

### 1.1 测试范围

本次测试覆盖以下模块:
- 订单全流程（正常流程 + 异常分支）
- 退款全流程（正常流程 + 超时/申诉/仲裁异常分支）
- 权限隔离测试（普通用户/商家/管理员）
- 商品流程（发布→审核→上架→浏览）
- 购物车（加购/修改/删除/选中结算）
- 用户系统（注册/登录/个人信息/地址管理）
- 评价模块（评价创建/商家回复）
- 管理员模块（仪表盘/用户管理/商家审核/退款仲裁/操作日志）

### 1.2 测试方法

- **静态测试**: 通过代码审查验证业务逻辑、状态流转、权限控制的正确性
- **接口测试**: 基于 API 定义编写测试用例，验证各接口的输入输出
- **边界测试**: 验证异常场景下的错误处理和状态保护

### 1.3 测试环境

| 项目 | 版本/配置 |
|------|-----------|
| JDK | 17+ |
| Spring Boot | 3.2.5 |
| MyBatis | 3.0.3 (注解模式) |
| MySQL | 8.0+ |
| Vue | 3.5.x |
| Vite | 5.4.x |
| Element Plus | 2.14.x |

---

## 2. 代码审查发现

### 2.1 订单模块

#### 2.1.1 正面发现

✅ **订单状态机完整**: OrderService 定义了 6 个状态常量(0-5)，状态流转逻辑清晰:
- 0(待支付) → 1(待发货) [支付]
- 1(待发货) → 2(已发货) [商家发货]
- 2(已发货) → 3(已收货) [用户确认收货]
- 3(已收货) → 4(已评价) [用户评价]
- 0(待支付) → 5(已取消) [用户取消]

✅ **库存管理**: 下单时扣减库存(decreaseStock 使用 `stock >= #{quantity}` 条件防超卖)，取消订单时恢复库存。

✅ **权限校验**: 订单操作(支付/取消/确认收货)均校验 `userId` 匹配，商家发货校验 `merchantId` 匹配。

✅ **多商家订单**: createOrder 按商家分组，每个商家生成独立订单。

✅ **事务管理**: 关键操作(createOrder, payOrder, cancelOrder, shipOrder, confirmReceive)均使用 `@Transactional`。

#### 2.1.2 问题发现

⚠️ **[中] OrderCreateRequest.cartIds 未使用**: 
- DTO 定义了 `cartIds` 字段，但 OrderService.createOrder 实际使用 `cartMapper.findSelectedByUserId(userId)` 获取选中商品，未使用 cartIds 参数。
- **影响**: 用户无法指定只结算部分购物车商品，只能结算全部选中项。
- **建议**: 如果设计为"选中即结算"，应移除 cartIds 字段避免混淆；如需支持指定结算，应修改 service 逻辑。

⚠️ **[低] 模拟支付无金额校验**: payOrder 直接使用 order.totalAmount 创建支付记录，未做额外校验。模拟支付场景下可接受。

### 2.2 退款模块

#### 2.2.1 正面发现

✅ **退款状态机完整**: RefundService 定义了 8 个状态常量(0-7)，覆盖全部流程:
- 正常流程: 0→1→2→3
- 商家拒绝: 0→4→5(申诉)→6/7(仲裁)
- 超时申诉: 0→5(申诉)→6/7(仲裁)
- 管理员支持后: 6→2(寄回)→3(完成)

✅ **超时机制**: isMerchantAuditTimedOut 方法检查退款创建时间是否超过 timeoutHours(默认48小时)。

✅ **申诉条件控制**: appeal 方法仅在 MERCHANT_REJECTED 或 PENDING+超时 时允许申诉。

✅ **管理员仲裁**: 仅对 STATUS_APPEALED 状态的退款可仲裁，仲裁后记录 adminLog。

✅ **权限校验**: 用户操作校验 userId，商家操作校验 merchantId。

#### 2.2.2 问题发现

⚠️ **[中] 退款仲裁接口路径不明确**: 
- RefundController 中管理员仲裁接口已确认为两个独立端点: `/refunds/{id}/admin-approve` 和 `/refunds/{id}/admin-reject`，分别对应支持退款和拒绝退款。
- 代码中 adminApproveRefund 和 adminRejectRefund 是两个独立方法，但 Controller 中需要确认是否暴露了两个端点或使用参数区分。
- **建议**: 确认 API 设计，确保管理员可以明确选择支持或拒绝。

⚠️ **[低] 超时检查依赖系统时间**: isMerchantAuditTimedOut 使用 `LocalDateTime.now()` 与 `createdAt` 比较，测试时需手动修改数据库时间或调整 timeoutHours。

### 2.3 权限控制

#### 2.3.1 正面发现

✅ **JWT 认证**: JwtInterceptor 拦截所有请求(除白名单)，验证 Bearer Token。

✅ **角色校验**: AdminController 所有接口调用 `RequestContextUtil.requireRole("ADMIN")`，确保仅管理员可访问。

✅ **商家接口校验**: 商家相关接口(发货/退款审核/商品管理等)通过 `merchantService.findByUserId(userId)` 校验商家身份，非商家返回 403。

✅ **公开接口白名单**: WebConfig.excludePathPatterns 正确排除了登录/注册/商品浏览/分类/轮播图/Swagger 等公开接口。

#### 2.3.2 问题发现

⚠️ **[高] 部分接口缺少角色级权限校验**:
- OrderController 的商家端点(`/orders/merchant`, `/orders/{id}/ship`)仅校验是否为商家，未校验具体角色。虽然 `findByUserId` 返回 null 时抛 403，但普通用户调用时不会触发 requireRole("MERCHANT")，而是依赖 merchant=null 的判断。逻辑上可行但不够显式。
- CartController 的 `delete` 方法 (`DELETE /api/cart/{id}`) 未校验 cartId 是否属于当前用户，任何登录用户可删除任意购物车项。
- **建议**: CartController.delete 应校验 cart.userId == currentUserId。

⚠️ **[中] 用户信息泄露**: UserService.getUserById 将 password 设为 null 后返回，但 AdminService.searchUsers 也做了同样处理。需确认 User 实体序列化时不会意外暴露 password 字段。

### 2.4 商品模块

#### 2.4.1 正面发现

✅ **商品审核流程**: 商家发布→status=0(待审核)→管理员审核→status=1(上架)/3(拒绝)。

✅ **商家权限**: 发布/编辑/上下架商品均校验 merchantId 归属。

✅ **未审核商家限制**: createProduct 校验 merchant.auditStatus == 1。

✅ **搜索功能**: 支持关键词搜索、分类筛选、多种排序(销量/价格升序/价格降序)。

### 2.5 购物车模块

#### 2.5.1 正面发现

✅ **加购合并**: 同一 SKU 重复加购自动合并数量。

✅ **库存校验**: 加购时检查 SKU 库存。

✅ **选中/全选**: 支持单项选中/取消和全选/取消全选。

#### 2.5.2 问题发现

⚠️ **[高] CartController.delete 无权限校验** (同 2.3.2):
- `DELETE /api/cart/{id}` 直接调用 `cartService.deleteCart(cartId)`，未校验该购物车项是否属于当前用户。
- **影响**: 任何登录用户可删除其他用户的购物车项。
- **建议**: 添加 userId 校验。

⚠️ **[中] CartUpdateRequest.quantity @NotNull**: 
- quantity 标注了 `@NotNull`，但实际使用中可能只想修改 selected 而不传 quantity。
- **建议**: 移除 @NotNull 或使用 @Validated 分组。

### 2.6 用户系统

#### 2.6.1 正面发现

✅ **密码加密**: 使用 BCrypt 加密存储密码。

✅ **注册校验**: 用户名唯一、手机号唯一、手机号格式校验。

✅ **商家入驻**: 商家注册创建 User(role=MERCHANT) + Merchant(auditStatus=0) 双记录。

✅ **禁用用户**: 管理员可禁用用户(status=0)，登录时校验。

### 2.7 评价模块

#### 2.7.1 正面发现

✅ **评价条件**: 仅 STATUS_RECEIVED(已收货) 订单可评价。

✅ **评价后状态更新**: 创建评价后自动将订单标记为 STATUS_REVIEWED。

✅ **商家回复**: 商家可回复自己的商品评价。

---

## 3. 测试执行结果

### 3.1 静态测试结果

基于代码审查的静态测试结论:

| 模块 | 用例总数 | 通过 | 失败 | 阻塞 | 通过率 |
|------|----------|------|------|------|--------|
| 订单全流程 | 9 | 8 | 0 | 1 | 89% |
| 退款流程 | 9 | 9 | 0 | 0 | 100% |
| 权限测试 | 7 | 5 | 2 | 0 | 71% |
| 商品流程 | 5 | 5 | 0 | 0 | 100% |
| 购物车 | 8 | 6 | 2 | 0 | 75% |
| 用户系统 | 8 | 8 | 0 | 0 | 100% |
| 评价模块 | 4 | 4 | 0 | 0 | 100% |
| 管理员模块 | 5 | 5 | 0 | 0 | 100% |
| **合计** | **55** | **50** | **4** | **1** | **91%** |

> 注: 当前环境无 MySQL 实例，测试以代码审查+逻辑验证方式进行。标记为"失败"的用例对应代码审查中发现的问题。

### 3.2 失败用例详情

#### FAIL: TC-PERM-005 - 购物车删除无权限校验

- **问题**: `DELETE /api/cart/{id}` 未校验 cartId 归属
- **严重级别**: 高
- **代码位置**: CartController.java line ~57
- **修复建议**: 
```java
@DeleteMapping("/{id}")
public Result<Void> delete(@PathVariable Long id) {
    Long userId = RequestContextUtil.getCurrentUserId();
    cartService.deleteCart(userId, id); // 传入 userId 做校验
    return Result.success();
}
```

#### FAIL: TC-CART-004 - 数量设为0自动删除

- **问题**: CartUpdateRequest.quantity 标注 @NotNull，当只传 selected 不传 quantity 时会触发校验失败
- **严重级别**: 中
- **代码位置**: CartUpdateRequest.java
- **修复建议**: 移除 quantity 的 @NotNull 注解

#### FAIL: TC-PERM-006 - 用户操作他人购物车

- **问题**: 同 TC-PERM-005，购物车删除和修改缺少用户归属校验
- **严重级别**: 高
- **修复建议**: updateCart 和 deleteCart 均应校验 userId

#### FAIL: TC-ORD-007 - 空购物车下单时 cartIds 未使用

- **问题**: OrderCreateRequest.cartIds 字段未被使用，实际下单使用 findSelectedByUserId
- **严重级别**: 中
- **修复建议**: 移除 cartIds 字段或修改 service 逻辑使用该字段

### 3.3 阻塞用例详情

#### BLOCKED: TC-ORD-009 - 订单列表分页

- **原因**: 当前环境无 MySQL 数据库实例，无法实际执行分页查询验证 PageHelper 行为
- **建议**: 部署 MySQL 后补充集成测试

---

## 4. 风险评估

### 4.1 高风险项

| 风险项 | 影响 | 建议 |
|--------|------|------|
| 购物车删除/修改无权限校验 | 用户可恶意删除他人购物车 | **必须修复** |
| 部分接口缺少显式角色校验 | 依赖隐式逻辑，存在安全隐患 | 建议增加显式 requireRole |

### 4.2 中风险项

| 风险项 | 影响 | 建议 |
|--------|------|------|
| cartIds 字段未使用 | 接口语义与实现不一致 | 统一设计 |
| CartUpdateRequest @NotNull | 部分更新场景受限 | 调整校验 |
| 退款仲裁接口路径 | API 设计需明确 | 确认接口定义 |

### 4.3 低风险项

| 风险项 | 影响 | 建议 |
|--------|------|------|
| 超时检查依赖系统时间 | 测试不便 | 可接受 |
| 模拟支付无额外校验 | 模拟场景可接受 | 可接受 |

---

## 5. 测试结论

### 5.1 总体评价

项目核心业务逻辑实现完整，订单全流程和退款全流程（含异常分支）的状态机设计正确，权限控制基本覆盖。代码质量良好，使用了统一响应格式、全局异常处理、JWT 认证等最佳实践。

### 5.2 主要优点

1. **订单状态机完整**: 6 个状态覆盖全部流转路径，含取消分支
2. **退款流程完善**: 8 个状态覆盖正常流程 + 3 种异常分支(商家拒绝/超时/仲裁)
3. **权限分层清晰**: USER/MERCHANT/ADMIN 三级角色，管理员接口有显式 requireRole
4. **事务管理规范**: 关键业务方法均使用 @Transactional
5. **库存安全**: 下单扣库存使用乐观锁条件(`stock >= #{quantity}`)

### 5.3 需修复项

1. **[必须] 购物车删除/修改添加 userId 校验** - 安全漏洞
2. **[建议] 移除 CartUpdateRequest.quantity 的 @NotNull** - 功能限制
3. **[建议] 统一 cartIds 设计** - 接口一致性
4. **[建议] 补充集成测试** - 部署 MySQL 后执行

### 5.4 测试签名

```
测试人: 测试运维-菜菜 (pt-dev2)
日期: 2026-07-07
状态: 代码审查测试完成，发现 4 个问题(2高/2中)，建议修复高优先级问题后进行集成测试
```
