<template>
  <div class="profile-view container">
    <h1 class="page-title">👤 个人中心</h1>

    <div class="profile-layout">
      <!-- 侧边导航 -->
      <aside class="profile-sidebar">
        <div class="user-card">
          <div class="user-avatar" :style="{ background: userStore.userInfo?.avatar || 'linear-gradient(135deg, #e1251b, #f5af19)' }"></div>
          <p class="user-name">{{ userStore.nickname }}</p>
          <p class="user-level">{{ userStore.userInfo?.level || '普通会员' }}</p>
        </div>
        <nav class="sidebar-nav">
          <router-link to="/profile" class="nav-item" :class="{ active: true }">个人中心</router-link>
          <router-link to="/orders" class="nav-item">我的订单</router-link>
          <router-link to="/profile/addresses" class="nav-item">收货地址</router-link>
          <router-link to="/cart" class="nav-item">我的购物车</router-link>
        </nav>
      </aside>

      <!-- 主内容 -->
      <div class="profile-main">
        <!-- 统计卡片 -->
        <div class="stats-grid">
          <div class="stat-card">
            <span class="stat-icon">📦</span>
            <div class="stat-info">
              <p class="stat-num">{{ orderStats.total }}</p>
              <p class="stat-label">全部订单</p>
            </div>
          </div>
          <div class="stat-card">
            <span class="stat-icon">⏳</span>
            <div class="stat-info">
              <p class="stat-num">{{ orderStats.pending }}</p>
              <p class="stat-label">待付款</p>
            </div>
          </div>
          <div class="stat-card">
            <span class="stat-icon">🚚</span>
            <div class="stat-info">
              <p class="stat-num">{{ orderStats.shipped }}</p>
              <p class="stat-label">待收货</p>
            </div>
          </div>
          <div class="stat-card">
            <span class="stat-icon">💰</span>
            <div class="stat-info">
              <p class="stat-num">{{ userStore.userInfo?.points || 0 }}</p>
              <p class="stat-label">积分</p>
            </div>
          </div>
        </div>

        <!-- 个人信息 -->
        <div class="info-card">
          <h3 class="card-title">个人信息</h3>
          <div class="info-list">
            <div class="info-row">
              <span class="info-label">昵称</span>
              <span class="info-value">{{ userStore.userInfo?.nickname || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ userStore.userInfo?.username || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">手机号</span>
              <span class="info-value">{{ userStore.userInfo?.phone || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ userStore.userInfo?.email || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">会员等级</span>
              <span class="info-value">{{ userStore.userInfo?.level || '普通会员' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">优惠券</span>
              <span class="info-value">{{ userStore.userInfo?.couponCount || 0 }} 张</span>
            </div>
          </div>
        </div>

        <!-- 最近订单 -->
        <div class="info-card">
          <div class="card-header">
            <h3 class="card-title">最近订单</h3>
            <router-link to="/orders" class="more-link">查看全部 →</router-link>
          </div>
          <div v-for="order in recentOrders" :key="order.id" class="recent-order" @click="$router.push(`/order/${order.id}`)">
            <div class="recent-order-info">
              <span class="recent-order-no">{{ order.orderNo }}</span>
              <span class="recent-order-date">{{ formatDate(order.createdAt) }}</span>
            </div>
            <span class="recent-order-status" :style="{ color: getStatusInfo(order.status).color }">{{ getStatusInfo(order.status).label }}</span>
            <span class="recent-order-amount">¥{{ order.totalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getOrderList } from '@/api/order'
import { orderStatusMap } from '@/utils/mock-data'

const userStore = useUserStore()
const recentOrders = ref([])
const orderStats = ref({ total: 0, pending: 0, shipped: 0 })

function getStatusInfo(status) {
  return orderStatusMap[status] || { label: '未知', color: '#909399' }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth()+1}-${d.getDate()}`
}

onMounted(async () => {
  try {
    const res = await getOrderList({ size: 5 })
    recentOrders.value = res.items
    orderStats.value.total = res.total
    orderStats.value.pending = recentOrders.value.filter(o => o.status === 'pending_payment').length
    orderStats.value.shipped = recentOrders.value.filter(o => o.status === 'shipped').length
  } catch (e) {}
  userStore.fetchUserInfo()
})
</script>

<style scoped>
.profile-view { padding: 16px 0 40px; }
.page-title { font-size: 24px; font-weight: 700; margin-bottom: 16px; }

.profile-layout { display: flex; gap: 16px; }
.profile-sidebar { width: 200px; flex-shrink: 0; }
.user-card { background: var(--jd-red-gradient); border-radius: var(--radius-md); padding: 24px; text-align: center; color: #fff; margin-bottom: 12px; }
.user-avatar { width: 64px; height: 64px; border-radius: 50%; margin: 0 auto 12px; border: 3px solid rgba(255,255,255,0.3); }
.user-name { font-size: 16px; font-weight: 700; }
.user-level { font-size: 12px; opacity: 0.9; margin-top: 4px; }

.sidebar-nav { background: #fff; border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.nav-item { display: block; padding: 12px 20px; font-size: 14px; color: var(--text-regular); border-left: 3px solid transparent; transition: all var(--transition-fast); }
.nav-item:hover { background: #fff5f5; color: var(--jd-red); }
.nav-item.active { border-left-color: var(--jd-red); color: var(--jd-red); font-weight: 600; background: #fff5f5; }

.profile-main { flex: 1; }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 16px; }
.stat-card { background: #fff; border-radius: var(--radius-md); padding: 16px; display: flex; align-items: center; gap: 12px; box-shadow: var(--shadow-sm); }
.stat-icon { font-size: 32px; }
.stat-num { font-size: 24px; font-weight: 800; color: var(--text-primary); }
.stat-label { font-size: 12px; color: var(--text-secondary); }

.info-card { background: #fff; border-radius: var(--radius-md); padding: 20px; margin-bottom: 16px; box-shadow: var(--shadow-sm); }
.card-title { font-size: 16px; font-weight: 700; margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.more-link { font-size: 13px; color: var(--text-secondary); }
.more-link:hover { color: var(--jd-red); }

.info-list { display: flex; flex-direction: column; gap: 12px; }
.info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--border-light); }
.info-label { font-size: 14px; color: var(--text-secondary); }
.info-value { font-size: 14px; color: var(--text-primary); }

.recent-order { display: flex; align-items: center; gap: 16px; padding: 12px 0; border-bottom: 1px solid var(--border-light); cursor: pointer; }
.recent-order:hover { background: #fafafa; }
.recent-order-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.recent-order-no { font-size: 13px; color: var(--text-primary); }
.recent-order-date { font-size: 12px; color: var(--text-secondary); }
.recent-order-status { font-size: 13px; font-weight: 600; }
.recent-order-amount { font-size: 16px; font-weight: 700; color: var(--jd-red); }
</style>
