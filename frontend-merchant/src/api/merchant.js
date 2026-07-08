/**
 * 商家后台 API
 * 注意：商家维度列表接口使用 /merchant/* 前缀（后端 MerchantController）
 *       操作类接口（发货/退款审核等）使用各自 Controller 的路径
 */
import request from '@/utils/request'

// 登录 - 调用后端 POST /auth/login
export function login(data) {
  return request.post('/auth/login', data)
}

// 店铺信息
export function getMerchantInfo() {
  return request.get('/merchant/info')
}

export function updateMerchantInfo(data) {
  return request.put('/merchant/info', data)
}

// 店铺配置
export function getMerchantConfigs() {
  return request.get('/merchant/configs')
}

export function updateMerchantConfigs(data) {
  return request.post('/merchant/configs', data)
}

// 商品管理 - 商家维度列表
export function getProductList(params) {
  return request.get('/merchant/products', { params })
}

// 商品详情（通用接口）
export function getProductDetail(id) {
  return request.get(`/products/detail/${id}`)
}

// 发布商品
export function createProduct(data) {
  return request.post('/products', data)
}

// 编辑商品
export function updateProduct(id, data) {
  return request.put(`/products/${id}`, data)
}

export function deleteProduct(id) {
  return request.delete(`/products/${id}`)
}

// 上下架
export function updateProductStatus(id, status) {
  return request.put(`/products/${id}/status`, { status })
}

// SKU管理
export function getProductSkus(productId) {
  return request.get(`/products/${productId}/skus`)
}

export function createSku(productId, data) {
  return request.post(`/products/${productId}/skus`, data)
}

export function updateSku(productId, skuId, data) {
  return request.put(`/products/${productId}/skus/${skuId}`, data)
}

export function deleteSku(productId, skuId) {
  return request.delete(`/products/${productId}/skus/${skuId}`)
}

// 订单管理 - 商家维度列表
export function getOrderList(params) {
  return request.get('/merchant/orders', { params })
}

// 订单详情（商家维度，后端校验merchantId归属）
export function getOrderDetail(id) {
  return request.get(`/merchant/orders/${id}`)
}

// 发货 - 后端 POST /orders/{id}/ship
export function shipOrder(id, data) {
  return request.post(`/orders/${id}/ship`, data)
}

// 退款处理 - 商家维度列表
export function getRefundList(params) {
  return request.get('/merchant/refunds', { params })
}

// 退款详情（通用接口）
export function getRefundDetail(id) {
  return request.get(`/refunds/${id}`)
}

// 商家审核退款 - 通过: POST /refunds/{id}/approve
export function approveRefund(id, remark) {
  return request.post(`/refunds/${id}/approve`, { remark })
}

// 商家审核退款 - 拒绝: POST /refunds/{id}/reject
export function rejectRefund(id, remark) {
  return request.post(`/refunds/${id}/reject`, { remark })
}

// 商家确认收货: POST /refunds/{id}/confirm-receive
export function confirmRefundReceipt(id) {
  return request.post(`/refunds/${id}/confirm-receive`)
}

// 评价管理 - 商家维度列表
export function getReviewList(params) {
  return request.get('/merchant/reviews', { params })
}

// 回复评价: POST /reviews/{id}/reply
export function replyReview(id, content) {
  return request.post(`/reviews/${id}/reply`, { content })
}

// 仪表盘统计
export function getDashboardStats() {
  return request.get('/merchant/dashboard')
}
