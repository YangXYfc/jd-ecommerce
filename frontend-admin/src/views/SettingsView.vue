<template>
  <div class="settings-view">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="角色权限管理" name="roles">
        <el-table :data="roles" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="角色名称" width="150" />
          <el-table-column prop="desc" label="描述" />
          <el-table-column prop="userCount" label="用户数" width="100" />
          <el-table-column label="操作" width="150">
            <template #default>
              <el-button size="small" link>编辑权限</el-button>
              <el-button size="small" link>查看用户</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="系统配置" name="system">
        <el-form label-width="140px" style="max-width: 600px; margin-top: 16px">
          <el-form-item label="平台名称"><el-input v-model="systemConfig.platformName" /></el-form-item>
          <el-form-item label="客服电话"><el-input v-model="systemConfig.servicePhone" /></el-form-item>
          <el-form-item label="商家审核超时"><el-input v-model="systemConfig.merchantTimeout" /><span style="margin-left: 8px">小时</span></el-form-item>
          <el-form-item label="自动确认收货"><el-input v-model="systemConfig.autoConfirmDays" /><span style="margin-left: 8px">天</span></el-form-item>
          <el-form-item><el-button type="primary">保存配置</el-button></el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('roles')
const roles = ref([
  { id: 1, name: '超级管理员', desc: '拥有所有权限', userCount: 1 },
  { id: 2, name: '运营管理员', desc: '商品审核、订单管理', userCount: 3 },
  { id: 3, name: '客服管理员', desc: '退款仲裁、用户管理', userCount: 5 },
  { id: 4, name: '数据分析师', desc: '数据查看、报表导出', userCount: 2 }
])
const systemConfig = ref({
  platformName: '极东商城',
  servicePhone: '400-888-8888',
  merchantTimeout: 48,
  autoConfirmDays: 7
})
</script>

<style scoped>
.settings-view { background: #fff; border-radius: 8px; padding: 20px; box-shadow: var(--shadow-sm); }
</style>
