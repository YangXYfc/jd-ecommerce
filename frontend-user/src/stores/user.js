/**
 * 用户状态管理 - Pinia Store
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('jd_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('jd_user_info') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname || '游客')

  // 登录 - 后端返回 {token, userId, username, nickname, role}，映射为前端格式
  async function login(loginData) {
    const res = await loginApi(loginData)
    // res 是后端 LoginResponse: {token, userId, username, nickname, role}
    const mapped = {
      token: res.token,
      userInfo: {
        id: res.userId,
        username: res.username,
        nickname: res.nickname,
        role: res.role
      }
    }
    token.value = mapped.token
    userInfo.value = mapped.userInfo
    localStorage.setItem('jd_token', mapped.token)
    localStorage.setItem('jd_user_info', JSON.stringify(mapped.userInfo))
    return mapped
  }

  // 注册 - 同样映射后端响应
  async function register(registerData) {
    const res = await registerApi(registerData)
    const mapped = {
      token: res.token,
      userInfo: {
        id: res.userId,
        username: res.username,
        nickname: res.nickname,
        role: res.role
      }
    }
    token.value = mapped.token
    userInfo.value = mapped.userInfo
    localStorage.setItem('jd_token', mapped.token)
    localStorage.setItem('jd_user_info', JSON.stringify(mapped.userInfo))
    return mapped
  }

  // 获取用户信息
  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      userInfo.value = res
      localStorage.setItem('jd_user_info', JSON.stringify(res))
    } catch (e) {
      console.warn('获取用户信息失败', e)
    }
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('jd_token')
    localStorage.removeItem('jd_user_info')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    nickname,
    login,
    register,
    fetchUserInfo,
    logout
  }
})
