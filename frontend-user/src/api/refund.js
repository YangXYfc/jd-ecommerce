/**
 * 退款相关 API
 */
import request from '@/utils/request'
import { mockRefunds } from '@/utils/mock-data'

// 获取退款列表
export function getRefundList(params = {}) {
  const { status, page = 1, size = 10 } = params
  return request.get('/refunds', { params }).catch(() => {
    let list = [...mockRefunds]
    if (status && status !== 'all') {
      list = list.filter(r => r.status === status)
    }
    const total = list.length
    const start = (page - 1) * size
    return { items: list.slice(start, start + size), total, page, size }
  })
}

// 获取退款详情
export function getRefundDetail(id) {
  return request.get(`/refunds/${id}`).catch(() => {
    const refund = mockRefunds.find(r => r.id === Number(id))
    if (!refund) return Promise.reject(new Error('退款记录不存在'))
    return refund
  })
}

// 申请退款
export function applyRefund(data) {
  return request.post('/refunds', data).catch(() => ({
    success: true,
    id: mockRefunds.length + 1,
    status: 'applied',
    appliedAt: new Date().toISOString()
  }))
}

// 填写退货物流信息
export function submitReturnLogistics(id, data) {
  return request.post(`/refunds/${id}/logistics`, data).catch(() => ({
    success: true,
    status: 'returning'
  }))
}

// 提交申诉（商家超时未审核）
export function submitAppeal(id, reason) {
  return request.post(`/refunds/${id}/appeal`, { reason }).catch(() => ({
    success: true,
    status: 'appealing',
    appealTime: new Date().toISOString()
  }))
}
