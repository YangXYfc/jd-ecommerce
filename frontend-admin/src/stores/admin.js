import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/admin'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const adminInfo = ref(JSON.parse(localStorage.getItem('admin_info') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => adminInfo.value?.nickname || '管理员')

  // 登录 - 调用真实后端 API，检查 role === ADMIN
  async function login(loginData) {
    const res = await loginApi(loginData)
    // 后端返回: {token, userId, username, nickname, role}
    if (res.role !== 'ADMIN') {
      throw new Error('非管理员账号，禁止登录')
    }
    token.value = res.token
    adminInfo.value = {
      id: res.userId,
      username: res.username,
      nickname: res.nickname,
      role: res.role
    }
    localStorage.setItem('admin_token', res.token)
    localStorage.setItem('admin_info', JSON.stringify(adminInfo.value))
    return adminInfo.value
  }

  function logout() {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
  }

  return { token, adminInfo, isLoggedIn, nickname, login, logout }
})
