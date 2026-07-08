<template>
  <div class="products-view" v-loading="loading">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索商品名称" style="width: 250px" clearable @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-select v-model="filterStatus" placeholder="商品状态" style="width: 120px; margin-left: 12px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="在售" value="on_sale" />
        <el-option label="已下架" value="off_shelf" />
        <el-option label="草稿" value="draft" />
        <el-option label="审核中" value="under_review" />
      </el-select>
      <el-button type="primary" style="margin-left: 12px" @click="$router.push('/products/create')">
        <el-icon><Plus /></el-icon> 发布新商品
      </el-button>
    </div>

    <!-- 商品列表 -->
    <el-table :data="products" stripe style="width: 100%; margin-top: 16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="商品" width="240">
        <template #default="{ row }">
          <div class="product-cell">
            <div class="product-thumb" :style="{ background: row.mainImage }"></div>
            <span class="product-name text-ellipsis">{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80">
        <template #default="{ row }">
          <span :class="{ 'low-stock': row.stock < 50 }">{{ row.stock }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link @click="$router.push(`/products/${row.id}/edit`)">编辑</el-button>
          <el-button v-if="row.status === 'on_sale'" size="small" link @click="toggleStatus(row, 'off_shelf')">下架</el-button>
          <el-button v-if="row.status === 'off_shelf'" size="small" type="primary" link @click="toggleStatus(row, 'on_sale')">上架</el-button>
          <el-button size="small" link @click="openSkuDialog(row)">SKU</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="margin-top: 16px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchProducts"
        @current-change="fetchProducts"
      />
    </div>

    <!-- SKU管理弹窗 -->
    <el-dialog v-model="skuDialogVisible" title="SKU管理" width="700px">
      <div v-if="currentProduct" style="margin-bottom: 16px">
        <p><strong>商品：</strong>{{ currentProduct.name }}</p>
      </div>
      <el-table :data="skuList" stripe style="width: 100%" v-loading="skuLoading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="规格名称" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" link @click="editSku(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="deleteSku(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px">
        <el-button type="primary" size="small" @click="addSku">+ 新增SKU</el-button>
      </div>

      <!-- SKU编辑子弹窗 -->
      <el-dialog v-model="skuEditVisible" title="SKU编辑" width="400px" append-to-body>
        <el-form label-width="80px">
          <el-form-item label="规格名称"><el-input v-model="skuForm.name" placeholder="如：标准版/高配版" /></el-form-item>
          <el-form-item label="价格"><el-input-number v-model="skuForm.price" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="库存"><el-input-number v-model="skuForm.stock" :min="0" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="skuEditVisible = false">取消</el-button>
          <el-button type="primary" @click="saveSku">保存</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getProductList, updateProductStatus, deleteProduct, getProductSkus, createSku, updateSku, deleteSku as deleteSkuApi } from '@/api/merchant'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const products = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const skuDialogVisible = ref(false)
const skuLoading = ref(false)
const skuEditVisible = ref(false)
const currentProduct = ref(null)
const skuList = ref([])
const skuForm = ref({ name: '', price: 0, stock: 0 })
const editingSkuId = ref(null)

const statusMap = {
  on_sale: { label: '在售', type: 'success' },
  off_shelf: { label: '已下架', type: 'info' },
  draft: { label: '草稿', type: 'warning' },
  under_review: { label: '审核中', type: 'primary' }
}
function statusLabel(s) { return statusMap[s]?.label || s }
function statusType(s) { return statusMap[s]?.type || '' }

async function fetchProducts() {
  loading.value = true
  try {
    const res = await getProductList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value || undefined
    })
    products.value = res.list || res.items || res.records || []
    total.value = res.total || 0
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('获取商品列表失败')
    }
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchProducts()
}

async function toggleStatus(row, newStatus) {
  try {
    await updateProductStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(newStatus === 'on_sale' ? '已上架' : '已下架')
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('操作失败')
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除商品「${row.name}」？`, '删除确认', { type: 'warning' })
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    fetchProducts()
  } catch (e) {}
}

async function openSkuDialog(row) {
  currentProduct.value = row
  skuDialogVisible.value = true
  skuLoading.value = true
  try {
    const res = await getProductSkus(row.id)
    skuList.value = res || []
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('获取SKU列表失败')
    }
    skuList.value = []
  } finally {
    skuLoading.value = false
  }
}

function addSku() {
  editingSkuId.value = null
  skuForm.value = { name: '', price: currentProduct.value.price, stock: 0 }
  skuEditVisible.value = true
}

function editSku(row) {
  editingSkuId.value = row.id
  skuForm.value = { ...row }
  skuEditVisible.value = true
}

async function saveSku() {
  if (!skuForm.value.name) {
    ElMessage.warning('请输入规格名称')
    return
  }
  try {
    if (editingSkuId.value) {
      await updateSku(currentProduct.value.id, editingSkuId.value, { ...skuForm.value })
    } else {
      await createSku(currentProduct.value.id, { ...skuForm.value })
    }
    ElMessage.success('保存成功')
    skuEditVisible.value = false
    // 刷新SKU列表
    const res = await getProductSkus(currentProduct.value.id)
    skuList.value = res || []
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('保存失败')
    }
  }
}

async function deleteSku(row) {
  try {
    await ElMessageBox.confirm(`确认删除SKU「${row.name}」？`, '删除', { type: 'warning' })
    await deleteSkuApi(currentProduct.value.id, row.id)
    ElMessage.success('删除成功')
    // 刷新SKU列表
    const res = await getProductSkus(currentProduct.value.id)
    skuList.value = res || []
  } catch (e) {}
}

// 初始加载
fetchProducts()
</script>

<style scoped>
.toolbar { display: flex; align-items: center; }
.product-cell { display: flex; align-items: center; gap: 8px; }
.product-thumb { width: 40px; height: 40px; border-radius: 4px; flex-shrink: 0; }
.low-stock { color: var(--merchant-danger); font-weight: 600; }
</style>
