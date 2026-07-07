<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-logo">
        <span class="logo-icon">🦞</span>
        <span v-show="!isCollapsed" class="logo-text">极东管理后台</span>
      </div>
      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems" :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: $route.path === item.path }"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span v-show="!isCollapsed" class="nav-label">{{ item.title }}</span>
        </router-link>
      </nav>
    </aside>

    <!-- 主区域 -->
    <div class="main-area">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <button class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <el-icon><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
          </button>
          <span class="breadcrumb">{{ $route.meta.title }}</span>
        </div>
        <div class="topbar-right">
          <el-dropdown @command="handleCommand">
            <span class="admin-info">
              <span class="admin-avatar" :style="{ background: adminStore.adminInfo?.avatar }"></span>
              <span>{{ adminStore.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const adminStore = useAdminStore()
const router = useRouter()
const isCollapsed = ref(false)

const menuItems = [
  { path: '/dashboard', title: '仪表盘', icon: '📊' },
  { path: '/users', title: '用户管理', icon: '👥' },
  { path: '/merchants', title: '商家管理', icon: '🏪' },
  { path: '/product-audit', title: '商品审核', icon: '📦' },
  { path: '/orders', title: '订单管理', icon: '📋' },
  { path: '/refunds', title: '退款仲裁', icon: '⚖️' },
  { path: '/logs', title: '操作日志', icon: '📝' },
  { path: '/settings', title: '系统设置', icon: '⚙️' }
]

function handleCommand(cmd) {
  if (cmd === 'logout') {
    adminStore.logout()
    router.push('/login')
  } else if (cmd === 'settings') {
    router.push('/settings')
  }
}
</script>

<style scoped>
.admin-layout { display: flex; height: 100vh; }

.sidebar { width: 220px; background: var(--admin-sidebar); display: flex; flex-direction: column; transition: width 0.3s; flex-shrink: 0; }
.sidebar.collapsed { width: 64px; }
.sidebar-logo { height: 64px; display: flex; align-items: center; justify-content: center; gap: 8px; color: #fff; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo-icon { font-size: 28px; }
.logo-text { font-size: 16px; font-weight: 700; white-space: nowrap; }
.sidebar-nav { flex: 1; padding: 8px 0; overflow-y: auto; }
.nav-item { display: flex; align-items: center; gap: 12px; padding: 12px 24px; color: rgba(255,255,255,0.65); transition: all 0.2s; }
.nav-item:hover { color: #fff; background: var(--admin-sidebar-hover); }
.nav-item.active { color: #fff; background: var(--admin-sidebar-hover); }
.nav-icon { font-size: 18px; }
.nav-label { font-size: 14px; white-space: nowrap; }

.main-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.topbar { height: 64px; background: #fff; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; box-shadow: var(--shadow-sm); z-index: 10; }
.topbar-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn { background: none; font-size: 18px; color: var(--admin-text); padding: 8px; }
.breadcrumb { font-size: 16px; font-weight: 600; }
.topbar-right { display: flex; align-items: center; }
.admin-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.admin-avatar { width: 32px; height: 32px; border-radius: 50%; }
.content-area { flex: 1; overflow-y: auto; padding: 24px; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
