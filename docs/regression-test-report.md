# 京东风格电商平台 - 回归测试报告

> 项目: jd-ecommerce  
> 测试人: 测试运维-菜菜 (pt-dev2)  
> 日期: 2026-07-07  
> 版本: v1.1 (回归测试)  
> 关联: task-26 (Bug修复) → 回归验证

---

## 1. 回归测试概述

### 1.1 测试范围

对 task-26 修复的 5 个 Bug 进行代码级回归验证，重点回归 4 个原失败用例：

| 原用例ID | 关联Bug | 严重级别 | 修复内容 |
|----------|---------|----------|----------|
| TC-PERM-005 | Bug 1 [高] | 高 | CartController.delete 新增 userId 权限校验 |
| TC-PERM-006 | Bug 2 [高] | 高 | CartController.update 新增 userId 权限校验 |
| TC-CART-004 | Bug 3 [中] | 中 | CartUpdateRequest.quantity 移除 @NotNull |
| TC-ORD-007 | Bug 4 [中] | 中 | OrderCreateRequest.cartIds 字段移除 |
| (附带验证) | Bug 5 [中] | 中 | 退款仲裁确认为两个独立端点 |

### 1.2 测试方法

代码审查回归：逐行审查修复后的源码，验证权限校验逻辑、DTO 字段变更、接口路径设计是否符合预期。

### 1.3 审查文件清单

| 文件 | 审查重点 |
|------|----------|
| CartController.java | delete/update 是否传入 userId |
| CartService.java | deleteCart/updateCart 是否校验 cartId 归属 |
| CartMapper.java | SQL 是否带 WHERE user_id 条件 |
| CartUpdateRequest.java | quantity 是否移除 @NotNull |
| OrderCreateRequest.java | cartIds 字段是否移除 |
| RefundController.java | 仲裁端点是否为两个独立路径 |
| OrderService.java | createOrder 空购物车逻辑是否正常 |
| RefundService.java | adminApproveRefund/adminRejectRefund 逻辑 |
| BusinessException.java | 异常 code 机制 |
| GlobalExceptionHandler.java | 异常响应格式 |

---

## 2. 回归测试结果

### 2.1 总览

| 用例ID | 原状态 | 回归结果 | 说明 |
|--------|--------|----------|------|
| TC-PERM-005 | FAIL | ✅ PASS | 删除权限校验已修复 |
| TC-PERM-006 | FAIL | ✅ PASS | 修改权限校验已修复 |
| TC-CART-004 | FAIL | ✅ PASS | @NotNull 已移除，部分更新可用 |
| TC-ORD-007 | FAIL | ✅ PASS | cartIds 已移除，接口一致 |
| Bug 5 仲裁路径 | 问题待确认 | ✅ PASS | 两个独立端点 /admin-approve 和 /admin-reject |

**回归通过率: 5/5 = 100%**

---

### 2.2 详细回归验证

#### ✅ TC-PERM-005: 购物车删除权限校验

**原问题**: `DELETE /api/cart/{id}` 未校验 cartId 归属，任何登录用户可删除他人购物车项。

**修复验证**:

1. **CartController.delete()** — 现在传入 userId：
```java
@DeleteMapping("/{id}")
public Result<Void> delete(@PathVariable Long id) {
    Long userId = RequestContextUtil.getCurrentUserId();
    cartService.deleteCart(userId, id);  // ✅ 传入 userId
    return Result.success();
}
```

2. **CartService.deleteCart()** — 先校验归属再删除：
```java
public void deleteCart(Long userId, Long cartId) {
    Cart target = cartMapper.findByIdAndUserId(cartId, userId);  // ✅ 查询带 userId
    if (target == null) {
        throw new BusinessException(404, "购物车项不存在");  // ✅ 不属于当前用户 → 404
    }
    cartMapper.delete(cartId, userId);  // ✅ 删除 SQL 也带 userId
}
```

3. **CartMapper.findByIdAndUserId()** — SQL 层面校验：
```sql
SELECT * FROM cart WHERE id = #{id} AND user_id = #{userId}  -- ✅ 双条件
```

4. **CartMapper.delete()** — 删除 SQL 防御性条件：
```sql
DELETE FROM cart WHERE id = #{id} AND user_id = #{userId}  -- ✅ 纵深防御
```

**回归结论**: ✅ **PASS**

- 用户 A 尝试删除用户 B 的购物车项 (id=100)：
  - `findByIdAndUserId(100, A)` → 查询 `WHERE id=100 AND user_id=A` → 返回 null
  - 抛出 `BusinessException(404, "购物车项不存在")`
  - GlobalExceptionHandler 返回 `{code: 404, message: "购物车项不存在"}`
- 即使绕过 Service 层，Mapper.delete SQL 也包含 `AND user_id` 条件，不会误删

---

#### ✅ TC-PERM-006: 用户操作他人购物车（修改）

**原问题**: `PUT /api/cart/{id}` 未校验 cartId 归属，任何登录用户可修改他人购物车项。

**修复验证**:

1. **CartController.update()** — 现在传入 userId：
```java
@PutMapping("/{id}")
public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CartUpdateRequest request) {
    Long userId = RequestContextUtil.getCurrentUserId();
    cartService.updateCart(userId, id, request);  // ✅ 传入 userId
    return Result.success();
}
```

2. **CartService.updateCart()** — 先校验归属再操作：
```java
public void updateCart(Long userId, Long cartId, CartUpdateRequest request) {
    Cart target = cartMapper.findByIdAndUserId(cartId, userId);  // ✅ 查询带 userId
    if (target == null) {
        throw new BusinessException(404, "购物车项不存在");  // ✅ 不属于当前用户 → 404
    }
    // 后续 updateQuantity / updateSelected 均传入 userId
    cartMapper.updateQuantity(cartId, userId, request.getQuantity());
    cartMapper.updateSelected(cartId, userId, request.getSelected());
}
```

3. **CartMapper SQL** — 所有更新操作带 userId：
```sql
UPDATE cart SET quantity = #{quantity} WHERE id = #{id} AND user_id = #{userId}  -- ✅
UPDATE cart SET selected = #{selected} WHERE id = #{id} AND user_id = #{userId}  -- ✅
```

**回归结论**: ✅ **PASS**

- 用户 A 尝试修改用户 B 的购物车项 → `findByIdAndUserId` 返回 null → 404
- 所有 UPDATE SQL 均包含 `AND user_id` 条件，纵深防御完整

---

#### ✅ TC-CART-004: 数量设为0自动删除 + 部分更新

**原问题**: `CartUpdateRequest.quantity` 标注 `@NotNull`，只传 `selected` 不传 `quantity` 时触发校验失败 (400)。

**修复验证**:

1. **CartUpdateRequest** — @NotNull 已移除：
```java
@Data
public class CartUpdateRequest {
    private Integer quantity;   // ✅ 无 @NotNull，可选
    private Integer selected;   // ✅ 无 @NotNull，可选
}
```

2. **CartService.updateCart()** — 部分更新逻辑正确：
```java
if (request.getQuantity() != null) {        // ✅ 只在传了 quantity 时处理
    if (request.getQuantity() <= 0) {
        cartMapper.delete(cartId, userId);   // ✅ 数量 ≤ 0 → 自动删除
        return;
    }
    cartMapper.updateQuantity(cartId, userId, request.getQuantity());
}
if (request.getSelected() != null) {        // ✅ 只在传了 selected 时处理
    cartMapper.updateSelected(cartId, userId, request.getSelected());
}
```

**回归结论**: ✅ **PASS**

| 请求 body | 预期行为 | 验证结果 |
|-----------|----------|----------|
| `{"quantity": 0}` | 删除该购物车项 | ✅ `delete(cartId, userId)` 被调用 |
| `{"quantity": 5}` | 更新数量为 5 | ✅ `updateQuantity` 被调用 |
| `{"selected": 0}` | 只改选中状态，不触发校验错误 | ✅ quantity 为 null，跳过数量逻辑，只更新 selected |
| `{"selected": 1, "quantity": 3}` | 同时更新 | ✅ 两个 if 块均执行 |
| `{}` | 无操作，不报错 | ✅ 两个 if 均跳过，正常返回 |

---

#### ✅ TC-ORD-007: 空购物车下单

**原问题**: `OrderCreateRequest` 定义了 `cartIds` 字段但 `OrderService.createOrder` 未使用，接口语义不一致。

**修复验证**:

1. **OrderCreateRequest** — cartIds 已移除：
```java
@Data
public class OrderCreateRequest {
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;
    private String remark;
    // ✅ cartIds 字段已移除
}
```

2. **OrderService.createOrder()** — 使用"选中即结算"逻辑：
```java
List<Cart> selectedCarts = cartMapper.findSelectedByUserId(userId);
if (selectedCarts.isEmpty()) {
    throw new BusinessException("购物车中没有选中商品");  // ✅ 空购物车 → 400
}
```

3. **接口一致性**: DTO 不再声明 `cartIds`，Service 也不依赖它，设计统一为"选中即结算"模式。

**回归结论**: ✅ **PASS**

| 场景 | 请求 body | 预期响应 | 验证结果 |
|------|-----------|----------|----------|
| 空购物车下单 | `{"addressId": 1}` | `{code: 400, message: "购物车中没有选中商品"}` | ✅ |
| 有选中商品下单 | `{"addressId": 1, "remark": "测试"}` | 正常创建订单 | ✅ |
| 未传 addressId | `{}` | `{code: 400, message: "收货地址ID不能为空"}` | ✅ @NotNull 校验 |

---

#### ✅ Bug 5: 退款仲裁接口路径确认

**原问题**: 仲裁接口路径不明确，需确认是单个 `/arbitrate` 端点用参数区分，还是两个独立端点。

**修复验证**:

**RefundController** 确认为两个独立端点：

```java
// 端点 1: 管理员仲裁-支持退款
@PostMapping("/{id}/admin-approve")
public Result<Void> adminApprove(@PathVariable Long id, @Valid @RequestBody RefundArbitrateRequest request) {
    RequestContextUtil.requireRole("ADMIN");  // ✅ 角色校验
    Long adminId = RequestContextUtil.getCurrentUserId();
    refundService.adminApproveRefund(id, adminId, request);  // → status=6
    return Result.success();
}

// 端点 2: 管理员仲裁-拒绝退款
@PostMapping("/{id}/admin-reject")
public Result<Void> adminReject(@PathVariable Long id, @Valid @RequestBody RefundArbitrateRequest request) {
    RequestContextUtil.requireRole("ADMIN");  // ✅ 角色校验
    Long adminId = RequestContextUtil.getCurrentUserId();
    refundService.adminRejectRefund(id, adminId, request);  // → status=7
    return Result.success();
}
```

**RefundService** 逻辑验证：
- `adminApproveRefund`: 校验 `status == STATUS_APPEALED(5)` → 更新为 `STATUS_ADMIN_APPROVED(6)` → 记录 adminLog ✅
- `adminRejectRefund`: 校验 `status == STATUS_APPEALED(5)` → 更新为 `STATUS_ADMIN_REJECTED(7)` → 记录 adminLog ✅
- 非申诉状态仲裁 → `BusinessException("只能仲裁申诉中的退款")` ✅

**回归结论**: ✅ **PASS**

| 操作 | API 路径 | 退款状态变化 | 验证结果 |
|------|----------|-------------|----------|
| 管理员支持退款 | `POST /refunds/{id}/admin-approve` | 5→6 | ✅ |
| 管理员拒绝退款 | `POST /refunds/{id}/admin-reject` | 5→7 | ✅ |
| 非管理员调用 | 同上 | 403 无权限访问 | ✅ |
| 非申诉状态仲裁 | 同上 | 400 只能仲裁申诉中的退款 | ✅ |

---

## 3. 额外发现（非阻塞）

### 3.1 测试用例文档需同步更新

回归测试中发现 `test-cases.md` 中部分用例仍引用旧 API 路径，建议同步更新：

| 用例ID | 旧路径/字段 | 应更新为 |
|--------|------------|----------|
| TC-RFD-002 Step 5 | `POST /api/refunds/{id}/arbitrate?decision=approve` | `POST /api/refunds/{id}/admin-approve` |
| TC-RFD-003 Step 4 | `POST /api/refunds/{id}/arbitrate?decision=approve` | `POST /api/refunds/{id}/admin-approve` |
| TC-RFD-004 Step 1 | `POST /api/refunds/{id}/arbitrate?decision=reject` | `POST /api/refunds/{id}/admin-reject` |
| TC-ADMIN-004 Step 2 | `POST /api/refunds/{id}/arbitrate?decision=approve` | `POST /api/refunds/{id}/admin-approve` |
| TC-ORD-007 Step 1 | body 含 `"cartIds":[]` | 移除 cartIds，body 为 `{"addressId":1}` |

> 这些是文档同步问题，不影响功能，建议在 README 最终版一起更新。

### 3.2 安全防御纵深确认

修复后的购物车权限校验实现了**三层防御**，设计良好：

1. **Controller 层**: 从 RequestContextUtil 获取当前 userId 并传入 Service
2. **Service 层**: `findByIdAndUserId` 校验 cartId 归属，不属于当前用户则抛 404
3. **Mapper 层**: 所有 UPDATE/DELETE SQL 均包含 `AND user_id = #{userId}` 条件

---

## 4. 回归测试结论

### 4.1 总体评价

| 指标 | v1.0 (初次测试) | v1.1 (回归测试) |
|------|-----------------|-----------------|
| 通过率 | 91% (50/55) | **100% (5/5 回归用例)** |
| 高优先级问题 | 2 个 | **0 个** |
| 中优先级问题 | 2 个 | **0 个** |
| 阻塞项 | 1 个 (无 MySQL) | 1 个 (同，环境限制) |

### 4.2 修复质量评价

王拜佛的修复质量很高：

1. **权限校验**: 不是简单加个 if 判断，而是从 Controller → Service → Mapper 三层全部传入 userId，SQL 层面也有防御条件，纵深防御设计优秀
2. **DTO 清理**: cartIds 移除干净，CartUpdateRequest 注释清晰说明了部分更新的设计意图
3. **接口设计**: 退款仲裁改为两个独立端点，RESTful 语义更清晰，比用参数区分更规范
4. **代码注释**: 新增了有意义的注释说明权限校验目的

### 4.3 剩余阻塞项

| 阻塞项 | 原因 | 建议 |
|--------|------|------|
| TC-ORD-009 订单列表分页 | 无 MySQL 实例，无法验证 PageHelper 实际行为 | 部署 MySQL 后补充集成测试 |

### 4.4 测试签名

```
测试人: 测试运维-菜菜 (pt-dev2)
日期: 2026-07-07
回归版本: v1.1
回归结果: 5/5 用例通过 (100%)
结论: 全部 Bug 修复验证通过，建议进入集成测试阶段
```
