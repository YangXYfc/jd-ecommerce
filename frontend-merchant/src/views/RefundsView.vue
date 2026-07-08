<template>
  <div class="refunds-view" v-loading="loading">
    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索订单号/买家" style="width: 250px" clearable @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-select v-model="filterStatus" placeholder="退款状态" style="width: 120px; margin-left: 12px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="待处理" value="pending" />
        <el-option label="已同意" value="approved" />
        <el-option label="已拒绝" value="rejected" />
      </el-select>
      <el-button type="primary" style="margin-left: 12px" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="refunds" stripe style="width: 100%; margin-top: 16px" :row-class-name="rowClassName">
      <el-table-column prop="orderNo" label="订单号" width="140" />
      <el-table-column prop="userName" label="买家" width="100" />
      <el-table-column prop="productName" label="商品" width="160" />
      <el-table-column prop="reason" label="退款原因" width="120" />
      <el-table-column prop="amount" label="退款金额" width="100">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isUrgent && row.status === 'pending' ? 'danger' : row.status === 'approved' ? 'success' : row.status === 'rejected' ? 'info' : 'warning'" size="small">
            {{ row.status === 'approved' ? '已同意' : row.status === 'rejected' ? '已拒绝' : '待处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="appliedAt" label="申请时间" width="160" />
      <el-table-column label="截止时间" width="160">
        <template #default="{ row }">
          <span :class="{ 'deadline-urgent': row.isUrgent && row.status === 'pending' }">{{ row.deadline }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link @click="viewDetail(row)">详情</el-button>
          <template v-if="row.status === 'pending'">
            <el-button size="small" type="success" @click="openAudit(row, 'approved')">同意</el-button>
            <el-button size="small" type="danger" @click="openAudit(row, 'rejected')">拒绝</el-button>
          </template>
          <el-button v-if="row.status === 'approved'" size="small" type="primary" @click="confirmReceipt(row)">确认收货</el-button>
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
        @size-change="fetchRefunds"
        @current-change="fetchRefunds"
      />
    </div>

    <!-- 退款详情弹窗 -->
    <el-dialog v-model="detailVisible" title="退款详情" width="600px">
      <template v-if="currentRefund">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentRefund.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="买家">{{ currentRefund.userName }}</el-descriptions-item>
          <el-descriptions-item label="商品">{{ currentRefund.productName }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ currentRefund.amount }}</el-descriptions-item>
          <el-descriptions-item label="退款原因">{{ currentRefund.reason }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRefund.status === 'approved' ? 'success' : currentRefund.status === 'rejected' ? 'info' : 'warning'" size="small">
              {{ currentRefund.status === 'approved' ? '已同意' : currentRefund.status === 'rejected' ? '已拒绝' : '待处理' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentRefund.appliedAt }}</el-descriptions-item>
          <el-descriptions-item label="处理截止">{{ currentRefund.deadline }}</el-descriptions-item>
          <el-descriptions-item label="买家备注" :span="2">{{ currentRefund.userRemark }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRefund.merchantRemark" label="商家备注" :span="2">{{ currentRefund.merchantRemark }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" :title="auditResult === 'approved' ? '同意退款' : '拒绝退款'" width="500px">
      <div v-if="currentRefund" style="margin-bottom: 16px">
        <p style="margin-bottom: 8px"><strong>订单号：</strong>{{ currentRefund.orderNo }}</p>
        <p style="margin-bottom: 8px"><strong>退款金额：</strong>¥{{ currentRefund.amount }}</p>
        <p style="margin-bottom: 8px"><strong>退款原因：</strong>{{ currentRefund.reason }}</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="处理备注">
          <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请填写处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button :type="auditResult === 'approved' ? 'success' : 'danger'" :loading="auditLoading" @click="handleAudit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getRefundList, approveRefund, rejectRefund, confirmRefundReceipt } from '@/api/merchant'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const refunds = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const auditVisible = ref(false)
const auditLoading = ref(false)
const currentRefund = ref(null)
const auditResult = ref('approved')
const auditRemark = ref('')

async function fetchRefunds() {
  loading.value = true
  try {
    const res = await getRefundList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value || undefined
    })
    refunds.value = res.list || res.items || res.records || []
    total.value = res.total || 0
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('获取退款列表失败')
    }
    refunds.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchRefunds()
}

function rowClassName({ row }) {
  if (row.isUrgent && row.status === 'pending') return 'urgent-row'
  return ''
}

function viewDetail(row) {
  currentRefund.value = row
  detailVisible.value = true
}

function openAudit(row, result) {
  currentRefund.value = row
  auditResult.value = result
  auditRemark.value = ''
  auditVisible.value = true
}

async function handleAudit() {
  auditLoading.value = true
  try {
    if (auditResult.value === 'approved') {
      await approveRefund(currentRefund.value.id, auditRemark.value)
    } else {
      await rejectRefund(currentRefund.value.id, auditRemark.value)
    }
    ElMessage.success(auditResult.value === 'approved' ? '已同意退款' : '已拒绝退款')
    auditVisible.value = false
    fetchRefunds()
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('操作失败')
    }
  } finally {
    auditLoading.value = false
  }
}

async function confirmReceipt(row) {
  try {
    await ElMessageBox.confirm('确认已收到买家退回的商品？', '确认收货', { type: 'info' })
    await confirmRefundReceipt(row.id)
    ElMessage.success('已确认收货，退款完成')
    fetchRefunds()
  } catch (e) {}
}

// 初始加载
fetchRefunds()
</script>

<style scoped>
.toolbar { display: flex; align-items: center; }
:deep(.urgent-row) { background-color: #fff1f0 !important; }
.deadline-urgent { color: var(--merchant-danger); font-weight: 600; }
</style>
