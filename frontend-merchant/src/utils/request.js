import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useMerchantStore } from '@/stores/merchant'
import router from '@/router'
import { isBackendUnavailableError } from '@/utils/request-error'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器 - 自动携带 JWT
service.interceptors.request.use(config => {
  const store = useMerchantStore()
  if (store.token) config.headers['Authorization'] = `Bearer ${store.token}`
  return config
}, err => Promise.reject(err))

// 响应拦截器 - 统一处理
service.interceptors.response.use(res => {
  const data = res.data
  if (data.code === 200) return data.data
  if (data.code === 401) {
    ElMessage.error('登录已过期')
    const store = useMerchantStore()
    store.logout()
    router.push('/login')
    return Promise.reject(new Error(data.message))
  }
  ElMessage.error(data.message || '请求失败')
  return Promise.reject(new Error(data.message))
}, error => {
  if (isBackendUnavailableError(error)) {
    return Promise.reject({ type: 'NETWORK_ERROR', ...error })
  }
  ElMessage.error(error.message || '网络异常')
  return Promise.reject(error)
})

export default service
