/**
 * 管理员后台 API
 */
import request from '@/utils/request'

// 登录 - 调用后端 POST /auth/login
export function login(data) {
  return request.post('/auth/login', data)
}

// 获取管理员信息
export function getAdminInfo() {
  return request.get('/admin/info')
}

// 用户管理
export function getUserList(params) {
  return request.get('/admin/users', { params })
}

export function updateUserStatus(id, status) {
  return request.put(`/admin/users/${id}/status`, { status })
}

// 商家管理
export function getMerchantList(params) {
  return request.get('/admin/merchants', { params })
}

export function auditMerchant(id, status, remark) {
  return request.put(`/admin/merchants/${id}/audit`, { status, remark })
}

// 商品审核
export function getProductAuditList(params) {
  return request.get('/admin/products/audit', { params })
}

export function auditProduct(id, status, remark) {
  return request.put(`/admin/products/${id}/audit`, { status, remark })
}

// 订单管理
export function getOrderList(params) {
  return request.get('/admin/orders', { params })
}

// 退款仲裁
export function getRefundList(params) {
  return request.get('/admin/refunds', { params })
}

export function arbitrateRefund(id, result, remark) {
  return request.put(`/admin/refunds/${id}/arbitrate`, { result, remark })
}

// 操作日志
export function getLogList(params) {
  return request.get('/admin/logs', { params })
}

// 仪表盘统计
export function getDashboardStats() {
  return request.get('/admin/dashboard')
}
