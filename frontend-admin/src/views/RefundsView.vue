<template>
  <div class="refunds-view">
    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索订单号/用户/商家" style="width: 300px" clearable />
      <el-select v-model="filterStatus" placeholder="仲裁状态" style="width: 140px; margin-left: 12px">
        <el-option label="全部" value="" />
        <el-option label="待仲裁" value="appealing" />
        <el-option label="已同意" value="admin_approved" />
        <el-option label="已拒绝" value="admin_rejected" />
      </el-select>
    </div>

    <el-table :data="filteredRefunds" stripe style="width: 100%; margin-top: 16px" :row-class-name="rowClassName">
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="userName" label="用户" width="100" />
      <el-table-column prop="merchantName" label="商家" width="180" />
      <el-table-column prop="reason" label="退款原因" width="140" />
      <el-table-column prop="amount" label="退款金额" width="100">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.isUrgent && row.status === 'appealing' ? 'danger' : row.status === 'admin_approved' ? 'success' : row.status === 'admin_rejected' ? 'info' : 'warning'" size="small">
            {{ row.status === 'admin_approved' ? '已同意' : row.status === 'admin_rejected' ? '已拒绝' : '待仲裁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="appliedAt" label="申请时间" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link @click="viewDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'appealing'" size="small" type="primary" @click="openArbitrate(row)">仲裁</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 退款详情弹窗 -->
    <el-dialog v-model="detailVisible" title="退款详情" width="650px">
      <template v-if="currentRefund">
        <!-- 时间线 -->
        <el-timeline>
          <el-timeline-item :timestamp="currentRefund.appliedAt" placement="top" type="primary">
            <p>用户发起退款申请</p>
            <p style="font-size: 13px; color: #999;">原因：{{ currentRefund.reason }} | 金额：¥{{ currentRefund.amount }}</p>
          </el-timeline-item>
          <el-timeline-item v-if="currentRefund.merchantAuditTime" :timestamp="currentRefund.merchantAuditTime" placement="top" :type="currentRefund.merchantAuditResult === 'approved' ? 'success' : 'danger'">
            <p>商家{{ currentRefund.merchantAuditResult === 'approved' ? '同意' : '拒绝' }}退款</p>
            <p v-if="currentRefund.merchantAuditRemark" style="font-size: 13px; color: #999;">备注：{{ currentRefund.merchantAuditRemark }}</p>
          </el-timeline-item>
          <el-timeline-item v-else timestamp="未审核" placement="top" type="warning">
            <p>商家未审核（超时）</p>
          </el-timeline-item>
          <el-timeline-item v-if="currentRefund.appealTime" :timestamp="currentRefund.appealTime" placement="top" type="warning">
            <p>用户发起申诉</p>
            <p style="font-size: 13px; color: #999;">原因：{{ currentRefund.appealReason }}</p>
          </el-timeline-item>
          <el-timeline-item v-if="currentRefund.adminHandleTime" :timestamp="currentRefund.adminHandleTime" placement="top" :type="currentRefund.adminResult === 'approved' ? 'success' : 'danger'">
            <p>平台仲裁：{{ currentRefund.adminResult === 'approved' ? '同意退款' : '拒绝退款' }}</p>
            <p v-if="currentRefund.adminRemark" style="font-size: 13px; color: #999;">仲裁意见：{{ currentRefund.adminRemark }}</p>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-dialog>

    <!-- 仲裁弹窗 -->
    <el-dialog v-model="arbitrateVisible" title="退款仲裁" width="500px">
      <div v-if="currentRefund" style="margin-bottom: 16px">
        <p style="margin-bottom: 8px"><strong>订单号：</strong>{{ currentRefund.orderNo }}</p>
        <p style="margin-bottom: 8px"><strong>退款金额：</strong>¥{{ currentRefund.amount }}</p>
        <p style="margin-bottom: 8px"><strong>退款原因：</strong>{{ currentRefund.reason }}</p>
        <p style="margin-bottom: 8px"><strong>商家审核：</strong>{{ currentRefund.merchantAuditResult === 'approved' ? '同意' : currentRefund.merchantAuditResult === 'rejected' ? '拒绝' : '未审核' }}</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="仲裁结果">
          <el-radio-group v-model="arbitrateForm.result">
            <el-radio label="approved">同意退款</el-radio>
            <el-radio label="rejected">拒绝退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理意见">
          <el-input v-model="arbitrateForm.remark" type="textarea" :rows="3" placeholder="请填写仲裁处理意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="arbitrateVisible = false">取消</el-button>
        <el-button type="primary" @click="handleArbitrate">确认仲裁</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { mockRefundsArbitration } from '@/utils/mock-data'
import { ElMessage } from 'element-plus'

const refunds = ref([...mockRefundsArbitration])
const searchKeyword = ref('')
const filterStatus = ref('')
const detailVisible = ref(false)
const currentRefund = ref(null)
const arbitrateVisible = ref(false)
const arbitrateForm = ref({ result: 'approved', remark: '' })

const filteredRefunds = computed(() => {
  let list = refunds.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(r => r.orderNo.includes(kw) || r.userName.includes(kw) || r.merchantName.includes(kw))
  }
  if (filterStatus.value) list = list.filter(r => r.status === filterStatus.value)
  return list
})

function rowClassName({ row }) {
  if (row.isUrgent && row.status === 'appealing') return 'urgent-row'
  return ''
}

function viewDetail(row) {
  currentRefund.value = row
  detailVisible.value = true
}

function openArbitrate(row) {
  currentRefund.value = row
  arbitrateForm.value = { result: 'approved', remark: '' }
  arbitrateVisible.value = true
}

function handleArbitrate() {
  if (!arbitrateForm.value.remark) {
    ElMessage.warning('请填写处理意见')
    return
  }
  currentRefund.value.status = arbitrateForm.value.result === 'approved' ? 'admin_approved' : 'admin_rejected'
  currentRefund.value.adminResult = arbitrateForm.value.result
  currentRefund.value.adminRemark = arbitrateForm.value.remark
  currentRefund.value.adminHandleTime = new Date().toLocaleString()
  ElMessage.success('仲裁完成')
  arbitrateVisible.value = false
}
</script>

<style scoped>
.toolbar { display: flex; align-items: center; }
:deep(.urgent-row) { background-color: #fff1f0 !important; }
</style>
