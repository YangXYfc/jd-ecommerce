<template>
  <div class="orders-view">
    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索订单号/用户/商家" style="width: 300px" clearable />
      <el-select v-model="filterStatus" placeholder="订单状态" style="width: 140px; margin-left: 12px">
        <el-option label="全部" value="" />
        <el-option label="待付款" value="pending_payment" />
        <el-option label="待发货" value="pending_shipment" />
        <el-option label="已发货" value="shipped" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
    </div>

    <el-table :data="filteredOrders" stripe style="width: 100%; margin-top: 16px">
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="userName" label="用户" width="100" />
      <el-table-column prop="merchantName" label="商家" width="180" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="itemCount" label="商品数" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(currentOrder.status)" size="small">{{ statusLabel(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentOrder.userName }}</el-descriptions-item>
        <el-descriptions-item label="商家">{{ currentOrder.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="商品数">{{ currentOrder.itemCount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { mockAdminOrders } from '@/utils/mock-data'

const orders = ref([...mockAdminOrders])
const searchKeyword = ref('')
const filterStatus = ref('')
const detailVisible = ref(false)
const currentOrder = ref(null)

const statusMap = {
  pending_payment: { label: '待付款', type: 'warning' },
  pending_shipment: { label: '待发货', type: 'primary' },
  shipped: { label: '已发货', type: 'info' },
  completed: { label: '已完成', type: 'success' },
  cancelled: { label: '已取消', type: 'danger' }
}
function statusLabel(s) { return statusMap[s]?.label || s }
function statusType(s) { return statusMap[s]?.type || '' }

const filteredOrders = computed(() => {
  let list = orders.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(o => o.orderNo.includes(kw) || o.userName.includes(kw) || o.merchantName.includes(kw))
  }
  if (filterStatus.value) list = list.filter(o => o.status === filterStatus.value)
  return list
})

function viewDetail(row) {
  currentOrder.value = row
  detailVisible.value = true
}
</script>

<style scoped>
.toolbar { display: flex; align-items: center; }
</style>
