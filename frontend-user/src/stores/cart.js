/**
 * 购物车状态管理 - Pinia Store
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getCartList,
  addToCart as addCartApi,
  updateCartQuantity,
  updateCartSelected,
  updateCartBatchSelected,
  deleteCartItem
} from '@/api/cart'
import { ElMessage } from 'element-plus'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const selectedItems = computed(() => items.value.filter(i => i.selected))
  const selectedCount = computed(() => selectedItems.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalAmount = computed(() =>
    selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  )
  const allSelected = computed(() => items.value.length > 0 && items.value.every(i => i.selected))

  // 获取购物车列表
  async function fetchCart() {
    loading.value = true
    try {
      items.value = await getCartList()
    } catch (e) {
      console.error('获取购物车失败', e)
    } finally {
      loading.value = false
    }
  }

  // 添加到购物车
  async function addToCart(product, sku, quantity = 1) {
    try {
      await addCartApi({
        productId: product.id,
        skuId: sku.id,
        quantity
      })
      ElMessage.success('已加入购物车')
      await fetchCart()
    } catch (e) {
      console.error('加购失败', e)
    }
  }

  // 更新数量
  async function updateQuantity(id, quantity) {
    const item = items.value.find(i => i.id === id)
    if (!item) return
    item.quantity = quantity
    try {
      await updateCartQuantity(id, quantity)
    } catch (e) {
      console.error('更新数量失败', e)
    }
  }

  // 切换选中
  async function toggleSelect(id) {
    const item = items.value.find(i => i.id === id)
    if (!item) return
    item.selected = !item.selected
    try {
      await updateCartSelected(id, item.selected)
    } catch (e) {
      console.error('更新选中状态失败', e)
    }
  }

  // 全选/取消全选
  async function toggleSelectAll() {
    const newSelected = !allSelected.value
    items.value.forEach(i => { i.selected = newSelected })
    const ids = items.value.map(i => i.id)
    try {
      await updateCartBatchSelected(ids, newSelected)
    } catch (e) {
      console.error('批量更新失败', e)
    }
  }

  // 删除商品
  async function removeItem(id) {
    try {
      await deleteCartItem(id)
      items.value = items.value.filter(i => i.id !== id)
      ElMessage.success('已删除')
    } catch (e) {
      console.error('删除失败', e)
    }
  }

  return {
    items,
    loading,
    totalCount,
    selectedItems,
    selectedCount,
    totalAmount,
    allSelected,
    fetchCart,
    addToCart,
    updateQuantity,
    toggleSelect,
    toggleSelectAll,
    removeItem
  }
})
