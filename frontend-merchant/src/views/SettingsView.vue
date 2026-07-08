<template>
  <div class="settings-view" v-loading="loading">
    <el-tabs v-model="activeTab">
      <!-- 店铺信息 -->
      <el-tab-pane label="店铺信息" name="shop">
        <el-form :model="shopForm" label-width="120px" style="max-width: 600px; margin-top: 16px">
          <el-form-item label="店铺名称">
            <el-input v-model="shopForm.shopName" />
          </el-form-item>
          <el-form-item label="联系人">
            <el-input v-model="shopForm.contactName" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="shopForm.contactPhone" />
          </el-form-item>
          <el-form-item label="联系邮箱">
            <el-input v-model="shopForm.contactEmail" />
          </el-form-item>
          <el-form-item label="店铺简介">
            <el-input v-model="shopForm.description" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="店铺状态">
            <el-tag :type="shopForm.status === 'approved' ? 'success' : 'warning'" size="small">
              {{ shopForm.status === 'approved' ? '正常营业' : '审核中' }}
            </el-tag>
          </el-form-item>
          <el-form-item label="入驻时间">{{ shopForm.createdAt }}</el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingShop" @click="saveShopInfo">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 店铺配置 -->
      <el-tab-pane label="运营配置" name="config">
        <el-form :model="configForm" label-width="140px" style="max-width: 600px; margin-top: 16px">
          <el-form-item label="包邮门槛">
            <el-input-number v-model="configForm.freeShippingThreshold" :min="0" />
            <span style="margin-left: 8px; color: #999">元</span>
          </el-form-item>
          <el-form-item label="默认运费">
            <el-input-number v-model="configForm.defaultShippingFee" :min="0" />
            <span style="margin-left: 8px; color: #999">元</span>
          </el-form-item>
          <el-form-item label="退货地址">
            <el-input v-model="configForm.returnAddress" />
          </el-form-item>
          <el-form-item label="自动回复语">
            <el-input v-model="configForm.autoReplyMessage" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="营业时间">
            <el-input v-model="configForm.businessHours" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingConfig" @click="saveConfig">保存配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMerchantInfo, updateMerchantInfo, getMerchantConfigs, updateMerchantConfigs } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const activeTab = ref('shop')
const loading = ref(false)
const savingShop = ref(false)
const savingConfig = ref(false)

const shopForm = ref({
  shopName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  description: '',
  status: '',
  createdAt: ''
})

const configForm = ref({
  freeShippingThreshold: 0,
  defaultShippingFee: 0,
  returnAddress: '',
  autoReplyMessage: '',
  businessHours: ''
})

async function fetchShopInfo() {
  loading.value = true
  try {
    const data = await getMerchantInfo()
    if (data) {
      shopForm.value = {
        shopName: data.shopName || '',
        contactName: data.contactName || '',
        contactPhone: data.contactPhone || '',
        contactEmail: data.contactEmail || '',
        description: data.description || '',
        status: data.status || '',
        createdAt: data.createdAt || ''
      }
    }
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('获取店铺信息失败')
    }
  } finally {
    loading.value = false
  }
}

async function fetchConfigs() {
  loading.value = true
  try {
    const data = await getMerchantConfigs()
    if (data) {
      configForm.value = {
        freeShippingThreshold: data.freeShippingThreshold ?? 0,
        defaultShippingFee: data.defaultShippingFee ?? 0,
        returnAddress: data.returnAddress || '',
        autoReplyMessage: data.autoReplyMessage || '',
        businessHours: data.businessHours || ''
      }
    }
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('获取店铺配置失败')
    }
  } finally {
    loading.value = false
  }
}

async function saveShopInfo() {
  savingShop.value = true
  try {
    await updateMerchantInfo({ ...shopForm.value })
    ElMessage.success('店铺信息已保存')
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('保存失败')
    }
  } finally {
    savingShop.value = false
  }
}

async function saveConfig() {
  savingConfig.value = true
  try {
    await updateMerchantConfigs({ ...configForm.value })
    ElMessage.success('运营配置已保存')
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('保存失败')
    }
  } finally {
    savingConfig.value = false
  }
}

onMounted(() => {
  fetchShopInfo()
  fetchConfigs()
})
</script>

<style scoped>
.settings-view { background: #fff; border-radius: 8px; padding: 20px; box-shadow: var(--shadow-sm); }
</style>
