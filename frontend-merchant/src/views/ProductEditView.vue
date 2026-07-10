<template>
  <div class="product-edit-view" v-loading="loading">
    <div class="page-header">
      <el-button link @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
      <h2>{{ isEdit ? '编辑商品' : '发布新商品' }}</h2>
    </div>

    <el-form :model="form" label-width="100px" style="max-width: 700px; margin-top: 20px">
      <el-form-item label="商品名称" required>
        <el-input v-model="form.name" placeholder="请输入商品名称" />
      </el-form-item>
      <el-form-item label="商品分类" required>
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="价格" required>
        <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 200px" />
        <span style="margin-left: 8px; color: #999">元</span>
      </el-form-item>
      <el-form-item label="库存" required>
        <el-input-number v-model="form.stock" :min="0" style="width: 200px" />
      </el-form-item>
      <el-form-item label="商品图片">
        <div class="image-upload-area">
          <div class="image-preview" :style="imageStyle(form.mainImage, Number(route.params.id) || 0)"></div>
          <el-button size="small">上传图片</el-button>
          <span style="margin-left: 8px; color: #999; font-size: 12px">建议尺寸 800×800px</span>
        </div>
      </el-form-item>
      <el-form-item label="商品描述">
        <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请输入商品描述" />
      </el-form-item>
      <el-form-item label="上架状态">
        <el-radio-group v-model="form.status">
          <el-radio label="on_sale">立即上架</el-radio>
          <el-radio label="draft">存为草稿</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ isEdit ? '保存修改' : '发布商品' }}</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, createProduct, updateProduct } from '@/api/merchant'
import { ElMessage } from 'element-plus'
import { imageStyle } from '@/utils/product-images'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const saving = ref(false)

const categories = ref([
  { id: 1, name: '手机数码' },
  { id: 2, name: '电脑办公' },
  { id: 3, name: '家用电器' },
  { id: 4, name: '服饰内衣' },
  { id: 5, name: '美妆护肤' },
  { id: 6, name: '食品生鲜' }
])

const form = ref({
  name: '',
  categoryId: null,
  price: 0,
  stock: 0,
  mainImage: '',
  description: '',
  status: 'on_sale'
})

onMounted(async () => {
  if (isEdit.value) {
    loading.value = true
    try {
      const product = await getProductDetail(route.params.id)
      if (product) {
        form.value = {
          name: product.name || '',
          categoryId: product.categoryId || null,
          price: product.price || 0,
          stock: product.stock || 0,
          mainImage: product.mainImage || '',
          description: product.description || '',
          status: product.status || 'on_sale'
        }
      }
    } catch (err) {
      if (err?.type !== 'NETWORK_ERROR') {
        ElMessage.error('获取商品详情失败')
      }
    } finally {
      loading.value = false
    }
  }
})

async function handleSave() {
  if (!form.value.name) {
    ElMessage.warning('请输入商品名称')
    return
  }
  if (!form.value.categoryId) {
    ElMessage.warning('请选择商品分类')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateProduct(route.params.id, { ...form.value })
    } else {
      await createProduct({ ...form.value })
    }
    ElMessage.success(isEdit.value ? '保存成功' : '发布成功')
    router.push('/products')
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error(isEdit.value ? '保存失败' : '发布失败')
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.product-edit-view { background: #fff; border-radius: 8px; padding: 24px; box-shadow: var(--shadow-sm); }
.page-header { display: flex; align-items: center; gap: 16px; }
.page-header h2 { font-size: 18px; font-weight: 700; }
.image-upload-area { display: flex; align-items: center; gap: 12px; }
.image-preview { width: 100px; height: 100px; border-radius: 8px; border: 1px dashed #ddd; }
</style>
