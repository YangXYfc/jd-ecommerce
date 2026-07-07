<template>
  <div class="logs-view">
    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索操作/管理员" style="width: 250px" clearable />
    </div>
    <el-table :data="filteredLogs" stripe style="width: 100%; margin-top: 16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="adminName" label="操作人" width="120" />
      <el-table-column prop="action" label="操作类型" width="140">
        <template #default="{ row }">
          <el-tag :type="actionType(row.action)" size="small">{{ row.action }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetName" label="操作对象" width="150" />
      <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="操作时间" width="160" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { mockAdminLogs } from '@/utils/mock-data'

const logs = ref([...mockAdminLogs])
const searchKeyword = ref('')

const filteredLogs = computed(() => {
  if (!searchKeyword.value) return logs.value
  const kw = searchKeyword.value.toLowerCase()
  return logs.value.filter(l => l.action.includes(kw) || l.adminName.includes(kw))
})

function actionType(action) {
  if (action.includes('通过')) return 'success'
  if (action.includes('拒绝') || action.includes('禁用')) return 'danger'
  if (action.includes('仲裁')) return 'warning'
  return 'info'
}
</script>

<style scoped>
.toolbar { display: flex; align-items: center; }
</style>
