<template>
  <div class="address-view container">
    <el-breadcrumb separator=">" class="breadcrumb">
      <el-breadcrumb-item to="/profile">个人中心</el-breadcrumb-item>
      <el-breadcrumb-item>收货地址管理</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="address-header">
      <h1 class="page-title">📍 收货地址管理</h1>
      <button class="add-btn" @click="openDialog()">+ 新增收货地址</button>
    </div>

    <div v-loading="loading">
      <div v-for="addr in addresses" :key="addr.id" class="address-card">
        <div class="addr-main">
          <div class="addr-top">
            <span class="addr-name">{{ addr.name }}</span>
            <span class="addr-phone">{{ addr.phone }}</span>
            <span v-if="addr.isDefault" class="default-tag">默认</span>
            <span v-if="addr.label" class="label-tag">{{ addr.label }}</span>
          </div>
          <p class="addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
        </div>
        <div class="addr-actions">
          <button @click="openDialog(addr)">编辑</button>
          <button v-if="!addr.isDefault" @click="handleSetDefault(addr)">设为默认</button>
          <button class="danger" @click="handleDelete(addr)">删除</button>
        </div>
      </div>
      <div v-if="!loading && addresses.length === 0" class="empty">
        <p>暂无收货地址</p>
      </div>
    </div>

    <!-- 编辑/新增弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingAddr ? '编辑地址' : '新增地址'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="收货人" required>
          <el-input v-model="form.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" placeholder="省份" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" placeholder="城市" />
        </el-form-item>
        <el-form-item label="区/县">
          <el-input v-model="form.district" placeholder="区/县" />
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.label" placeholder="如：家、公司、学校" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="btn-save" @click="handleSave">保存</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/order'
import { mockAddresses } from '@/utils/mock-data'
import { ElMessage, ElMessageBox } from 'element-plus'

const addresses = ref(mockAddresses)
const loading = ref(false)
const dialogVisible = ref(false)
const editingAddr = ref(null)

const form = ref({
  name: '', phone: '', province: '', city: '', district: '', detail: '', label: '', isDefault: false
})

async function fetchAddresses() {
  loading.value = true
  try { addresses.value = await getAddressList() } catch {} finally { loading.value = false }
}

function openDialog(addr = null) {
  if (addr) {
    editingAddr.value = addr
    form.value = { ...addr }
  } else {
    editingAddr.value = null
    form.value = { name: '', phone: '', province: '', city: '', district: '', detail: '', label: '', isDefault: false }
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name || !form.value.phone || !form.value.detail) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    if (editingAddr.value) {
      await updateAddress(editingAddr.value.id, form.value)
      ElMessage.success('修改成功')
    } else {
      await addAddress(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchAddresses()
  } catch (e) { ElMessage.error('操作失败') }
}

async function handleDelete(addr) {
  try {
    await ElMessageBox.confirm('确认删除该地址？', '提示', { type: 'warning' })
    await deleteAddress(addr.id)
    ElMessage.success('删除成功')
    fetchAddresses()
  } catch (e) {}
}

async function handleSetDefault(addr) {
  try {
    await setDefaultAddress(addr.id)
    ElMessage.success('已设为默认')
    fetchAddresses()
  } catch (e) {}
}

onMounted(fetchAddresses)
</script>

<style scoped>
.address-view { padding: 16px 0 40px; }
.breadcrumb { margin-bottom: 16px; }
.address-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 24px; font-weight: 700; }
.add-btn { padding: 8px 20px; background: var(--jd-red); color: #fff; border-radius: var(--radius-md); font-size: 14px; font-weight: 600; }

.address-card { background: #fff; border-radius: var(--radius-md); padding: 16px 20px; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); }
.addr-top { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.addr-name { font-size: 16px; font-weight: 700; }
.addr-phone { font-size: 14px; color: var(--text-regular); }
.default-tag { font-size: 10px; background: var(--jd-red); color: #fff; padding: 1px 6px; border-radius: 2px; }
.label-tag { font-size: 10px; background: #f0f0f0; color: var(--text-secondary); padding: 1px 6px; border-radius: 2px; }
.addr-detail { font-size: 14px; color: var(--text-regular); }
.addr-actions { display: flex; gap: 8px; }
.addr-actions button { font-size: 13px; color: var(--text-secondary); background: none; padding: 4px 8px; }
.addr-actions button:hover { color: var(--jd-red); }
.addr-actions .danger:hover { color: var(--color-danger); }

.empty { text-align: center; padding: 60px; color: var(--text-secondary); }

.btn-cancel, .btn-save { padding: 8px 24px; border-radius: var(--radius-md); font-size: 14px; margin-left: 8px; }
.btn-cancel { border: 1px solid var(--border-base); background: #fff; color: var(--text-regular); }
.btn-save { background: var(--jd-red); color: #fff; border: none; }
</style>
