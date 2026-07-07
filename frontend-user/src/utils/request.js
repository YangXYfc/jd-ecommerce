/**
 * Axios 请求封装
 * 含 JWT 拦截器、统一响应处理、mock 数据降级
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import { isBackendUnavailableError } from '@/utils/request-error'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器 - 自动携带 JWT
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 - 统一处理
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    // token 过期 / 未认证
    if (res.code === 401) {
      ElMessage.error(res.message || '登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      return Promise.reject(new Error(res.message))
    }
    // 业务错误（如 403 禁用、400 参数错误等）- 传递错误，不降级
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  error => {
    // 网络错误时标记为 NETWORK_ERROR，允许上层选择性降级
    if (isBackendUnavailableError(error)) {
      console.warn('[API] 后端不可达')
      return Promise.reject({ type: 'NETWORK_ERROR', ...error })
    }
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service
