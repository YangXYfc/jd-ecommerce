<template>
  <div class="refund-detail-view container" v-loading="loading">
    <template v-if="refund">
      <el-breadcrumb separator=">" class="breadcrumb">
        <el-breadcrumb-item to="/orders">我的订单</el-breadcrumb-item>
        <el-breadcrumb-item>退款详情</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 退款状态步骤条 -->
      <div class="status-card">
        <div class="status-header">
          <span class="status-label">当前状态</span>
          <span class="status-value" :style="{ color: getStatusInfo.color }">{{ getStatusInfo.label }}</span>
        </div>
        <p class="status-desc">{{ getStatusInfo.desc }}</p>

        <!-- 步骤条 -->
        <el-steps :active="getStatusInfo.step" align-center class="refund-steps">
          <el-step title="申请退款" :description="formatDate(refund.appliedAt)" />
          <el-step title="商家审核" :description="refund.merchantAuditTime ? formatDate(refund.merchantAuditTime) : ''" />
          <el-step title="退货物流" :description="refund.returnLogisticsNo ? '已寄回' : ''" />
          <el-step title="退款完成" :description="refund.refundTime ? formatDate(refund.refundTime) : ''" />
        </el-steps>
      </div>

      <!-- 时间线 -->
      <div class="timeline-card">
        <h3>📋 退款进度</h3>
        <el-timeline>
          <el-timeline-item :timestamp="formatDate(refund.appliedAt)" placement="top" type="primary">
            <p>退款申请已提交</p>
            <p class="timeline-detail">退款原因：{{ refund.reason }}</p>
            <p class="timeline-detail">退款金额：¥{{ refund.amount.toFixed(2) }}</p>
          </el-timeline-item>
          <el-timeline-item v-if="refund.merchantAuditTime" :timestamp="formatDate(refund.merchantAuditTime)" placement="top" :type="refund.status === 'merchant_rejected' ? 'danger' : 'success'">
            <p>商家{{ refund.status === 'merchant_rejected' ? '拒绝' : '同意' }}退款</p>
            <p class="timeline-detail" v-if="refund.merchantAuditRemark">商家备注：{{ refund.merchantAuditRemark }}</p>
          </el-timeline-item>
          <el-timeline-item v-if="refund.appealTime" :timestamp="formatDate(refund.appealTime)" placement="top" type="warning">
            <p>已发起申诉</p>
            <p class="timeline-detail">申诉原因：{{ refund.appealReason }}</p>
          </el-timeline-item>
          <el-timeline-item v-if="refund.adminHandleTime" :timestamp="formatDate(refund.adminHandleTime)" placement="top" :type="refund.status === 'admin_approved' ? 'success' : 'danger'">
            <p>平台仲裁结果：{{ refund.status === 'admin_approved' ? '同意退款' : '拒绝退款' }}</p>
            <p class="timeline-detail" v-if="refund.adminRemark">仲裁意见：{{ refund.adminRemark }}</p>
          </el-timeline-item>
          <el-timeline-item v-if="refund.returnLogisticsNo" :timestamp="formatDate(refund.appliedAt)" placement="top" type="info">
            <p>退货物流已提交</p>
            <p class="timeline-detail">{{ refund.returnLogisticsCompany }}：{{ refund.returnLogisticsNo }}</p>
          </el-timeline-item>
          <el-timeline-item v-if="refund.refundTime" :timestamp="formatDate(refund.refundTime)" placement="top" type="success">
            <p>退款已完成</p>
            <p class="timeline-detail">退款金额 ¥{{ refund.amount.toFixed(2) }} 已原路退回</p>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 退款信息 -->
      <div class="info-card">
        <h3>📦 退款商品</h3>
        <div v-for="item in refund.items" :key="item.id" class="product-row">
          <ProductImage class="product-image" :src="item.image" :alt="item.productName" :fallback-index="item.productId || 0" />
          <div class="product-info">
            <p class="product-name text-ellipsis-2">{{ item.productName }}</p>
            <span class="product-sku">{{ item.skuName }}</span>
          </div>
          <div class="product-price">¥{{ item.unitPrice.toFixed(2) }} × {{ item.quantity }}</div>
        </div>
        <div class="refund-total">退款总额：<span>¥{{ refund.amount.toFixed(2) }}</span></div>
      </div>

      <!-- 操作区 -->
      <div class="action-card" v-if="showActions">
        <!-- 填写退货物流 -->
        <div v-if="refund.status === 'merchant_approved' && !refund.returnLogisticsNo" class="action-section">
          <h3>📦 填写退货物流</h3>
          <p class="action-tip">商家已同意退款，请将商品寄回并填写物流信息</p>
          <div class="logistics-form">
            <el-select v-model="logisticsForm.company" placeholder="物流公司" size="large">
              <el-option label="顺丰速运" value="顺丰速运" />
              <el-option label="中通快递" value="中通快递" />
              <el-option label="圆通速递" value="圆通速递" />
              <el-option label="极兔速递" value="极兔速递" />
            </el-select>
            <el-input v-model="logisticsForm.trackingNo" placeholder="物流单号" size="large" />
            <button class="btn primary" @click="handleSubmitLogistics">提交</button>
          </div>
        </div>

        <!-- 申诉按钮（商家超时未审核 或 商家拒绝） -->
        <div v-if="canAppeal" class="action-section appeal-section">
          <h3>⚠️ 申请平台介入</h3>
          <p class="action-tip" v-if="refund.isOvertime">商家已超时未审核，您可以直接申请平台介入处理</p>
          <p class="action-tip" v-else-if="refund.status === 'merchant_rejected'">商家拒绝了您的退款申请，您可以申请平台仲裁</p>
          <el-input v-model="appealReason" type="textarea" :rows="3" placeholder="请说明申诉原因" />
          <button class="btn appeal-btn" @click="handleAppeal">申请平台介入</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRefundDetail, submitReturnLogistics, submitAppeal } from '@/api/refund'
import { refundStatusMap } from '@/utils/mock-data'
import { ElMessage } from 'element-plus'
import ProductImage from '@/components/ProductImage.vue'

const route = useRoute()
const router = useRouter()
const refund = ref(null)
const loading = ref(true)
const logisticsForm = ref({ company: '', trackingNo: '' })
const appealReason = ref('')

const getStatusInfo = computed(() => {
  if (!refund.value) return { label: '', color: '', desc: '', step: 0 }
  return refundStatusMap[refund.value.status] || { label: '未知', color: '#909399', desc: '', step: 0 }
})

const showActions = computed(() => {
  if (!refund.value) return false
  return ['merchant_approved', 'merchant_rejected', 'appealing', 'applied'].includes(refund.value.status)
})

const canAppeal = computed(() => {
  if (!refund.value) return false
  return refund.value.status === 'merchant_rejected' || (refund.value.status === 'applied' && refund.value.isOvertime)
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

async function fetchDetail() {
  loading.value = true
  try {
    refund.value = await getRefundDetail(route.params.id)
  } catch (e) {
    ElMessage.error('退款记录不存在')
    router.push('/orders')
  } finally {
    loading.value = false
  }
}

async function handleSubmitLogistics() {
  if (!logisticsForm.value.company || !logisticsForm.value.trackingNo) {
    ElMessage.warning('请填写完整物流信息')
    return
  }
  try {
    await submitReturnLogistics(refund.value.id, logisticsForm.value)
    ElMessage.success('物流信息已提交')
    fetchDetail()
  } catch (e) { ElMessage.error('提交失败') }
}

async function handleAppeal() {
  if (!appealReason.value.trim()) {
    ElMessage.warning('请填写申诉原因')
    return
  }
  try {
    await submitAppeal(refund.value.id, appealReason.value)
    ElMessage.success('申诉已提交，等待平台处理')
    fetchDetail()
  } catch (e) { ElMessage.error('提交失败') }
}

onMounted(fetchDetail)
</script>

<style scoped>
.refund-detail-view { padding: 16px 0 40px; }
.breadcrumb { margin-bottom: 16px; }

.status-card { background: #fff; border-radius: var(--radius-md); padding: 24px; margin-bottom: 16px; box-shadow: var(--shadow-sm); }
.status-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.status-label { font-size: 14px; color: var(--text-secondary); }
.status-value { font-size: 20px; font-weight: 700; }
.status-desc { font-size: 13px; color: var(--text-secondary); margin-bottom: 20px; }
.refund-steps { margin-top: 16px; }

.timeline-card { background: #fff; border-radius: var(--radius-md); padding: 20px; margin-bottom: 16px; box-shadow: var(--shadow-sm); }
.timeline-card h3 { font-size: 16px; font-weight: 700; margin-bottom: 16px; }
.timeline-detail { font-size: 13px; color: var(--text-secondary); }

.info-card { background: #fff; border-radius: var(--radius-md); padding: 20px; margin-bottom: 16px; box-shadow: var(--shadow-sm); }
.info-card h3 { font-size: 16px; font-weight: 700; margin-bottom: 12px; }
.product-row { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid var(--border-light); }
.product-image { width: 60px; height: 60px; border-radius: var(--radius-sm); flex-shrink: 0; }
.product-info { flex: 1; }
.product-name { font-size: 13px; margin-bottom: 4px; }
.product-sku { font-size: 12px; color: var(--text-secondary); }
.product-price { font-size: 13px; color: var(--text-regular); }
.refund-total { text-align: right; padding-top: 12px; font-size: 14px; color: var(--text-regular); }
.refund-total span { font-size: 20px; font-weight: 700; color: var(--jd-red); }

.action-card { background: #fff; border-radius: var(--radius-md); padding: 20px; box-shadow: var(--shadow-sm); }
.action-section { margin-bottom: 24px; }
.action-section h3 { font-size: 16px; font-weight: 700; margin-bottom: 8px; }
.action-tip { font-size: 13px; color: var(--text-secondary); margin-bottom: 12px; }
.logistics-form { display: flex; gap: 8px; }
.logistics-form .el-select { width: 140px; }
.logistics-form .el-input { flex: 1; }
.appeal-section { border-top: 1px solid var(--border-light); padding-top: 20px; }
.appeal-btn { background: var(--color-warning) !important; color: #fff !important; border-color: var(--color-warning) !important; margin-top: 12px; }

.btn { padding: 10px 32px; border: 1px solid var(--border-base); border-radius: var(--radius-md); background: #fff; font-size: 14px; color: var(--text-regular); transition: all var(--transition-fast); }
.btn:hover { border-color: var(--jd-red); color: var(--jd-red); }
.btn.primary { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.btn.primary:hover { background: var(--jd-red-dark); }
</style>
