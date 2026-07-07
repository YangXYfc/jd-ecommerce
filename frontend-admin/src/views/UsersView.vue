<template>
  <div class="users-view">
    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索用户名/手机号" style="width: 250px" :prefix-icon="Search" clearable />
      <el-select v-model="filterStatus" placeholder="状态筛选" style="width: 120px; margin-left: 12px">
        <el-option label="全部" value="" />
        <el-option label="正常" value="active" />
        <el-option label="已禁用" value="disabled" />
      </el-select>
    </div>

    <el-table :data="filteredUsers" stripe style="width: 100%; margin-top: 16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="100" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="orderCount" label="订单数" width="80" />
      <el-table-column prop="totalSpent" label="消费总额" width="100">
        <template #default="{ row }">¥{{ row.totalSpent }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
            {{ row.status === 'active' ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :type="row.status === 'active' ? 'danger' : 'success'" link @click="toggleStatus(row)">
            {{ row.status === 'active' ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" link @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      :page-size="10"
      :total="filteredUsers.length"
      layout="prev, pager, next, total"
      style="margin-top: 16px; justify-content: flex-end"
    />

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="500px">
      <el-descriptions :column="1" border v-if="currentUser">
        <el-descriptions-item label="ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUser.status === 'active' ? 'success' : 'danger'" size="small">
            {{ currentUser.status === 'active' ? '正常' : '已禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单数">{{ currentUser.orderCount }}</el-descriptions-item>
        <el-descriptions-item label="消费总额">¥{{ currentUser.totalSpent }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentUser.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { mockUsers } from '@/utils/mock-data'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([...mockUsers])
const searchKeyword = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const detailVisible = ref(false)
const currentUser = ref(null)

const filteredUsers = computed(() => {
  let list = users.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(u => u.username.toLowerCase().includes(kw) || u.phone.includes(kw))
  }
  if (filterStatus.value) {
    list = list.filter(u => u.status === filterStatus.value)
  }
  return list
})

function viewDetail(row) {
  currentUser.value = row
  detailVisible.value = true
}

async function toggleStatus(row) {
  const action = row.status === 'active' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}用户 ${row.username}？`, '提示', { type: 'warning' })
    row.status = row.status === 'active' ? 'disabled' : 'active'
    ElMessage.success(`${action}成功`)
  } catch (e) {}
}
</script>

<style scoped>
.toolbar { display: flex; align-items: center; }
</style>
