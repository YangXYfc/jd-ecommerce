<template>
  <div class="login-view">
    <div class="login-bg"></div>
    <div class="login-card">
      <div class="login-header">
        <span class="logo-icon">🦞</span>
        <h1>极东商城商家后台</h1>
      </div>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <el-input v-model="form.username" size="large" placeholder="商家账号" prefix-icon="User" />
        </div>
        <div class="form-group">
          <el-input v-model="form.password" type="password" size="large" placeholder="密码" prefix-icon="Lock" show-password />
        </div>
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
      <p class="login-tip">商家账号登录，仅限 MERCHANT 角色访问</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMerchantStore } from '@/stores/merchant'
import { ElMessage } from 'element-plus'

const router = useRouter()
const merchantStore = useMerchantStore()
const loading = ref(false)
const form = ref({ username: '', password: '' })

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await merchantStore.login(form.value)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-view { min-height: 100vh; display: flex; align-items: center; justify-content: center; position: relative; }
.login-bg { position: absolute; inset: 0; background: linear-gradient(135deg, #1f1d2e 0%, #3d2c1e 50%, #ff6a00 100%); }
.login-bg::before { content: ''; position: absolute; inset: 0; background: radial-gradient(circle at 30% 70%, rgba(255,106,0,0.15) 0%, transparent 50%); }
.login-card { position: relative; z-index: 1; width: 400px; background: #fff; border-radius: 12px; padding: 40px 32px; box-shadow: 0 20px 60px rgba(0,0,0,0.2); }
.login-header { text-align: center; margin-bottom: 32px; }
.logo-icon { font-size: 40px; }
.login-header h1 { font-size: 22px; font-weight: 700; color: var(--merchant-text); margin-top: 8px; }
.login-form { display: flex; flex-direction: column; gap: 16px; }
.form-group .el-input { width: 100%; }
.login-btn { padding: 14px; background: var(--merchant-primary); color: #fff; font-size: 16px; font-weight: 700; border-radius: 8px; margin-top: 8px; }
.login-btn:hover { background: var(--merchant-primary-dark); }
.login-btn:disabled { opacity: 0.6; }
.login-tip { text-align: center; margin-top: 16px; font-size: 12px; color: var(--merchant-text-secondary); }
</style>
