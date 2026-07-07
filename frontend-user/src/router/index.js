/**
 * Vue Router 路由配置
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/HomeView.vue'), meta: { title: '首页' } },
      { path: 'product-list', name: 'ProductList', component: () => import('@/views/ProductListView.vue'), meta: { title: '商品列表' } },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetailView.vue'), meta: { title: '商品详情' } },
      { path: 'cart', name: 'Cart', component: () => import('@/views/CartView.vue'), meta: { title: '购物车' } },
      { path: 'checkout', name: 'Checkout', component: () => import('@/views/CheckoutView.vue'), meta: { title: '结算', requiresAuth: true } },
      { path: 'orders', name: 'OrderList', component: () => import('@/views/OrderListView.vue'), meta: { title: '我的订单', requiresAuth: true } },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetailView.vue'), meta: { title: '订单详情', requiresAuth: true } },
      { path: 'refund/apply/:orderId', name: 'RefundApply', component: () => import('@/views/RefundApplyView.vue'), meta: { title: '申请退款', requiresAuth: true } },
      { path: 'refund/:id', name: 'RefundDetail', component: () => import('@/views/RefundDetailView.vue'), meta: { title: '退款详情', requiresAuth: true } },
      { path: 'review/:orderId', name: 'Review', component: () => import('@/views/ReviewView.vue'), meta: { title: '评价', requiresAuth: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/ProfileView.vue'), meta: { title: '个人中心', requiresAuth: true } },
      { path: 'profile/addresses', name: 'AddressManage', component: () => import('@/views/AddressManageView.vue'), meta: { title: '地址管理', requiresAuth: true } },
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: () => import('@/views/RegisterView.vue'), meta: { title: '注册' } },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFoundView.vue'), meta: { title: '页面不存在' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 极东商城` : '极东商城'
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }
  next()
})

export default router
