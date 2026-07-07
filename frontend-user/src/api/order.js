/**
 * 订单相关 API
 */
import request from '@/utils/request'
import { mockOrders, mockAddresses } from '@/utils/mock-data'

// 获取订单列表
export function getOrderList(params = {}) {
  const { status, page = 1, size = 10 } = params
  return request.get('/orders', { params }).catch(() => {
    let list = [...mockOrders]
    if (status && status !== 'all') {
      list = list.filter(o => o.status === status)
    }
    list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    const total = list.length
    const start = (page - 1) * size
    return { items: list.slice(start, start + size), total, page, size }
  })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request.get(`/orders/${id}`).catch(() => {
    const order = mockOrders.find(o => o.id === Number(id))
    if (!order) return Promise.reject(new Error('订单不存在'))
    return order
  })
}

// 创建订单（从购物车结算）
export function createOrder(data) {
  return request.post('/orders', data).catch(() => {
    const newId = mockOrders.length + 1
    return {
      id: newId,
      orderNo: `JD${Date.now().toString().slice(-8)}${String(newId).padStart(4, '0')}`,
      status: 'pending_payment',
      totalAmount: data.totalAmount || 0,
      createdAt: new Date().toISOString()
    }
  })
}

// 模拟支付
export function payOrder(id, data = {}) {
  return request.post(`/orders/${id}/pay`, data).catch(() => ({ success: true, status: 'pending_shipment' }))
}

// 取消订单
export function cancelOrder(id, reason = '') {
  return request.post(`/orders/${id}/cancel`, { reason }).catch(() => ({ success: true, status: 'cancelled' }))
}

// 确认收货
export function confirmReceive(id) {
  return request.post(`/orders/${id}/confirm`).catch(() => ({ success: true, status: 'completed' }))
}

// 提交评价
export function submitReview(orderId, data) {
  return request.post(`/orders/${orderId}/review`, data).catch(() => ({ success: true }))
}

// 获取收货地址列表
export function getAddressList() {
  return request.get('/addresses').catch(() => mockAddresses)
}

// 添加收货地址
export function addAddress(data) {
  return request.post('/addresses', data).catch(() => ({ success: true, id: Date.now() }))
}

// 更新收货地址
export function updateAddress(id, data) {
  return request.put(`/addresses/${id}`, data).catch(() => ({ success: true }))
}

// 删除收货地址
export function deleteAddress(id) {
  return request.delete(`/addresses/${id}`).catch(() => ({ success: true }))
}

// 设置默认地址
export function setDefaultAddress(id) {
  return request.put(`/addresses/${id}/default`).catch(() => ({ success: true }))
}
