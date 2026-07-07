<template>
  <div class="merchants-view">
    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索店铺名称" style="width: 250px" clearable />
      <el-select v-model="filterStatus" placeholder="审核状态" style="width: 120px; margin-left: 12px">
        <el-option label="全部" value="" />
        <el-option label="已通过" value="approved" />
        <el-option label="待审核" value="pending" />
        <el-option label="已拒绝" value="rejected" />
      </el-select>
    </div>

    <el-table :data="filteredMerchants" stripe style="width: 100%; margin-top: 16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="shopName" label="店铺名称" width="180" />
      <el-table-column prop="contactName" label="联系人" width="100" />
      <el-table-column prop="contactPhone" label="联系电话" width="140" />
      <el-table-column prop="productCount" label="商品数" width="80" />
      <el-table-column prop="orderCount" label="订单数" width="80" />
      <el-table-column prop="auditStatus" label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.auditStatus === 'approved' ? 'success' : row.auditStatus === 'pending' ? 'warning' : 'danger'" size="small">
            {{ row.auditStatus === 'approved' ? '已通过' : row.auditStatus === 'pending' ? '待审核' : '已拒绝' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="入驻时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.auditStatus === 'pending'">
            <el-button size="small" type="success" @click="handleAudit(row, 'approved')">通过</el-button>
            <el-button size="small" type="danger" @click="handleAudit(row, 'rejected')">拒绝</el-button>
          </template>
          <el-button size="small" link @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="商家详情" width="500px">
      <el-descriptions :column="1" border v-if="currentMerchant">
        <el-descriptions-item label="ID">{{ currentMerchant.id }}</el-descriptions-item>
        <el-descriptions-item label="店铺名称">{{ currentMerchant.shopName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentMerchant.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMerchant.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="商品数">{{ currentMerchant.productCount }}</el-descriptions-item>
        <el-descriptions-item label="订单数">{{ currentMerchant.orderCount }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="currentMerchant.auditStatus === 'approved' ? 'success' : currentMerchant.auditStatus === 'pending' ? 'warning' : 'danger'" size="small">
            {{ currentMerchant.auditStatus === 'approved' ? '已通过' : currentMerchant.auditStatus === 'pending' ? '待审核' : '已拒绝' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入驻时间">{{ currentMerchant.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { mockMerchants } from '@/utils/mock-data'
import { ElMessage, ElMessageBox } from 'element-plus'

const merchants = ref([...mockMerchants])
const searchKeyword = ref('')
const filterStatus = ref('')
const detailVisible = ref(false)
const currentMerchant = ref(null)

const filteredMerchants = computed(() => {
  let list = merchants.value
  if (searchKeyword.value) {
    list = list.filter(m => m.shopName.includes(searchKeyword.value))
  }
  if (filterStatus.value) {
    list = list.filter(m => m.auditStatus === filterStatus.value)
  }
  return list
})

function viewDetail(row) {
  currentMerchant.value = row
  detailVisible.value = true
}

async function handleAudit(row, result) {
  const action = result === 'approved' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确认${action}商家 ${row.shopName} 的入驻申请？`, '审核', { type: 'warning' })
    row.auditStatus = result
    row.status = result
    ElMessage.success(`已${action}`)
  } catch (e) {}
}
</script>

<style scoped>
.toolbar { display: flex; align-items: center; }
</style>
