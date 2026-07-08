import { createRouter, createWebHistory } from 'vue-router'
import { useMerchantStore } from '@/stores/merchant'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue'), meta: { title: '商家登录' } },
  {
    path: '/',
    component: () => import('@/layouts/MerchantLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/DashboardView.vue'), meta: { title: '店铺仪表盘', icon: '📊' } },
      { path: 'products', name: 'Products', component: () => import('@/views/ProductsView.vue'), meta: { title: '商品管理', icon: '📦' } },
      { path: 'products/create', name: 'ProductCreate', component: () => import('@/views/ProductEditView.vue'), meta: { title: '发布商品', icon: '📦' } },
      { path: 'products/:id/edit', name: 'ProductEdit', component: () => import('@/views/ProductEditView.vue'), meta: { title: '编辑商品', icon: '📦' } },
      { path: 'orders', name: 'Orders', component: () => import('@/views/OrdersView.vue'), meta: { title: '订单管理', icon: '📋' } },
      { path: 'orders/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetailView.vue'), meta: { title: '订单详情', icon: '📋' } },
      { path: 'refunds', name: 'Refunds', component: () => import('@/views/RefundsView.vue'), meta: { title: '退款处理', icon: '⚖️' } },
      { path: 'reviews', name: 'Reviews', component: () => import('@/views/ReviewsView.vue'), meta: { title: '评价管理', icon: '💬' } },
      { path: 'settings', name: 'Settings', component: () => import('@/views/SettingsView.vue'), meta: { title: '店铺设置', icon: '⚙️' } },
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 极东商家后台` : '极东商家后台'
  const store = useMerchantStore()
  if (to.path !== '/login' && !store.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
