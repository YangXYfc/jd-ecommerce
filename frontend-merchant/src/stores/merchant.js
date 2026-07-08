import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getMerchantInfo } from '@/api/merchant'

export const useMerchantStore = defineStore('merchant', () => {
  const token = ref(localStorage.getItem('merchant_token') || '')
  const merchantInfo = ref(JSON.parse(localStorage.getItem('merchant_info') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => merchantInfo.value?.nickname || merchantInfo.value?.shopName || '商家')

  // 登录 - 调用后端 POST /auth/login，校验 role === MERCHANT
  async function login(loginData) {
    const res = await loginApi(loginData)
    // 后端返回: {token, userId, username, nickname, role}
    if (res.role !== 'MERCHANT') {
      throw new Error('非商家账号，禁止登录')
    }
    token.value = res.token
    merchantInfo.value = {
      id: res.userId,
      username: res.username,
      nickname: res.nickname,
      role: res.role
    }
    localStorage.setItem('merchant_token', res.token)
    localStorage.setItem('merchant_info', JSON.stringify(merchantInfo.value))
    return merchantInfo.value
  }

  // 获取店铺信息
  async function fetchMerchantInfo() {
    if (!token.value) return
    try {
      const res = await getMerchantInfo()
      merchantInfo.value = { ...merchantInfo.value, ...res }
      localStorage.setItem('merchant_info', JSON.stringify(merchantInfo.value))
    } catch (e) {
      console.warn('获取店铺信息失败', e)
    }
  }

  function logout() {
    token.value = ''
    merchantInfo.value = null
    localStorage.removeItem('merchant_token')
    localStorage.removeItem('merchant_info')
  }

  return { token, merchantInfo, isLoggedIn, nickname, login, fetchMerchantInfo, logout }
})
