/**
 * 用户相关 API
 */
import request from '@/utils/request'
import { mockUserInfo } from '@/utils/mock-data'

// 登录 - 不再 catch 降级，后端错误必须传递给用户
export function login(data) {
  return request.post('/auth/login', data)
}

// 注册 - 不再 catch 降级
export function register(data) {
  return request.post('/auth/register', data)
}

// 获取用户信息 - 仅在网络不可达时降级到 mock
export function getUserInfo() {
  return request.get('/user/info').catch(err => {
    if (err && err.type === 'NETWORK_ERROR') {
      console.warn('[API] 后端不可达，getUserInfo 使用 mock 降级')
      return mockUserInfo
    }
    return Promise.reject(err)
  })
}

// 更新用户信息 - 仅在网络不可达时降级
export function updateUserInfo(data) {
  return request.put('/user/info', data).catch(err => {
    if (err && err.type === 'NETWORK_ERROR') {
      console.warn('[API] 后端不可达，updateUserInfo 使用 mock 降级')
      return { success: true }
    }
    return Promise.reject(err)
  })
}
