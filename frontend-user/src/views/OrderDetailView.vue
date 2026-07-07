<template>
  <div class="order-detail-view container" v-loading="loading">
    <template v-if="order">
      <!-- 面包屑 -->
      <el-breadcrumb separator=">" class="breadcrumb">
        <el-breadcrumb-item to="/orders">我的订单</el-breadcrumb-item>
        <el-breadcrumb-item>订单详情</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 状态步骤条 -->
      <div class="status-section">
        <el-steps :active="currentStep" align-center finish-status="success">
          <el-step title="提交订单" :description="formatDate(order.createdAt)" />
          <el-step title="付款成功" :description="order.payTime ? formatDate(order.payTime) : ''" />
          <el-step title="商品出库" :description="order.shipTime ? formatDate(order.shipTime) : ''" />
          <el-step title="确认收货" :description="order.receiveTime ? formatDate(order.receiveTime) : ''" />
          <el-step title="交易完成" />
        </el-steps>
      </div>

      <!-- 物流信息 -->
      <div v-if="order.logisticsNo" class="info-card">
        <h3>🚚 物流信息</h3>
        <div class="logistics-info">
          <span>物流公司：{{ order.logisticsCompany }}</span>
          <span>物流单号：{{ order.logisticsNo }}</span>
        </div>
      </div>

      <!-- 收货信息 -->
      <div class="info-card">
        <h3>📍 收货信息</h3>
        <div class="address-info" v-if="order.address">
          <span>{{ order.address.name }}</span>
          <span>{{ order.address.phone }}</span>
          <span>{{ order.address.province }}{{ order.address.city }}{{ order.address.district }} {{ order.address.detail }}</span>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="info-card">
        <h3>📦 商品信息</h3>
        <div v-for="item in order.items" :key="item.id" class="product-row">
          <div class="product-image" :style="{ background: item.image }"></div>
          <div class="product-info">
            <p class="product-name text-ellipsis-2" @click="$router.push(`/product/${item.productId}`)">{{ item.productName }}</p>
            <span class="product-sku">{{ item.skuName }}</span>
          </div>
          <div class="product-price">¥{{ item.unitPrice.toFixed(2) }}</div>
          <div class="product-qty">x{{ item.quantity }}</div>
          <div class="product-subtotal">¥{{ (item.unitPrice * item.quantity).toFixed(2) }}</div>
        </div>
      </div>

      <!-- 金额信息 -->
      <div class="info-card amount-card">
        <div class="amount-row"><span>商品总额</span><span>¥{{ order.totalAmount.toFixed(2) }}</span></div>
        <div class="amount-row"><span>运费</span><span class="free">免运费</span></div>
        <div class="amount-row total"><span>实付款</span><span class="total-price">¥{{ order.totalAmount.toFixed(2) }}</span></div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-bar" v-if="order.status !== 'cancelled'">
        <button v-if="order.status === 'pending_payment'" class="btn primary" @click="handlePay">立即付款</button>
        <button v-if="order.status === 'pending_payment'" class="btn" @click="handleCancel">取消订单</button>
        <button v-if="order.status === 'shipped'" class="btn primary" @click="handleConfirm">确认收货</button>
        <button v-if="order.status === 'completed'" class="btn primary" @click="$router.push(`/review/${order.id}`)">去评价</button>
        <button v-if="['completed', 'shipped'].includes(order.status)" class="btn" @click="$router.push(`/refund/apply/${order.id}`)">申请退款</button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder, cancelOrder, confirmReceive } from '@/api/order'
import { orderStatusMap } from '@/utils/mock-data'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(true)

const currentStep = computed(() => {
  if (!order.value) return 0
  return orderStatusMap[order.value.status]?.step ?? 0
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth()+1}月${d.getDate()}日 ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

async function fetchDetail() {
  loading.value = true
  try {
    order.value = await getOrderDetail(route.params.id)
  } catch (e) {
    ElMessage.error('订单不存在')
    router.push('/orders')
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  try {
    await payOrder(order.value.id)
    ElMessage.success('支付成功')
    fetchDetail()
  } catch (e) { ElMessage.error('支付失败') }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    fetchDetail()
  } catch (e) {}
}

async function handleConfirm() {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'info' })
    await confirmReceive(order.value.id)
    ElMessage.success('确认收货成功')
    fetchDetail()
  } catch (e) {}
}

onMounted(fetchDetail)
</script>

<style scoped>
.order-detail-view { padding: 16px 0 40px; }
.breadcrumb { margin-bottom: 16px; }

.status-section { background: #fff; border-radius: var(--radius-md); padding: 24px; margin-bottom: 16px; box-shadow: var(--shadow-sm); }

.info-card { background: #fff; border-radius: var(--radius-md); padding: 20px; margin-bottom: 16px; box-shadow: var(--shadow-sm); }
.info-card h3 { font-size: 16px; font-weight: 700; margin-bottom: 12px; }
.logistics-info, .address-info { display: flex; gap: 24px; font-size: 14px; color: var(--text-regular); }

.product-row { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid var(--border-light); }
.product-row:last-child { border-bottom: none; }
.product-image { width: 60px; height: 60px; border-radius: var(--radius-sm); flex-shrink: 0; }
.product-info { flex: 1; }
.product-name { font-size: 13px; margin-bottom: 4px; cursor: pointer; }
.product-name:hover { color: var(--jd-red); }
.product-sku { font-size: 12px; color: var(--text-secondary); }
.product-price, .product-qty { font-size: 13px; color: var(--text-regular); width: 80px; text-align: center; }
.product-subtotal { font-size: 14px; color: var(--jd-red); font-weight: 700; width: 100px; text-align: right; }

.amount-card { text-align: right; }
.amount-row { display: flex; justify-content: flex-end; gap: 12px; padding: 4px 0; font-size: 14px; color: var(--text-regular); }
.amount-row .free { color: var(--color-success); }
.amount-row.total { padding-top: 12px; border-top: 1px solid var(--border-light); margin-top: 8px; }
.total-price { font-size: 24px; font-weight: 800; color: var(--jd-red); }

.action-bar { display: flex; justify-content: flex-end; gap: 12px; }
.btn { padding: 10px 32px; border: 1px solid var(--border-base); border-radius: var(--radius-full); background: #fff; font-size: 14px; color: var(--text-regular); transition: all var(--transition-fast); }
.btn:hover { border-color: var(--jd-red); color: var(--jd-red); }
.btn.primary { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.btn.primary:hover { background: var(--jd-red-dark); }
</style>
