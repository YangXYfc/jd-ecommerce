# 极东商城 - 商家后台 (frontend-merchant)

独立商家后台前端项目，基于 Vue 3 + Vite + Element Plus + Pinia + Axios。

## 启动

```bash
cd frontend-merchant
npm install
npm run dev
```

默认端口 5175，API 代理到 `localhost:8080`。

## 功能模块

- 商家登录（校验 role === MERCHANT）
- 店铺仪表盘（销售统计概览）
- 商品管理（列表/发布/SKU/上下架）
- 订单管理（列表/详情/发货）
- 退款处理（退款列表/审核/确认收货/超时标红）
- 店铺设置（店铺信息修改/联系方式）
- 评价管理（查看评价/回复评价）
