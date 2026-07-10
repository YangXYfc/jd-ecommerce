<template>
  <div class="reviews-view" v-loading="loading">
    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索商品名称/买家" style="width: 250px" clearable @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-select v-model="filterRating" placeholder="评分" style="width: 100px; margin-left: 12px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="5星" :value="5" />
        <el-option label="4星" :value="4" />
        <el-option label="3星" :value="3" />
        <el-option label="2星" :value="2" />
        <el-option label="1星" :value="1" />
      </el-select>
      <el-button type="primary" style="margin-left: 12px" @click="handleSearch">搜索</el-button>
    </div>

    <div class="reviews-list" style="margin-top: 16px">
      <div v-for="review in reviews" :key="review.id" class="review-card">
        <div class="review-header">
          <div class="review-product">
            <span class="product-name">{{ review.productName }}</span>
            <span class="reviewer">{{ review.userName }}</span>
          </div>
          <div class="review-rating">
            <el-rate v-model="review.rating" disabled />
            <span class="review-date">{{ review.createdAt }}</span>
          </div>
        </div>
        <div class="review-content">{{ review.content }}</div>
        <div v-if="review.images && review.images.length" class="review-images">
          <div v-for="(img, i) in review.images" :key="i" class="review-img" :style="imageStyle(img, i)"></div>
        </div>
        <div class="review-footer">
          <div v-if="review.reply" class="reply-box">
            <span class="reply-label">商家回复：</span>
            <span class="reply-text">{{ review.reply }}</span>
          </div>
          <el-button v-else size="small" type="primary" @click="openReply(review)">回复</el-button>
          <el-button v-if="review.reply" size="small" link @click="openReply(review)">修改回复</el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div style="margin-top: 16px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchReviews"
        @current-change="fetchReviews"
      />
    </div>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyVisible" title="回复评价" width="500px">
      <div v-if="currentReview" style="margin-bottom: 16px">
        <p style="margin-bottom: 8px"><strong>商品：</strong>{{ currentReview.productName }}</p>
        <p style="margin-bottom: 8px"><strong>买家：</strong>{{ currentReview.userName }}</p>
        <p style="margin-bottom: 8px"><strong>评价：</strong>{{ currentReview.content }}</p>
      </div>
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="请输入回复内容" />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="handleReply">发送回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getReviewList, replyReview } from '@/api/merchant'
import { ElMessage } from 'element-plus'
import { imageStyle } from '@/utils/product-images'

const loading = ref(false)
const reviews = ref([])
const searchKeyword = ref('')
const filterRating = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const replyVisible = ref(false)
const replyLoading = ref(false)
const currentReview = ref(null)
const replyContent = ref('')

async function fetchReviews() {
  loading.value = true
  try {
    const res = await getReviewList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      rating: filterRating.value || undefined
    })
    reviews.value = res.list || res.items || res.records || []
    total.value = res.total || 0
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('获取评价列表失败')
    }
    reviews.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchReviews()
}

function openReply(review) {
  currentReview.value = review
  replyContent.value = review.reply || ''
  replyVisible.value = true
}

async function handleReply() {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replyLoading.value = true
  try {
    await replyReview(currentReview.value.id, replyContent.value)
    ElMessage.success('回复成功')
    replyVisible.value = false
    fetchReviews()
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('回复失败')
    }
  } finally {
    replyLoading.value = false
  }
}

// 初始加载
fetchReviews()
</script>

<style scoped>
.toolbar { display: flex; align-items: center; }
.reviews-list { display: flex; flex-direction: column; gap: 12px; }
.review-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: var(--shadow-sm); }
.review-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.review-product { display: flex; flex-direction: column; gap: 4px; }
.product-name { font-size: 15px; font-weight: 600; }
.reviewer { font-size: 12px; color: var(--merchant-text-secondary); }
.review-rating { display: flex; align-items: center; gap: 12px; }
.review-date { font-size: 12px; color: var(--merchant-text-secondary); }
.review-content { font-size: 14px; color: var(--merchant-text); line-height: 1.6; margin-bottom: 12px; }
.review-images { display: flex; gap: 8px; margin-bottom: 12px; }
.review-img { width: 60px; height: 60px; background: #f5f5f5; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 10px; color: #999; }
.review-footer { display: flex; align-items: center; gap: 12px; padding-top: 12px; border-top: 1px solid var(--merchant-border); }
.reply-box { flex: 1; font-size: 13px; }
.reply-label { color: var(--merchant-primary); font-weight: 600; }
.reply-text { color: var(--merchant-text-secondary); }
</style>
