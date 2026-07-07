<template>
  <div class="product-detail-view container" v-loading="loading">
    <template v-if="product">
      <!-- 面包屑 -->
      <el-breadcrumb :separator="'>'" class="breadcrumb">
        <el-breadcrumb-item to="/">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/product-list', query: { categoryId: product.categoryId } }">{{ product.categoryName }}</el-breadcrumb-item>
        <el-breadcrumb-item>商品详情</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 商品主信息 -->
      <div class="detail-main">
        <!-- 图片区 -->
        <div class="image-section">
          <div class="main-image" :style="{ background: currentImage }"></div>
          <div class="image-thumbs">
            <div
              v-for="(img, idx) in product.images" :key="idx"
              :class="['thumb', { active: currentImage === img }]"
              :style="{ background: img }"
              @click="currentImage = img"
            ></div>
          </div>
        </div>

        <!-- 信息区 -->
        <div class="info-section">
          <h1 class="product-title">{{ product.name }}</h1>
          <p class="product-subtitle">{{ product.subtitle }}</p>

          <div class="price-box">
            <div class="price-current">
              <span class="price-symbol">¥</span>
              <span class="price-integer">{{ Math.floor(currentSku ? currentSku.price : product.price) }}</span>
              <span class="price-decimal">.00</span>
            </div>
            <span class="price-tag">极东价</span>
          </div>

          <!-- SKU 选择 -->
          <div class="sku-section">
            <div class="sku-group" v-for="(values, attr) in skuAttributes" :key="attr">
              <span class="sku-label">{{ attr }}</span>
              <div class="sku-options">
                <button
                  v-for="val in values" :key="val"
                  :class="['sku-option', { active: selectedAttrs[attr] === val }]"
                  @click="selectAttr(attr, val)"
                >{{ val }}</button>
              </div>
            </div>
          </div>

          <!-- 数量 -->
          <div class="quantity-section">
            <span class="qty-label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="currentSku?.stock || 99" size="large" />
            <span class="stock-info">库存 {{ currentSku?.stock || product.stock }} 件</span>
          </div>

          <!-- 按钮 -->
          <div class="action-buttons">
            <button class="btn-cart" @click="handleAddCart">
              <el-icon><ShoppingCart /></el-icon> 加入购物车
            </button>
            <button class="btn-buy" @click="handleBuyNow">立即购买</button>
          </div>

          <!-- 服务保障 -->
          <div class="service-tags">
            <span>✅ 正品保障</span>
            <span>🚚 极速配送</span>
            <span>↩️ 七天无理由</span>
            <span>🎧 24h客服</span>
          </div>
        </div>
      </div>

      <!-- 详情Tab -->
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="商品详情" name="detail">
          <div class="detail-content">
            <p>{{ product.description }}</p>
            <div class="spec-table">
              <h3>规格参数</h3>
              <table>
                <tbody>
                  <tr v-for="spec in product.specs" :key="spec.label">
                    <td class="spec-label">{{ spec.label }}</td>
                    <td>{{ spec.value }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="用户评价" name="reviews">
          <div class="reviews-section">
            <div class="review-summary">
              <div class="rating-overview">
                <span class="big-rating">{{ product.rating }}</span>
                <span>好评率 {{ Math.floor(Math.random() * 5 + 95) }}%</span>
              </div>
            </div>
            <div v-for="review in product.reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <span class="review-user">{{ review.userName }}</span>
                <span class="review-rating">{{ '⭐'.repeat(review.rating) }}</span>
                <span class="review-date">{{ review.createdAt }}</span>
              </div>
              <p class="review-content">{{ review.content }}</p>
              <span class="review-sku">{{ review.skuName }}</span>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api/product'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)
const currentImage = ref('')
const quantity = ref(1)
const activeTab = ref('detail')
const selectedAttrs = ref({})

const skuAttributes = computed(() => {
  if (!product.value?.skus) return {}
  const attrs = {}
  product.value.skus.forEach(sku => {
    if (sku.attributes) {
      Object.entries(sku.attributes).forEach(([key, val]) => {
        if (!attrs[key]) attrs[key] = new Set()
        attrs[key].add(val)
      })
    }
  })
  const result = {}
  for (const [key, set] of Object.entries(attrs)) {
    result[key] = [...set]
  }
  return result
})

const currentSku = computed(() => {
  if (!product.value?.skus) return null
  return product.value.skus.find(sku => {
    return Object.entries(selectedAttrs.value).every(([key, val]) => sku.attributes[key] === val)
  })
})

function selectAttr(attr, val) {
  selectedAttrs.value = { ...selectedAttrs.value, [attr]: val }
}

async function fetchDetail() {
  loading.value = true
  try {
    const data = await getProductDetail(route.params.id)
    product.value = data
    currentImage.value = data.images?.[0] || data.image
    // 默认选第一个SKU属性
    if (data.skus?.length > 0) {
      const firstSku = data.skus[0]
      selectedAttrs.value = { ...firstSku.attributes }
    }
  } catch (e) {
    ElMessage.error('商品不存在')
    router.push('/product-list')
  } finally {
    loading.value = false
  }
}

function handleAddCart() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  if (!currentSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }
  cartStore.addToCart(product.value, currentSku.value, quantity.value)
}

function handleBuyNow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  if (!currentSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }
  // 直接跳转结算页
  router.push({
    path: '/checkout',
    query: {
      productId: product.value.id,
      skuId: currentSku.value.id,
      quantity: quantity.value
    }
  })
}

onMounted(fetchDetail)
watch(() => route.params.id, fetchDetail)
</script>

<style scoped>
.product-detail-view { padding: 16px 0 40px; }
.breadcrumb { margin-bottom: 16px; }

.detail-main { display: flex; gap: 24px; background: #fff; border-radius: var(--radius-md); padding: 24px; margin-bottom: 24px; }

.image-section { width: 400px; flex-shrink: 0; }
.main-image { width: 400px; height: 400px; border-radius: var(--radius-md); margin-bottom: 12px; }
.image-thumbs { display: flex; gap: 8px; }
.thumb { width: 60px; height: 60px; border-radius: var(--radius-sm); cursor: pointer; border: 2px solid transparent; }
.thumb.active { border-color: var(--jd-red); }

.info-section { flex: 1; }
.product-title { font-size: 22px; font-weight: 700; color: var(--text-primary); margin-bottom: 8px; line-height: 1.4; }
.product-subtitle { font-size: 13px; color: var(--text-secondary); margin-bottom: 16px; }

.price-box { background: linear-gradient(135deg, #fff5f5, #fff); border-radius: var(--radius-md); padding: 16px 20px; margin-bottom: 24px; display: flex; align-items: baseline; gap: 12px; }
.price-current { color: var(--jd-red); }
.price-current .price-symbol { font-size: 16px; }
.price-current .price-integer { font-size: 36px; font-weight: 800; }
.price-current .price-decimal { font-size: 16px; }
.price-tag { font-size: 12px; color: var(--text-secondary); border: 1px solid var(--border-base); padding: 2px 8px; border-radius: var(--radius-sm); }

.sku-section { margin-bottom: 24px; }
.sku-group { margin-bottom: 16px; display: flex; align-items: flex-start; gap: 12px; }
.sku-label { font-size: 13px; color: var(--text-secondary); min-width: 50px; padding-top: 6px; }
.sku-options { display: flex; flex-wrap: wrap; gap: 8px; }
.sku-option { padding: 6px 16px; border: 1px solid var(--border-base); border-radius: var(--radius-sm); background: #fff; font-size: 13px; color: var(--text-regular); transition: all var(--transition-fast); }
.sku-option:hover { border-color: var(--jd-red); color: var(--jd-red); }
.sku-option.active { border-color: var(--jd-red); background: #fff5f5; color: var(--jd-red); font-weight: 600; }

.quantity-section { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.qty-label { font-size: 13px; color: var(--text-secondary); }
.stock-info { font-size: 12px; color: var(--text-secondary); }

.action-buttons { display: flex; gap: 12px; margin-bottom: 24px; }
.btn-cart, .btn-buy { flex: 1; padding: 14px 0; font-size: 16px; font-weight: 700; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; gap: 6px; transition: all var(--transition-fast); }
.btn-cart { background: #fff5f5; color: var(--jd-red); border: 1px solid var(--jd-red); }
.btn-cart:hover { background: #ffe8e8; }
.btn-buy { background: var(--jd-red-gradient); color: #fff; }
.btn-buy:hover { opacity: 0.9; }

.service-tags { display: flex; gap: 16px; font-size: 12px; color: var(--text-secondary); }

.detail-tabs { background: #fff; border-radius: var(--radius-md); padding: 20px; }
.detail-content p { font-size: 14px; color: var(--text-regular); line-height: 1.8; margin-bottom: 24px; }
.spec-table h3 { font-size: 16px; margin-bottom: 12px; }
.spec-table table { width: 100%; border-collapse: collapse; }
.spec-table td { padding: 10px 16px; border: 1px solid var(--border-light); font-size: 13px; }
.spec-table .spec-label { width: 120px; background: #f9f9f9; color: var(--text-secondary); }

.reviews-section { padding: 8px 0; }
.review-summary { margin-bottom: 24px; }
.rating-overview { display: flex; align-items: baseline; gap: 12px; }
.big-rating { font-size: 36px; font-weight: 800; color: var(--jd-red); }
.review-item { padding: 16px 0; border-bottom: 1px solid var(--border-light); }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.review-user { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.review-rating { font-size: 12px; }
.review-date { font-size: 12px; color: var(--text-secondary); margin-left: auto; }
.review-content { font-size: 14px; color: var(--text-regular); line-height: 1.6; margin-bottom: 4px; }
.review-sku { font-size: 12px; color: var(--text-secondary); }
</style>
