<template>
  <div class="product-audit-view">
    <div class="toolbar">
      <el-radio-group v-model="filterStatus">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="pending">待审核</el-radio-button>
        <el-radio-button label="approved">已通过</el-radio-button>
        <el-radio-button label="rejected">已拒绝</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="filteredProducts" stripe style="width: 100%; margin-top: 16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="商品名称" width="200" />
      <el-table-column prop="merchantName" label="所属商家" width="180" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.auditStatus === 'approved' ? 'success' : row.auditStatus === 'pending' ? 'warning' : 'danger'" size="small">
            {{ row.auditStatus === 'approved' ? '已通过' : row.auditStatus === 'pending' ? '待审核' : '已拒绝' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submittedAt" label="提交时间" width="160" />
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

    <el-dialog v-model="detailVisible" title="商品详情" width="500px">
      <el-descriptions :column="1" border v-if="currentProduct">
        <el-descriptions-item label="ID">{{ currentProduct.id }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ currentProduct.name }}</el-descriptions-item>
        <el-descriptions-item label="所属商家">{{ currentProduct.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ currentProduct.price }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentProduct.description }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="currentProduct.auditStatus === 'approved' ? 'success' : currentProduct.auditStatus === 'pending' ? 'warning' : 'danger'" size="small">
            {{ currentProduct.auditStatus === 'approved' ? '已通过' : currentProduct.auditStatus === 'pending' ? '待审核' : '已拒绝' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentProduct.submittedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { mockProductsAudit } from '@/utils/mock-data'
import { ElMessage, ElMessageBox } from 'element-plus'

const products = ref([...mockProductsAudit])
const filterStatus = ref('')
const detailVisible = ref(false)
const currentProduct = ref(null)

const filteredProducts = computed(() => {
  if (!filterStatus.value) return products.value
  return products.value.filter(p => p.auditStatus === filterStatus.value)
})

function viewDetail(row) {
  currentProduct.value = row
  detailVisible.value = true
}

async function handleAudit(row, result) {
  const action = result === 'approved' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确认${action}商品 ${row.name}？`, '审核', { type: 'warning' })
    row.auditStatus = result
    ElMessage.success(`已${action}`)
  } catch (e) {}
}
</script>

<style scoped>
.toolbar { display: flex; align-items: center; }
</style>
