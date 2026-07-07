<template>
  <div class="refund-apply-view container">
    <el-breadcrumb separator=">" class="breadcrumb">
      <el-breadcrumb-item :to="`/order/${orderId}`">订单详情</el-breadcrumb-item>
      <el-breadcrumb-item>申请退款</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="refund-form-card">
      <h1 class="page-title">申请退款</h1>

      <!-- 退款原因 -->
      <div class="form-section">
        <label class="form-label">退款原因 <span class="required">*</span></label>
        <el-select v-model="form.reason" placeholder="请选择退款原因" size="large" class="full-width">
          <el-option v-for="r in reasons" :key="r" :label="r" :value="r" />
        </el-select>
      </div>

      <!-- 问题描述 -->
      <div class="form-section">
        <label class="form-label">问题描述</label>
        <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述您遇到的问题（200字以内）" maxlength="200" show-word-limit />
      </div>

      <!-- 退款金额 -->
      <div class="form-section">
        <label class="form-label">退款金额</label>
        <div class="refund-amount">¥{{ refundAmount.toFixed(2) }}</div>
      </div>

      <!-- 联系方式 -->
      <div class="form-section">
        <label class="form-label">联系电话 <span class="required">*</span></label>
        <el-input v-model="form.phone" placeholder="请输入联系电话" size="large" />
      </div>

      <!-- 提交 -->
      <div class="form-actions">
        <button class="btn-cancel" @click="$router.back()">取消</button>
        <button class="btn-submit" :disabled="submitting || !form.reason" @click="handleSubmit">
          {{ submitting ? '提交中...' : '提交申请' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail } from '@/api/order'
import { applyRefund as apiApplyRefund } from '@/api/refund'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const orderId = route.params.orderId
const order = ref(null)
const submitting = ref(false)

const reasons = [
  '商品质量问题',
  '收到商品与描述不符',
  '七天无理由退货',
  '商品损坏/少件',
  '未按约定时间发货',
  '不想要了'
]

const form = ref({
  reason: '',
  description: '',
  phone: ''
})

const refundAmount = ref(0)

onMounted(async () => {
  try {
    order.value = await getOrderDetail(orderId)
    refundAmount.value = order.value.totalAmount
  } catch (e) {
    ElMessage.error('获取订单信息失败')
    router.back()
  }
})

async function handleSubmit() {
  if (!form.value.reason) {
    ElMessage.warning('请选择退款原因')
    return
  }
  submitting.value = true
  try {
    const res = await apiApplyRefund({
      orderId: Number(orderId),
      reason: form.value.reason,
      description: form.value.description,
      amount: refundAmount.value,
      phone: form.value.phone
    })
    ElMessage.success('退款申请已提交')
    router.push(`/refund/${res.id}`)
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.refund-apply-view { padding: 16px 0 40px; }
.breadcrumb { margin-bottom: 16px; }

.refund-form-card { background: #fff; border-radius: var(--radius-md); padding: 32px; max-width: 600px; margin: 0 auto; box-shadow: var(--shadow-sm); }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 24px; text-align: center; }

.form-section { margin-bottom: 20px; }
.form-label { display: block; font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 8px; }
.required { color: var(--jd-red); }
.full-width { width: 100%; }
.refund-amount { font-size: 28px; font-weight: 800; color: var(--jd-red); }

.form-actions { display: flex; gap: 12px; justify-content: center; margin-top: 32px; }
.btn-cancel, .btn-submit { padding: 12px 40px; border-radius: var(--radius-md); font-size: 16px; font-weight: 600; transition: all var(--transition-fast); }
.btn-cancel { border: 1px solid var(--border-base); background: #fff; color: var(--text-regular); }
.btn-cancel:hover { border-color: var(--text-regular); }
.btn-submit { background: var(--jd-red-gradient); color: #fff; }
.btn-submit:disabled { background: #ccc; cursor: not-allowed; }
</style>
