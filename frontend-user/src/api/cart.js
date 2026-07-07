/**
 * 购物车相关 API
 */
import request from '@/utils/request'
import { mockProducts } from '@/utils/mock-data'

// 获取购物车列表
export function getCartList() {
  return request.get('/cart').catch(() => {
    // mock: 随机选几个商品作为购物车内容
    const items = []
    for (let i = 0; i < 4; i++) {
      const product = mockProducts[i * 3]
      items.push({
        id: i + 1,
        productId: product.id,
        productName: product.name,
        skuId: product.id * 100 + 1,
        skuName: '曜石黑 标准版',
        price: product.price,
        quantity: i + 1,
        selected: true,
        image: product.image,
        stock: product.stock,
        merchantId: product.merchantId,
        merchantName: product.merchantName
      })
    }
    return items
  })
}

// 添加到购物车
export function addToCart(data) {
  return request.post('/cart', data).catch(() => ({ success: true, ...data }))
}

// 更新购物车商品数量
export function updateCartQuantity(id, quantity) {
  return request.put(`/cart/${id}`, { quantity }).catch(() => ({ success: true }))
}

// 更新购物车选中状态
export function updateCartSelected(id, selected) {
  return request.put(`/cart/${id}/select`, { selected }).catch(() => ({ success: true }))
}

// 批量更新选中状态
export function updateCartBatchSelected(ids, selected) {
  return request.put('/cart/batch-select', { ids, selected }).catch(() => ({ success: true }))
}

// 删除购物车商品
export function deleteCartItem(id) {
  return request.delete(`/cart/${id}`).catch(() => ({ success: true }))
}
