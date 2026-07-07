import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue'), meta: { title: '管理员登录' } },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/DashboardView.vue'), meta: { title: '仪表盘', icon: '📊' } },
      { path: 'users', name: 'Users', component: () => import('@/views/UsersView.vue'), meta: { title: '用户管理', icon: '👥' } },
      { path: 'merchants', name: 'Merchants', component: () => import('@/views/MerchantsView.vue'), meta: { title: '商家管理', icon: '🏪' } },
      { path: 'product-audit', name: 'ProductAudit', component: () => import('@/views/ProductAuditView.vue'), meta: { title: '商品审核', icon: '📦' } },
      { path: 'orders', name: 'Orders', component: () => import('@/views/OrdersView.vue'), meta: { title: '订单管理', icon: '📋' } },
      { path: 'refunds', name: 'Refunds', component: () => import('@/views/RefundsView.vue'), meta: { title: '退款仲裁', icon: '⚖️' } },
      { path: 'logs', name: 'Logs', component: () => import('@/views/LogsView.vue'), meta: { title: '操作日志', icon: '📝' } },
      { path: 'settings', name: 'Settings', component: () => import('@/views/SettingsView.vue'), meta: { title: '系统设置', icon: '⚙️' } },
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 极东管理后台` : '极东管理后台'
  const store = useAdminStore()
  if (to.path !== '/login' && !store.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
