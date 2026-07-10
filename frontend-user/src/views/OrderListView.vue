<template>
  <div class="order-list-view container">
    <h1 class="page-title">📋 我的订单</h1>

    <!-- 状态Tab -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待付款" name="pending_payment" />
      <el-tab-pane label="待发货" name="pending_shipment" />
      <el-tab-pane label="待收货" name="shipped" />
      <el-tab-pane label="已收货" name="completed" />
    </el-tabs>

    <div v-loading="loading">
      <template v-if="orders.length > 0">
        <div v-for="order in orders" :key="order.id" class="order-card" @click="$router.push(`/order/${order.id}`)">
          <div class="order-header">
            <div class="order-meta">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-date">{{ formatDate(order.createdAt) }}</span>
            </div>
            <span class="order-status" :style="{ color: getStatusInfo(order.status).color }">
              {{ getStatusInfo(order.status).label }}
            </span>
          </div>
          <div class="order-body">
            <div v-for="item in order.items" :key="item.id" class="order-product">
              <ProductImage class="product-image" :src="item.image" :alt="item.productName" :fallback-index="item.productId || 0" />
              <div class="product-info">
                <p class="product-name text-ellipsis-2">{{ item.productName }}</p>
                <span class="product-sku">{{ item.skuName }}</span>
              </div>
              <div class="product-price">¥{{ item.unitPrice.toFixed(2) }} × {{ item.quantity }}</div>
            </div>
          </div>
          <div class="order-footer">
            <div class="order-total">
              共{{ order.itemCount }}件商品 合计：<span class="total-price">¥{{ order.totalAmount.toFixed(2) }}</span>
            </div>
            <div class="order-actions" @click.stop>
              <button v-if="order.status === 'pending_payment'" class="action-btn primary" @click="handlePay(order)">立即付款</button>
              <button v-if="order.status === 'pending_payment'" class="action-btn" @click="handleCancel(order)">取消订单</button>
              <button v-if="order.status === 'shipped'" class="action-btn primary" @click="handleConfirm(order)">确认收货</button>
              <button v-if="order.status === 'completed'" class="action-btn primary" @click="$router.push(`/review/${order.id}`)">去评价</button>
              <button v-if="['completed', 'shipped'].includes(order.status)" class="action-btn" @click="$router.push(`/refund/apply/${order.id}`)">申请退款</button>
              <button class="action-btn" @click="$router.push(`/order/${order.id}`)">查看详情</button>
            </div>
          </div>
        </div>
      </template>
      <div v-else class="empty-state">
        <div class="empty-icon">📦</div>
        <p>暂无相关订单</p>
        <router-link to="/product-list" class="go-shopping">去购物</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList, payOrder, cancelOrder, confirmReceive } from '@/api/order'
import { orderStatusMap } from '@/utils/mock-data'
import { ElMessage, ElMessageBox } from 'element-plus'
import ProductImage from '@/components/ProductImage.vue'

const router = useRouter()
const activeTab = ref('all')
const orders = ref([])
const loading = ref(false)

function getStatusInfo(status) {
  return orderStatusMap[status] || { label: '未知', color: '#909399' }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getOrderList({ status: activeTab.value })
    orders.value = res.items
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  fetchOrders()
}

async function handlePay(order) {
  try {
    await payOrder(order.id)
    ElMessage.success('支付成功')
    fetchOrders()
  } catch (e) {
    ElMessage.error('支付失败')
  }
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (e) {}
}

async function handleConfirm(order) {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'info' })
    await confirmReceive(order.id)
    ElMessage.success('确认收货成功')
    fetchOrders()
  } catch (e) {}
}

onMounted(fetchOrders)
</script>

<style scoped>
.order-list-view { padding: 16px 0 40px; }
.page-title { font-size: 24px; font-weight: 700; margin-bottom: 16px; }

.order-card { background: #fff; border-radius: var(--radius-md); margin-bottom: 12px; overflow: hidden; box-shadow: var(--shadow-sm); cursor: pointer; transition: box-shadow var(--transition-fast); }
.order-card:hover { box-shadow: var(--shadow-md); }
.order-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 20px; background: #fafafa; border-bottom: 1px solid var(--border-light); }
.order-meta { display: flex; gap: 16px; }
.order-no { font-size: 13px; color: var(--text-secondary); }
.order-date { font-size: 12px; color: var(--text-secondary); }
.order-status { font-size: 14px; font-weight: 600; }

.order-body { padding: 12px 20px; }
.order-product { display: flex; align-items: center; gap: 12px; padding: 8px 0; }
.product-image { width: 60px; height: 60px; border-radius: var(--radius-sm); flex-shrink: 0; }
.product-info { flex: 1; }
.product-name { font-size: 13px; margin-bottom: 4px; }
.product-sku { font-size: 12px; color: var(--text-secondary); }
.product-price { font-size: 13px; color: var(--text-regular); }

.order-footer { display: flex; justify-content: space-between; align-items: center; padding: 12px 20px; border-top: 1px solid var(--border-light); }
.order-total { font-size: 14px; color: var(--text-regular); }
.total-price { font-size: 18px; font-weight: 700; color: var(--jd-red); }
.order-actions { display: flex; gap: 8px; }
.action-btn { padding: 6px 16px; border: 1px solid var(--border-base); border-radius: var(--radius-full); background: #fff; font-size: 13px; color: var(--text-regular); transition: all var(--transition-fast); }
.action-btn:hover { border-color: var(--jd-red); color: var(--jd-red); }
.action-btn.primary { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.action-btn.primary:hover { background: var(--jd-red-dark); }

.empty-state { text-align: center; padding: 80px 0; }
.empty-icon { font-size: 64px; margin-bottom: 16px; }
.empty-state p { font-size: 16px; color: var(--text-secondary); margin-bottom: 24px; }
.go-shopping { display: inline-block; padding: 10px 32px; background: var(--jd-red); color: #fff; border-radius: var(--radius-full); font-size: 14px; font-weight: 600; }
</style>
