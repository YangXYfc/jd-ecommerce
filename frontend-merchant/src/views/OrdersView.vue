<template>
  <div class="orders-view" v-loading="loading">
    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索订单号/买家" style="width: 250px" clearable @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-select v-model="filterStatus" placeholder="订单状态" style="width: 140px; margin-left: 12px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="待付款" value="pending_payment" />
        <el-option label="待发货" value="pending_shipment" />
        <el-option label="已发货" value="shipped" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-button type="primary" style="margin-left: 12px" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="orders" stripe style="width: 100%; margin-top: 16px">
      <el-table-column prop="orderNo" label="订单号" width="140" />
      <el-table-column prop="userName" label="买家" width="100" />
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
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link @click="$router.push(`/orders/${row.id}`)">详情</el-button>
          <el-button v-if="row.status === 'pending_shipment'" size="small" type="primary" @click="openShipDialog(row)">发货</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="margin-top: 16px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchOrders"
        @current-change="fetchOrders"
      />
    </div>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shipDialogVisible" title="订单发货" width="500px">
      <div v-if="currentOrder" style="margin-bottom: 16px">
        <p style="margin-bottom: 8px"><strong>订单号：</strong>{{ currentOrder.orderNo }}</p>
        <p style="margin-bottom: 8px"><strong>收货地址：</strong>{{ currentOrder.shippingAddress }}</p>
      </div>
      <el-form label-width="100px">
        <el-form-item label="物流公司">
          <el-select v-model="shipForm.logisticsCompany" placeholder="请选择" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="京东物流" value="京东物流" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" @click="handleShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getOrderList, shipOrder } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const orders = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const shipDialogVisible = ref(false)
const shipLoading = ref(false)
const currentOrder = ref(null)
const shipForm = ref({ logisticsCompany: '', trackingNo: '' })

const statusMap = {
  pending_payment: { label: '待付款', type: 'warning' },
  pending_shipment: { label: '待发货', type: 'primary' },
  shipped: { label: '已发货', type: 'info' },
  completed: { label: '已完成', type: 'success' },
  cancelled: { label: '已取消', type: 'danger' }
}
function statusLabel(s) { return statusMap[s]?.label || s }
function statusType(s) { return statusMap[s]?.type || '' }

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getOrderList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value || undefined
    })
    orders.value = res.list || res.items || res.records || []
    total.value = res.total || 0
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('获取订单列表失败')
    }
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchOrders()
}

function openShipDialog(row) {
  currentOrder.value = row
  shipForm.value = { logisticsCompany: '', trackingNo: '' }
  shipDialogVisible.value = true
}

async function handleShip() {
  if (!shipForm.value.logisticsCompany) {
    ElMessage.warning('请选择物流公司')
    return
  }
  if (!shipForm.value.trackingNo) {
    ElMessage.warning('请输入物流单号')
    return
  }
  shipLoading.value = true
  try {
    await shipOrder(currentOrder.value.id, {
      logisticsCompany: shipForm.value.logisticsCompany,
      trackingNo: shipForm.value.trackingNo
    })
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    fetchOrders()
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('发货失败')
    }
  } finally {
    shipLoading.value = false
  }
}

// 初始加载
fetchOrders()
</script>

<style scoped>
.toolbar { display: flex; align-items: center; }
</style>
