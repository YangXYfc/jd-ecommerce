<template>
  <div class="auth-view">
    <div class="auth-bg"></div>
    <div class="auth-container">
      <div class="auth-card">
        <router-link to="/" class="auth-logo">
          <span class="logo-icon">🦞</span>
          <span class="logo-text">极东<em>商城</em></span>
        </router-link>
        <h2 class="auth-title">{{ isLogin ? '欢迎登录' : '注册新账号' }}</h2>

        <form @submit.prevent="handleSubmit" class="auth-form">
          <div class="form-group" v-if="!isLogin">
            <label>用户名</label>
            <input v-model="form.username" type="text" placeholder="请输入用户名" required />
          </div>
          <div class="form-group" v-if="!isLogin">
            <label>手机号</label>
            <input v-model="form.phone" type="tel" placeholder="请输入手机号" required />
          </div>
          <div class="form-group">
            <label>{{ isLogin ? '用户名 / 手机号' : '密码' }}</label>
            <input v-if="isLogin" v-model="form.username" type="text" placeholder="请输入用户名或手机号" required />
            <input v-else v-model="form.password" type="password" placeholder="请设置密码" required />
          </div>
          <div class="form-group" v-if="isLogin">
            <label>密码</label>
            <input v-model="form.password" type="password" placeholder="请输入密码" required />
          </div>

          <div v-if="isLogin" class="form-extra">
            <label class="checkbox"><input type="checkbox" v-model="rememberMe" /> 记住我</label>
            <a href="javascript:void(0)" class="forgot-link">忘记密码？</a>
          </div>

          <button type="submit" class="auth-submit" :disabled="submitting">
            {{ submitting ? '请稍候...' : (isLogin ? '登 录' : '注 册') }}
          </button>
        </form>

        <div class="auth-switch">
          <span v-if="isLogin">还没有账号？</span>
          <span v-else>已有账号？</span>
          <a href="javascript:void(0)" @click="isLogin = !isLogin" class="switch-link">
            {{ isLogin ? '立即注册' : '去登录' }}
          </a>
        </div>

        <div class="auth-terms">
          登录/注册即代表同意 <a href="javascript:void(0)">《用户协议》</a> 和 <a href="javascript:void(0)">《隐私政策》</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const submitting = ref(false)
const rememberMe = ref(false)

const form = ref({
  username: '',
  password: '',
  phone: ''
})

async function handleSubmit() {
  if (submitting.value) return
  submitting.value = true
  try {
    if (isLogin.value) {
      await userStore.login({ username: form.value.username, password: form.value.password })
      ElMessage.success('登录成功')
    } else {
      await userStore.register(form.value)
      ElMessage.success('注册成功')
    }
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    ElMessage.error(isLogin.value ? '登录失败，请检查账号密码' : '注册失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth-view { min-height: 100vh; display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden; }
.auth-bg { position: absolute; inset: 0; background: linear-gradient(135deg, #e1251b 0%, #f5af19 100%); }
.auth-bg::before { content: ''; position: absolute; inset: 0; background: radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%), radial-gradient(circle at 80% 20%, rgba(255,255,255,0.1) 0%, transparent 50%); }

.auth-container { position: relative; z-index: 1; width: 100%; max-width: 400px; padding: 20px; }
.auth-card { background: #fff; border-radius: 16px; padding: 40px 32px; box-shadow: 0 20px 60px rgba(0,0,0,0.15); }

.auth-logo { display: flex; align-items: center; gap: 8px; justify-content: center; margin-bottom: 24px; }
.logo-icon { font-size: 32px; }
.logo-text { font-size: 26px; font-weight: 800; color: var(--jd-red); }
.logo-text em { font-style: normal; color: var(--text-primary); }

.auth-title { text-align: center; font-size: 20px; font-weight: 700; color: var(--text-primary); margin-bottom: 24px; }

.auth-form { display: flex; flex-direction: column; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; color: var(--text-secondary); }
.form-group input { padding: 12px 16px; border: 1px solid var(--border-base); border-radius: 8px; font-size: 14px; transition: border-color var(--transition-fast); }
.form-group input:focus { border-color: var(--jd-red); }

.form-extra { display: flex; justify-content: space-between; align-items: center; font-size: 13px; }
.checkbox { display: flex; align-items: center; gap: 4px; color: var(--text-secondary); cursor: pointer; }
.checkbox input { accent-color: var(--jd-red); }
.forgot-link { color: var(--text-secondary); }
.forgot-link:hover { color: var(--jd-red); }

.auth-submit { padding: 14px; background: var(--jd-red-gradient); color: #fff; font-size: 16px; font-weight: 700; border-radius: 8px; margin-top: 8px; transition: opacity var(--transition-fast); }
.auth-submit:hover { opacity: 0.9; }
.auth-submit:disabled { opacity: 0.6; cursor: not-allowed; }

.auth-switch { text-align: center; margin-top: 20px; font-size: 14px; color: var(--text-secondary); }
.switch-link { color: var(--jd-red); font-weight: 600; }

.auth-terms { text-align: center; margin-top: 16px; font-size: 12px; color: var(--text-secondary); }
.auth-terms a { color: var(--text-secondary); text-decoration: underline; }
</style>
