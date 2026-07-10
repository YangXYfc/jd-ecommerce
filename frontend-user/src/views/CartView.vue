<template>
  <div class="cart-view container">
    <h1 class="page-title">🛒 我的购物车</h1>

    <div v-loading="cartStore.loading">
      <template v-if="cartStore.items.length > 0">
        <!-- 表头 -->
        <div class="cart-header">
          <div class="col-check">
            <input type="checkbox" :checked="cartStore.allSelected" @change="cartStore.toggleSelectAll()" />
            <span>全选</span>
          </div>
          <div class="col-info">商品信息</div>
          <div class="col-price">单价</div>
          <div class="col-qty">数量</div>
          <div class="col-subtotal">小计</div>
          <div class="col-action">操作</div>
        </div>

        <!-- 按店铺分组 -->
        <div v-for="merchant in merchantGroups" :key="merchant.merchantId" class="merchant-group">
          <div class="merchant-header">
            <span class="merchant-name">🏪 {{ merchant.merchantName }}</span>
          </div>
          <div v-for="item in merchant.items" :key="item.id" class="cart-item">
            <div class="col-check">
              <input type="checkbox" :checked="item.selected" @change="cartStore.toggleSelect(item.id)" />
            </div>
            <div class="col-info">
              <ProductImage class="item-image" :src="item.image" :alt="item.productName" :fallback-index="item.productId || 0" />
              <div class="item-detail">
                <p class="item-name text-ellipsis-2" @click="$router.push(`/product/${item.productId}`)">{{ item.productName }}</p>
                <span class="item-sku">{{ item.skuName }}</span>
              </div>
            </div>
            <div class="col-price">
              <span class="price">¥{{ item.price.toFixed(2) }}</span>
            </div>
            <div class="col-qty">
              <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small"
                @change="cartStore.updateQuantity(item.id, item.quantity)" />
            </div>
            <div class="col-subtotal">
              <span class="price subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
            <div class="col-action">
              <button class="delete-btn" @click="cartStore.removeItem(item.id)">删除</button>
            </div>
          </div>
        </div>

        <!-- 结算栏 -->
        <div class="cart-footer">
          <div class="footer-left">
            <input type="checkbox" :checked="cartStore.allSelected" @change="cartStore.toggleSelectAll()" />
            <span>全选</span>
          </div>
          <div class="footer-right">
            <div class="footer-summary">
              <span>已选 <em>{{ cartStore.selectedCount }}</em> 件</span>
              <span class="total-amount">合计：<em>¥{{ cartStore.totalAmount.toFixed(2) }}</em></span>
            </div>
            <button class="checkout-btn" :disabled="cartStore.selectedCount === 0" @click="goCheckout">
              去结算 ({{ cartStore.selectedCount }})
            </button>
          </div>
        </div>
      </template>

      <!-- 空购物车 -->
      <div v-else class="empty-cart">
        <div class="empty-icon">🛒</div>
        <p class="empty-text">购物车还是空的，快去挑选心仪的商品吧～</p>
        <router-link to="/product-list" class="go-shopping">去逛逛</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import ProductImage from '@/components/ProductImage.vue'

const cartStore = useCartStore()
const router = useRouter()

const merchantGroups = computed(() => {
  const groups = {}
  cartStore.items.forEach(item => {
    const key = item.merchantId
    if (!groups[key]) {
      groups[key] = { merchantId: item.merchantId, merchantName: item.merchantName, items: [] }
    }
    groups[key].items.push(item)
  })
  return Object.values(groups)
})

function goCheckout() {
  router.push('/checkout')
}

onMounted(() => {
  cartStore.fetchCart()
})
</script>

<style scoped>
.cart-view { padding: 16px 0 40px; }
.page-title { font-size: 24px; font-weight: 700; margin-bottom: 16px; }

.cart-header, .cart-item {
  display: grid;
  grid-template-columns: 60px 1fr 120px 140px 120px 80px;
  align-items: center;
  gap: 12px;
}
.cart-header {
  background: #fff; border-radius: var(--radius-md) var(--radius-md) 0 0;
  padding: 12px 16px; font-size: 13px; color: var(--text-secondary);
  border-bottom: 1px solid var(--border-light);
}
.col-check { display: flex; align-items: center; gap: 8px; }
.col-check input { width: 16px; height: 16px; accent-color: var(--jd-red); }

.merchant-group { background: #fff; border-radius: var(--radius-md); margin-bottom: 12px; overflow: hidden; box-shadow: var(--shadow-sm); }
.merchant-header { padding: 12px 16px; border-bottom: 1px solid var(--border-light); background: #fafafa; }
.merchant-name { font-size: 14px; font-weight: 600; color: var(--text-primary); }

.cart-item { padding: 16px; border-bottom: 1px solid var(--border-light); }
.cart-item:last-child { border-bottom: none; }

.col-info { display: flex; align-items: center; gap: 12px; }
.item-image { width: 80px; height: 80px; border-radius: var(--radius-sm); flex-shrink: 0; }
.item-detail { flex: 1; }
.item-name { font-size: 13px; color: var(--text-primary); cursor: pointer; margin-bottom: 4px; }
.item-name:hover { color: var(--jd-red); }
.item-sku { font-size: 12px; color: var(--text-secondary); background: #f5f5f5; padding: 2px 8px; border-radius: var(--radius-sm); }

.col-price .price { font-size: 14px; color: var(--text-regular); }
.col-subtotal .subtotal { font-size: 16px; font-weight: 700; }
.delete-btn { font-size: 13px; color: var(--text-secondary); background: none; }
.delete-btn:hover { color: var(--jd-red); }

.cart-footer {
  position: sticky; bottom: 0;
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; border-radius: var(--radius-md);
  padding: 12px 16px; margin-top: 16px;
  box-shadow: var(--shadow-lg);
}
.footer-left { display: flex; align-items: center; gap: 8px; }
.footer-right { display: flex; align-items: center; gap: 24px; }
.footer-summary { display: flex; align-items: center; gap: 16px; font-size: 14px; color: var(--text-regular); }
.footer-summary em { font-style: normal; color: var(--jd-red); font-weight: 700; }
.total-amount em { font-size: 24px; }
.checkout-btn {
  padding: 12px 40px; background: var(--jd-red-gradient); color: #fff;
  font-size: 16px; font-weight: 700; border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}
.checkout-btn:hover:not(:disabled) { opacity: 0.9; }
.checkout-btn:disabled { background: #ccc; cursor: not-allowed; }

.empty-cart { text-align: center; padding: 80px 0; background: #fff; border-radius: var(--radius-md); }
.empty-icon { font-size: 64px; margin-bottom: 16px; }
.empty-text { font-size: 16px; color: var(--text-secondary); margin-bottom: 24px; }
.go-shopping { display: inline-block; padding: 10px 32px; background: var(--jd-red); color: #fff; border-radius: var(--radius-full); font-size: 14px; font-weight: 600; }
</style>
