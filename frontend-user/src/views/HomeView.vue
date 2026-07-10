<template>
  <div class="home-view">
    <section class="banner-section">
      <div class="container banner-inner">
        <div class="banner-left">
          <el-carousel height="460px" :interval="4000" arrow="hover">
            <el-carousel-item v-for="banner in banners" :key="banner.id">
              <div class="banner-slide" :style="imageStyle(banner.image)" @click="$router.push(banner.link)">
                <div class="banner-text">
                  <h2>{{ banner.title }}</h2>
                  <p>{{ banner.subtitle }}</p>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
        <div class="banner-right">
          <div class="user-panel" v-if="userStore.isLoggedIn">
            <div class="user-avatar" :style="{ background: userStore.userInfo?.avatar }"></div>
            <p class="welcome-text">Hi, {{ userStore.nickname }}</p>
            <p class="user-level">{{ userStore.userInfo?.level || '普通会员' }}</p>
            <div class="user-actions">
              <router-link to="/orders" class="user-action-btn">我的订单</router-link>
              <router-link to="/profile" class="user-action-btn outline">个人中心</router-link>
            </div>
          </div>
          <div class="user-panel" v-else>
            <p class="welcome-text">Hi, 欢迎来到极东商城</p>
            <div class="user-actions">
              <router-link to="/login" class="user-action-btn">登录</router-link>
              <router-link to="/register" class="user-action-btn outline">注册</router-link>
            </div>
          </div>
          <div class="news-panel">
            <h4>📢 公告</h4>
            <ul>
              <li>年终大促，全场低至5折</li>
              <li>PLUS会员专享优惠来袭</li>
              <li>新人注册即送200元券</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <section class="category-section container">
      <div class="cat-grid">
        <div v-for="cat in categories.slice(0, 10)" :key="cat.id" class="cat-entry" @click="$router.push({ path: '/product-list', query: { categoryId: cat.id } })">
          <span v-if="isImageSource(cat.icon)" class="cat-entry-icon cat-entry-image" :style="imageStyle(cat.icon)"></span>
          <span v-else class="cat-entry-icon">{{ cat.icon }}</span>
          <span class="cat-entry-name">{{ cat.name }}</span>
        </div>
      </div>
    </section>

    <section class="hot-section container">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">🔥</span>
          热销排行
        </h2>
        <router-link to="/product-list?sortBy=sales" class="more-link">查看更多 →</router-link>
      </div>
      <div class="hot-grid">
        <div v-for="(product, idx) in hotProducts" :key="product.id" class="hot-item">
          <span class="hot-rank" :class="{ 'top-3': idx < 3 }">{{ idx + 1 }}</span>
          <ProductCard :product="product" />
        </div>
      </div>
    </section>

    <section class="recommend-section container">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">✨</span>
          为你推荐
        </h2>
      </div>
      <div class="recommend-grid">
        <ProductCard v-for="product in recommendProducts" :key="product.id" :product="product" />
      </div>
      <div class="load-more">
        <button class="load-more-btn" @click="loadMore" :disabled="loading">
          {{ loading ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getBanners, getCategories, getProductList } from '@/api/product'
import { mockBanners, mockCategories } from '@/utils/mock-data'
import ProductCard from '@/components/ProductCard.vue'
import { imageStyle, isImageSource } from '@/utils/product-images'

const userStore = useUserStore()
const banners = ref(mockBanners)
const categories = ref(mockCategories)
const hotProducts = ref([])
const recommendProducts = ref([])
const loading = ref(false)
const page = ref(1)

onMounted(async () => {
  try { banners.value = await getBanners() } catch {}
  try { categories.value = await getCategories() } catch {}
  const hotRes = await getProductList({ sortBy: 'sales', size: 6 })
  hotProducts.value = hotRes.items
  await loadMore()
})

async function loadMore() {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getProductList({ page: page.value, size: 20 })
    recommendProducts.value.push(...res.items)
    page.value++
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.home-view { padding-bottom: 40px; }

.banner-section { margin-bottom: 20px; }
.banner-inner {
  display: flex;
  gap: 10px;
  height: 460px;
}
.banner-left { flex: 1; border-radius: var(--radius-md); overflow: hidden; }
.banner-slide {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.banner-text { text-align: center; color: #fff; }
.banner-text h2 { font-size: 42px; font-weight: 800; margin-bottom: 12px; text-shadow: 0 2px 8px rgba(0,0,0,0.2); }
.banner-text p { font-size: 20px; opacity: 0.9; }

.banner-right { width: 240px; display: flex; flex-direction: column; gap: 10px; }
.user-panel {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 20px;
  text-align: center;
  flex-shrink: 0;
}
.user-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  margin: 0 auto 10px;
}
.welcome-text { font-size: 16px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.user-level { font-size: 12px; color: var(--jd-red); margin-bottom: 12px; }
.user-actions { display: flex; gap: 8px; justify-content: center; }
.user-action-btn {
  display: inline-block;
  padding: 6px 20px;
  border-radius: var(--radius-full);
  background: var(--jd-red);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  transition: all var(--transition-fast);
}
.user-action-btn:hover { background: var(--jd-red-dark); color: #fff; }
.user-action-btn.outline { background: #fff; color: var(--jd-red); border: 1px solid var(--jd-red); }

.news-panel {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px;
  flex: 1;
}
.news-panel h4 { font-size: 14px; margin-bottom: 10px; color: var(--text-primary); }
.news-panel ul li { font-size: 12px; color: var(--text-secondary); margin-bottom: 8px; line-height: 1.6; }

.category-section { margin-bottom: 24px; }
.cat-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 8px;
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px;
}
.cat-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding: 8px 4px;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}
.cat-entry:hover { background: #fff5f5; }
.cat-entry-icon { font-size: 32px; margin-bottom: 6px; }
.cat-entry-image {
  width: 36px;
  height: 36px;
  display: block;
  background-size: cover;
  background-position: center;
  border-radius: 8px;
}
.cat-entry-name { font-size: 12px; color: var(--text-regular); }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section-title { font-size: 22px; font-weight: 700; color: var(--text-primary); display: flex; align-items: center; gap: 8px; }
.title-icon { font-size: 26px; }
.more-link { font-size: 14px; color: var(--text-secondary); }
.more-link:hover { color: var(--jd-red); }

.hot-section { margin-bottom: 32px; }
.hot-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.hot-item { position: relative; }
.hot-rank {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 10;
  width: 24px;
  height: 24px;
  background: var(--text-secondary);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0 0 4px 0;
}
.hot-rank.top-3 { background: var(--jd-red); }

.recommend-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; }
.load-more { text-align: center; margin-top: 24px; }
.load-more-btn {
  padding: 10px 48px;
  border: 1px solid var(--border-base);
  border-radius: var(--radius-full);
  background: #fff;
  font-size: 14px;
  color: var(--text-regular);
  transition: all var(--transition-fast);
}
.load-more-btn:hover { border-color: var(--jd-red); color: var(--jd-red); }
.load-more-btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
