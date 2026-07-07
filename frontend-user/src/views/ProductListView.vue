<template>
  <div class="product-list-view container">
    <!-- 筛选条 -->
    <div class="filter-bar">
      <div class="filter-left">
        <span class="filter-label">排序：</span>
        <button
          v-for="opt in sortOptions" :key="opt.value"
          :class="['sort-btn', { active: sortBy === opt.value }]"
          @click="changeSort(opt.value)"
        >{{ opt.label }}</button>
      </div>
      <div class="filter-right">
        <div class="price-filter">
          <span>价格：</span>
          <input v-model.number="minPrice" type="number" placeholder="最低" class="price-input" @keyup.enter="applyFilter" />
          <span class="price-dash">-</span>
          <input v-model.number="maxPrice" type="number" placeholder="最高" class="price-input" @keyup.enter="applyFilter" />
          <button class="filter-confirm" @click="applyFilter">确定</button>
        </div>
      </div>
    </div>

    <!-- 商品网格 -->
    <div class="product-grid" v-loading="loading">
      <ProductCard v-for="product in products" :key="product.id" :product="product" />
    </div>
    <div v-if="!loading && products.length === 0" class="empty-state">
      <p>😮 没有找到相关商品</p>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper, total"
        @current-change="fetchProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductList } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()
const router = useRouter()

const products = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const sortBy = ref('default')
const minPrice = ref(null)
const maxPrice = ref(null)
const keyword = ref('')
const categoryId = ref(null)

const sortOptions = [
  { label: '综合', value: 'default' },
  { label: '价格从低到高', value: 'price_asc' },
  { label: '价格从高到低', value: 'price_desc' },
  { label: '销量优先', value: 'sales' }
]

async function fetchProducts() {
  loading.value = true
  try {
    const res = await getProductList({
      page: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value,
      keyword: keyword.value,
      categoryId: categoryId.value,
      minPrice: minPrice.value,
      maxPrice: maxPrice.value
    })
    products.value = res.items
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function changeSort(val) {
  sortBy.value = val
  currentPage.value = 1
  fetchProducts()
}

function applyFilter() {
  currentPage.value = 1
  fetchProducts()
}

function loadQueryParams() {
  keyword.value = route.query.keyword || ''
  categoryId.value = route.query.categoryId || null
  sortBy.value = route.query.sortBy || 'default'
  currentPage.value = 1
}

onMounted(() => {
  loadQueryParams()
  fetchProducts()
})

watch(() => route.query, () => {
  loadQueryParams()
  fetchProducts()
}, { deep: true })
</script>

<style scoped>
.product-list-view { padding: 16px 0 40px; }

.filter-bar {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; border-radius: var(--radius-md);
  padding: 12px 20px; margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
}
.filter-left { display: flex; align-items: center; gap: 4px; }
.filter-label { font-size: 14px; color: var(--text-secondary); margin-right: 8px; }
.sort-btn {
  padding: 6px 16px; border: 1px solid transparent;
  background: transparent; font-size: 13px; color: var(--text-regular);
  border-radius: var(--radius-sm); transition: all var(--transition-fast);
}
.sort-btn:hover { color: var(--jd-red); }
.sort-btn.active { background: var(--jd-red); color: #fff; font-weight: 600; }

.filter-right { display: flex; align-items: center; }
.price-filter { display: flex; align-items: center; gap: 4px; font-size: 13px; color: var(--text-secondary); }
.price-input {
  width: 70px; padding: 4px 8px; border: 1px solid var(--border-base);
  border-radius: var(--radius-sm); font-size: 13px;
}
.price-dash { margin: 0 4px; }
.filter-confirm {
  margin-left: 8px; padding: 4px 12px;
  border: 1px solid var(--jd-red); background: #fff; color: var(--jd-red);
  border-radius: var(--radius-sm); font-size: 13px;
}
.filter-confirm:hover { background: var(--jd-red); color: #fff; }

.product-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; min-height: 300px; }
.empty-state { text-align: center; padding: 80px 0; font-size: 16px; color: var(--text-secondary); }
.pagination { display: flex; justify-content: center; margin-top: 32px; }
</style>
