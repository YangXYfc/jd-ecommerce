<template>
  <div class="order-detail-view" v-loading="loading">
    <div class="page-header">
      <el-button link @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
      <h2>订单详情</h2>
    </div>

    <div v-if="order" class="detail-content">
      <!-- 订单信息 -->
      <el-card class="info-card" shadow="never">
        <template #header><span>📋 订单信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusType(order.status)" size="small">{{ statusLabel(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="买家">{{ order.userName }}</el-descriptions-item>
          <el-descriptions-item label="商品数">{{ order.itemCount }}</el-descriptions-item>
          <el-descriptions-item label="总金额"><span class="amount">¥{{ order.totalAmount }}</span></el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ order.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ order.shippingAddress }}</el-descriptions-item>
          <el-descriptions-item label="物流公司">{{ order.logisticsCompany || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ order.trackingNo || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 商品明细 -->
      <el-card class="info-card" shadow="never">
        <template #header><span>📦 商品明细</span></template>
        <el-table :data="order.items" stripe style="width: 100%">
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="skuName" label="规格" width="120" />
          <el-table-column label="单价" width="100">
            <template #default="{ row }">¥{{ row.unitPrice }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">¥{{ (row.unitPrice * row.quantity).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 操作 -->
      <div class="actions" v-if="order.status === 'pending_shipment'">
        <el-button type="primary" @click="goShip">发货</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref(null)

const statusMap = {
  pending_payment: { label: '待付款', type: 'warning' },
  pending_shipment: { label: '待发货', type: 'primary' },
  shipped: { label: '已发货', type: 'info' },
  completed: { label: '已完成', type: 'success' },
  cancelled: { label: '已取消', type: 'danger' }
}
function statusLabel(s) { return statusMap[s]?.label || s }
function statusType(s) { return statusMap[s]?.type || '' }

async function fetchOrderDetail() {
  loading.value = true
  try {
    const data = await getOrderDetail(route.params.id)
    order.value = data
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('获取订单详情失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrderDetail()
})

function goShip() {
  router.push('/orders')
}
</script>

<style scoped>
.order-detail-view { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; align-items: center; gap: 16px; }
.page-header h2 { font-size: 18px; font-weight: 700; }
.detail-content { display: flex; flex-direction: column; gap: 16px; }
.info-card { border-radius: 8px; }
.amount { color: var(--merchant-primary); font-weight: 700; font-size: 18px; }
.actions { display: flex; justify-content: flex-end; }
</style>
