<template>
  <div class="checkout-view container">
    <h1 class="page-title">确认订单</h1>

    <div class="checkout-content" v-loading="loading">
      <!-- 收货地址 -->
      <div class="section">
        <h2 class="section-title">📍 收货地址</h2>
        <div class="address-list">
          <div
            v-for="addr in addresses" :key="addr.id"
            :class="['address-card', { selected: selectedAddressId === addr.id }]"
            @click="selectedAddressId = addr.id"
          >
            <div class="addr-header">
              <span class="addr-name">{{ addr.name }}</span>
              <span class="addr-phone">{{ addr.phone }}</span>
              <span v-if="addr.isDefault" class="default-tag">默认</span>
            </div>
            <p class="addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
          </div>
          <div class="address-card add-address" @click="$router.push('/profile/addresses')">
            <span>+ 添加新地址</span>
          </div>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="section">
        <h2 class="section-title">📦 商品清单</h2>
        <div class="order-items">
          <div v-for="item in orderItems" :key="item.id" class="order-item">
            <div class="item-image" :style="{ background: item.image }"></div>
            <div class="item-info">
              <p class="item-name text-ellipsis-2">{{ item.productName }}</p>
              <span class="item-sku">{{ item.skuName }}</span>
            </div>
            <div class="item-price">¥{{ item.price?.toFixed(2) || item.unitPrice?.toFixed(2) }}</div>
            <div class="item-qty">x{{ item.quantity }}</div>
            <div class="item-subtotal">¥{{ ((item.price || item.unitPrice) * item.quantity).toFixed(2) }}</div>
          </div>
        </div>
      </div>

      <!-- 订单备注 -->
      <div class="section">
        <h2 class="section-title">📝 订单备注</h2>
        <el-input v-model="remark" type="textarea" :rows="2" placeholder="选填，给商家留言（50字以内）" maxlength="50" show-word-limit />
      </div>

      <!-- 金额汇总 -->
      <div class="section summary-section">
        <div class="summary-row">
          <span>商品总额</span>
          <span class="price">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <div class="summary-row">
          <span>运费</span>
          <span class="price free">免运费</span>
        </div>
        <div class="summary-row total">
          <span>应付金额</span>
          <span class="price total-price">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- 提交栏 -->
    <div class="submit-bar">
      <div class="submit-left">
        <span>寄送至：{{ selectedAddress?.province }}{{ selectedAddress?.city }}{{ selectedAddress?.district }}</span>
      </div>
      <div class="submit-right">
        <span class="submit-amount">应付：<em>¥{{ totalAmount.toFixed(2) }}</em></span>
        <button class="submit-btn" :disabled="!selectedAddressId || submitting" @click="handleSubmit">
          {{ submitting ? '提交中...' : '提交订单' }}
        </button>
      </div>
    </div>

    <!-- 模拟支付弹窗 -->
    <el-dialog v-model="showPayDialog" title="模拟支付" width="400px" :close-on-click-modal="false">
      <div class="pay-dialog">
        <div class="pay-amount">
          <span>支付金额</span>
          <p>¥{{ payAmount.toFixed(2) }}</p>
        </div>
        <div class="pay-methods">
          <div class="pay-method active">💰 模拟余额支付</div>
        </div>
        <button class="pay-confirm-btn" @click="handlePay">确认支付</button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { getAddressList, createOrder, payOrder } from '@/api/order'
import { getProductDetail } from '@/api/product'
import { mockAddresses } from '@/utils/mock-data'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const addresses = ref(mockAddresses)
const selectedAddressId = ref(null)
const orderItems = ref([])
const remark = ref('')
const loading = ref(true)
const submitting = ref(false)
const showPayDialog = ref(false)
const payAmount = ref(0)
const createdOrderId = ref(null)

const selectedAddress = computed(() => addresses.value.find(a => a.id === selectedAddressId.value))
const totalAmount = computed(() =>
  orderItems.value.reduce((sum, item) => sum + (item.price || item.unitPrice) * item.quantity, 0)
)

onMounted(async () => {
  try { addresses.value = await getAddressList() } catch {}
  selectedAddressId.value = addresses.value.find(a => a.isDefault)?.id || addresses.value[0]?.id

  // 从购物车结算 或 从商品详情直接购买
  if (route.query.productId) {
    // 直接购买
    try {
      const product = await getProductDetail(route.query.productId)
      const sku = product.skus?.find(s => s.id === Number(route.query.skuId)) || product.skus?.[0]
      orderItems.value = [{
        id: 1,
        productId: product.id,
        productName: product.name,
        skuId: sku.id,
        skuName: sku.skuName,
        price: sku.price,
        quantity: Number(route.query.quantity) || 1,
        image: product.image,
        merchantId: product.merchantId,
        merchantName: product.merchantName
      }]
    } catch (e) {
      ElMessage.error('获取商品信息失败')
    }
  } else {
    // 从购物车结算
    await cartStore.fetchCart()
    orderItems.value = cartStore.selectedItems
  }
  loading.value = false
})

async function handleSubmit() {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  submitting.value = true
  try {
    const res = await createOrder({
      addressId: selectedAddressId.value,
      items: orderItems.value.map(i => ({ productId: i.productId, skuId: i.skuId, quantity: i.quantity })),
      totalAmount: totalAmount.value,
      remark: remark.value
    })
    createdOrderId.value = res.id
    payAmount.value = totalAmount.value
    showPayDialog.value = true
    ElMessage.success('订单创建成功')
  } catch (e) {
    ElMessage.error('创建订单失败')
  } finally {
    submitting.value = false
  }
}

async function handlePay() {
  try {
    await payOrder(createdOrderId.value)
    ElMessage.success('支付成功')
    showPayDialog.value = false
    router.push(`/order/${createdOrderId.value}`)
  } catch (e) {
    ElMessage.error('支付失败')
  }
}
</script>

<style scoped>
.checkout-view { padding: 16px 0 80px; }
.page-title { font-size: 24px; font-weight: 700; margin-bottom: 16px; }

.section { background: #fff; border-radius: var(--radius-md); padding: 20px; margin-bottom: 16px; box-shadow: var(--shadow-sm); }
.section-title { font-size: 16px; font-weight: 700; margin-bottom: 16px; }

.address-list { display: flex; flex-wrap: wrap; gap: 12px; }
.address-card { width: 280px; border: 2px solid var(--border-light); border-radius: var(--radius-md); padding: 12px 16px; cursor: pointer; transition: all var(--transition-fast); }
.address-card:hover { border-color: var(--jd-red); }
.address-card.selected { border-color: var(--jd-red); background: #fff5f5; }
.addr-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.addr-name { font-weight: 700; font-size: 14px; }
.addr-phone { font-size: 13px; color: var(--text-regular); }
.default-tag { font-size: 10px; background: var(--jd-red); color: #fff; padding: 1px 6px; border-radius: 2px; }
.addr-detail { font-size: 12px; color: var(--text-secondary); line-height: 1.5; }
.add-address { display: flex; align-items: center; justify-content: center; border-style: dashed; color: var(--text-secondary); }

.order-items { display: flex; flex-direction: column; gap: 12px; }
.order-item { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid var(--border-light); }
.order-item:last-child { border-bottom: none; }
.item-image { width: 60px; height: 60px; border-radius: var(--radius-sm); flex-shrink: 0; }
.item-info { flex: 1; }
.item-name { font-size: 13px; margin-bottom: 4px; }
.item-sku { font-size: 12px; color: var(--text-secondary); }
.item-price, .item-qty, .item-subtotal { font-size: 14px; color: var(--text-regular); width: 80px; text-align: center; }
.item-subtotal { color: var(--jd-red); font-weight: 700; }

.summary-section { padding: 20px; }
.summary-row { display: flex; justify-content: flex-end; align-items: center; gap: 12px; padding: 4px 0; font-size: 14px; color: var(--text-regular); }
.summary-row .price { color: var(--jd-red); }
.summary-row .free { color: var(--color-success); }
.summary-row.total { padding-top: 12px; border-top: 1px solid var(--border-light); margin-top: 8px; }
.total-price { font-size: 24px; font-weight: 800; }

.submit-bar { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; box-shadow: var(--shadow-lg); padding: 12px 0; z-index: 100; }
.submit-bar .container { display: flex; justify-content: space-between; align-items: center; }
.submit-left { font-size: 13px; color: var(--text-secondary); }
.submit-right { display: flex; align-items: center; gap: 20px; }
.submit-amount { font-size: 14px; }
.submit-amount em { font-style: normal; color: var(--jd-red); font-size: 24px; font-weight: 800; }
.submit-btn { padding: 12px 48px; background: var(--jd-red-gradient); color: #fff; font-size: 16px; font-weight: 700; border-radius: var(--radius-md); }
.submit-btn:disabled { background: #ccc; }

.pay-dialog { text-align: center; }
.pay-amount { margin-bottom: 20px; }
.pay-amount span { font-size: 14px; color: var(--text-secondary); }
.pay-amount p { font-size: 32px; font-weight: 800; color: var(--jd-red); }
.pay-methods { margin-bottom: 20px; }
.pay-method { padding: 12px; border: 2px solid var(--jd-red); border-radius: var(--radius-md); font-size: 14px; color: var(--jd-red); }
.pay-confirm-btn { width: 100%; padding: 12px; background: var(--jd-red); color: #fff; font-size: 16px; font-weight: 700; border-radius: var(--radius-md); }
</style>
