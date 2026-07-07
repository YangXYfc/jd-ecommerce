<template>
  <div class="review-view container">
    <el-breadcrumb separator=">" class="breadcrumb">
      <el-breadcrumb-item :to="`/order/${orderId}`">订单详情</el-breadcrumb-item>
      <el-breadcrumb-item>发表评价</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="review-card">
      <h1 class="page-title">发表评价</h1>

      <!-- 商品列表 -->
      <div v-for="item in orderItems" :key="item.id" class="review-item">
        <div class="product-image" :style="{ background: item.image }"></div>
        <div class="product-info">
          <p class="product-name text-ellipsis-2">{{ item.productName }}</p>
          <span class="product-sku">{{ item.skuName }}</span>
        </div>
      </div>

      <!-- 评分 -->
      <div class="form-section">
        <label class="form-label">总体评分</label>
        <el-rate v-model="form.rating" :texts="['很差', '较差', '一般', '满意', '非常满意']" show-text />
      </div>

      <!-- 评价内容 -->
      <div class="form-section">
        <label class="form-label">评价内容</label>
        <el-input v-model="form.content" type="textarea" :rows="5" placeholder="分享您的使用感受，帮助其他买家做出选择（500字以内）" maxlength="500" show-word-limit />
      </div>

      <!-- 提交 -->
      <div class="form-actions">
        <button class="btn-cancel" @click="$router.back()">取消</button>
        <button class="btn-submit" :disabled="submitting || form.rating === 0" @click="handleSubmit">
          {{ submitting ? '提交中...' : '提交评价' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, submitReview } from '@/api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const orderId = route.params.orderId
const orderItems = ref([])
const submitting = ref(false)

const form = ref({
  rating: 5,
  content: ''
})

onMounted(async () => {
  try {
    const order = await getOrderDetail(orderId)
    orderItems.value = order.items
  } catch (e) {
    ElMessage.error('获取订单信息失败')
    router.back()
  }
})

async function handleSubmit() {
  if (form.value.rating === 0) {
    ElMessage.warning('请选择评分')
    return
  }
  submitting.value = true
  try {
    await submitReview(orderId, form.value)
    ElMessage.success('评价提交成功')
    router.push('/orders')
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.review-view { padding: 16px 0 40px; }
.breadcrumb { margin-bottom: 16px; }

.review-card { background: #fff; border-radius: var(--radius-md); padding: 32px; max-width: 600px; margin: 0 auto; box-shadow: var(--shadow-sm); }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 24px; text-align: center; }

.review-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border-light); margin-bottom: 20px; }
.product-image { width: 60px; height: 60px; border-radius: var(--radius-sm); flex-shrink: 0; }
.product-info { flex: 1; }
.product-name { font-size: 13px; margin-bottom: 4px; }
.product-sku { font-size: 12px; color: var(--text-secondary); }

.form-section { margin-bottom: 20px; }
.form-label { display: block; font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 8px; }

.form-actions { display: flex; gap: 12px; justify-content: center; margin-top: 24px; }
.btn-cancel, .btn-submit { padding: 12px 40px; border-radius: var(--radius-md); font-size: 16px; font-weight: 600; transition: all var(--transition-fast); }
.btn-cancel { border: 1px solid var(--border-base); background: #fff; color: var(--text-regular); }
.btn-submit { background: var(--jd-red-gradient); color: #fff; }
.btn-submit:disabled { background: #ccc; cursor: not-allowed; }
</style>
