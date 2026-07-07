<template>
  <header class="app-header">
    <!-- 顶部条 -->
    <div class="top-bar">
      <div class="container top-bar-inner">
        <div class="top-left">
          <span>📍 {{ deliveryLocation }}</span>
        </div>
        <nav class="top-nav">
          <template v-if="userStore.isLoggedIn">
            <span class="welcome">你好，{{ userStore.nickname }}</span>
            <router-link to="/profile">我的订单</router-link>
            <router-link to="/profile">个人中心</router-link>
            <a href="javascript:void(0)" @click="handleLogout">退出</a>
          </template>
          <template v-else>
            <router-link to="/login">你好，请登录</router-link>
            <router-link to="/register" class="register-link">免费注册</router-link>
          </template>
          <span class="divider">|</span>
          <router-link to="/orders">我的订单</router-link>
          <router-link to="/profile">会员中心</router-link>
          <router-link to="/cart">购物车</router-link>
        </nav>
      </div>
    </div>

    <!-- 搜索区 -->
    <div class="search-section">
      <div class="container search-inner">
        <!-- Logo -->
        <router-link to="/" class="logo">
          <span class="logo-icon">🦞</span>
          <span class="logo-text">极东<em>商城</em></span>
        </router-link>

        <!-- 搜索框 -->
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索你想要的商品"
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </button>
        </div>

        <!-- 购物车 -->
        <router-link to="/cart" class="cart-entry">
          <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0" :max="99">
            <span class="cart-icon">
              <el-icon :size="22"><ShoppingCart /></el-icon>
              <span>购物车</span>
            </span>
          </el-badge>
        </router-link>
      </div>
    </div>

    <!-- 分类导航条 -->
    <nav class="category-nav">
      <div class="container category-nav-inner">
        <div class="all-categories" @mouseenter="showCategoryPanel = true" @mouseleave="showCategoryPanel = false">
          <span class="all-cats-text">
            <el-icon><Menu /></el-icon>
            全部商品分类
          </span>
          <!-- 分类悬浮面板 -->
          <transition name="fade">
            <div v-show="showCategoryPanel" class="category-panel">
              <div
                v-for="cat in categories"
                :key="cat.id"
                class="category-item"
                @mouseenter="activeCat = cat"
                @mouseleave="activeCat = null"
              >
                <span class="cat-icon">{{ cat.icon }}</span>
                <span class="cat-name">{{ cat.name }}</span>
                <el-icon class="cat-arrow"><ArrowRight /></el-icon>
                <!-- 子分类 -->
                <div v-if="activeCat === cat" class="sub-categories">
                  <router-link
                    v-for="sub in cat.children"
                    :key="sub.id"
                    :to="{ path: '/product-list', query: { categoryId: sub.id } }"
                    class="sub-cat-link"
                  >
                    {{ sub.name }}
                  </router-link>
                </div>
              </div>
            </div>
          </transition>
        </div>
        <ul class="nav-links">
          <li><router-link to="/">首页</router-link></li>
          <li><router-link to="/product-list">全部商品</router-link></li>
          <li><router-link to="/orders">我的订单</router-link></li>
          <li><router-link to="/profile">会员中心</router-link></li>
        </ul>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { getCategories } from '@/api/product'
import { mockCategories } from '@/utils/mock-data'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const showCategoryPanel = ref(false)
const activeCat = ref(null)
const categories = ref(mockCategories)
const deliveryLocation = ref('北京')

onMounted(async () => {
  try {
    categories.value = await getCategories()
  } catch (e) {
    categories.value = mockCategories
  }
  if (userStore.isLoggedIn) {
    cartStore.fetchCart()
  }
})

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/product-list', query: { keyword: searchKeyword.value.trim() } })
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.app-header {
  background: #fff;
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 1000;
}

/* 顶部条 */
.top-bar {
  background: #f7f7f7;
  border-bottom: 1px solid var(--border-light);
  font-size: 12px;
  color: var(--text-secondary);
}

.top-bar-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 32px;
}

.top-nav {
  display: flex;
  align-items: center;
  gap: 16px;
}

.top-nav a {
  color: var(--text-secondary);
}

.top-nav a:hover {
  color: var(--jd-red);
}

.welcome {
  color: var(--jd-red);
  font-weight: 600;
}

.register-link {
  color: var(--jd-red) !important;
  font-weight: 600;
}

.divider {
  color: var(--border-base);
}

/* 搜索区 */
.search-section {
  padding: 20px 0;
  border-bottom: 2px solid var(--jd-red);
}

.search-inner {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 36px;
}

.logo-text {
  font-size: 28px;
  font-weight: 800;
  color: var(--jd-red);
  letter-spacing: -1px;
}

.logo-text em {
  font-style: normal;
  color: var(--text-primary);
}

.search-box {
  flex: 1;
  display: flex;
  max-width: 600px;
  border: 2px solid var(--jd-red);
  border-radius: 4px;
  overflow: hidden;
}

.search-input {
  flex: 1;
  border: none;
  padding: 8px 16px;
  font-size: 14px;
  background: #fff;
}

.search-btn {
  background: var(--jd-red);
  color: #fff;
  padding: 8px 24px;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: background var(--transition-fast);
}

.search-btn:hover {
  background: var(--jd-red-dark);
}

.cart-entry {
  flex-shrink: 0;
  border: 1px solid var(--border-base);
  padding: 8px 20px;
  border-radius: 4px;
  color: var(--jd-red);
  background: #fff;
  transition: all var(--transition-fast);
}

.cart-entry:hover {
  border-color: var(--jd-red);
  background: #fff5f5;
}

.cart-icon {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

/* 分类导航条 */
.category-nav {
  background: #fff;
  border-bottom: 1px solid var(--border-light);
}

.category-nav-inner {
  display: flex;
  align-items: center;
  height: 40px;
}

.all-categories {
  position: relative;
  width: 200px;
  height: 40px;
  display: flex;
  align-items: center;
  background: var(--jd-red);
  color: #fff;
  border-radius: 4px 4px 0 0;
  cursor: pointer;
}

.all-cats-text {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  font-size: 14px;
  font-weight: 600;
  width: 100%;
}

.category-panel {
  position: absolute;
  top: 40px;
  left: 0;
  width: 200px;
  background: #fff;
  border: 1px solid var(--border-light);
  border-top: none;
  box-shadow: var(--shadow-md);
  z-index: 100;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  font-size: 13px;
  color: var(--text-regular);
  cursor: pointer;
  transition: background var(--transition-fast);
  position: relative;
}

.category-item:hover {
  background: #fff5f5;
  color: var(--jd-red);
}

.cat-icon {
  margin-right: 8px;
  font-size: 16px;
}

.cat-name {
  flex: 1;
}

.cat-arrow {
  font-size: 12px;
  opacity: 0.5;
}

.sub-categories {
  position: absolute;
  left: 200px;
  top: 0;
  min-width: 200px;
  background: #fff;
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-lg);
  padding: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sub-cat-link {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  background: #f5f5f5;
  color: var(--text-regular);
  transition: all var(--transition-fast);
}

.sub-cat-link:hover {
  background: var(--jd-red);
  color: #fff;
}

.nav-links {
  display: flex;
  gap: 24px;
  padding-left: 24px;
}

.nav-links a {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  padding: 8px 0;
  position: relative;
}

.nav-links a.router-link-active {
  color: var(--jd-red);
  font-weight: 700;
}

.nav-links a:hover {
  color: var(--jd-red);
}
</style>
