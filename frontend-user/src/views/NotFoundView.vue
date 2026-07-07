<template>
  <div class="not-found">
    <div class="nf-content">
      <h1 class="nf-code">404</h1>
      <p class="nf-text">页面走丢了...</p>
      <router-link to="/" class="nf-link">回到首页 →</router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found { min-height: 60vh; display: flex; align-items: center; justify-content: center; }
.nf-content { text-align: center; }
.nf-code { font-size: 120px; font-weight: 800; color: var(--jd-red); line-height: 1; }
.nf-text { font-size: 20px; color: var(--text-secondary); margin-bottom: 24px; }
.nf-link { display: inline-block; padding: 10px 32px; background: var(--jd-red); color: #fff; border-radius: 9999px; font-size: 14px; font-weight: 600; }
</style>
