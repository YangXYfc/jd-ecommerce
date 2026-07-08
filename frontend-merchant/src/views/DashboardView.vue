<template>
  <div class="dashboard-view" v-loading="loading">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div v-for="card in statCards" :key="card.title" class="stat-card" :style="{ background: card.bg }">
        <div class="stat-icon">{{ card.icon }}</div>
        <div class="stat-body">
          <p class="stat-value">{{ card.value }}</p>
          <p class="stat-title">{{ card.title }}</p>
          <p class="stat-sub">{{ card.sub }}</p>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="charts-grid">
      <div class="chart-card">
        <h3>📈 销售趋势</h3>
        <div ref="salesChartRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <h3>📦 订单状态分布</h3>
        <div ref="orderChartRef" class="chart-box"></div>
      </div>
    </div>

    <!-- 热销商品 -->
    <div class="chart-card full">
      <h3>🏆 热销商品 TOP5</h3>
      <el-table :data="dashboard.topProducts" stripe style="width: 100%">
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="sales" label="销量" width="120" />
        <el-table-column label="销售额" width="160">
          <template #default="{ row }">¥{{ Number(row.amount).toLocaleString() }}</template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 待处理事项 -->
    <div class="pending-section">
      <h3>⚠️ 待处理事项</h3>
      <div class="pending-grid">
        <div class="pending-item" @click="$router.push('/orders')">
          <span class="pending-icon">📦</span>
          <span class="pending-text">待发货订单</span>
          <span class="pending-badge">{{ dashboard.stats.pendingShipment }}</span>
        </div>
        <div class="pending-item" @click="$router.push('/refunds')">
          <span class="pending-icon">⚖️</span>
          <span class="pending-text">待处理退款</span>
          <span class="pending-badge">{{ dashboard.stats.pendingRefunds }}</span>
        </div>
        <div class="pending-item" @click="$router.push('/products')">
          <span class="pending-icon">⚠️</span>
          <span class="pending-text">库存预警</span>
          <span class="pending-badge">{{ dashboard.stats.lowStockCount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const dashboard = ref({
  stats: { todaySales: 0, todayOrders: 0, totalSales: 0, totalOrders: 0, pendingShipment: 0, pendingRefunds: 0, productCount: 0, lowStockCount: 0 },
  salesChart: { dates: [], values: [] },
  orderChart: { statuses: [], values: [] },
  topProducts: []
})
const salesChartRef = ref(null)
const orderChartRef = ref(null)
let charts = []

const statCards = reactive([
  { title: '今日销售额', value: '¥0', sub: '总销售额 ¥0', icon: '💰', bg: 'linear-gradient(135deg, #ff6a00, #ff9a44)' },
  { title: '今日订单', value: 0, sub: '总订单 0', icon: '📋', bg: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { title: '在售商品', value: 0, sub: '库存预警 0', icon: '📦', bg: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
  { title: '待发货', value: 0, sub: '待退款 0', icon: '🚚', bg: 'linear-gradient(135deg, #43e97b, #38f9d7)' }
])

function initCharts() {
  if (salesChartRef.value) {
    const salesChart = echarts.init(salesChartRef.value)
    salesChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dashboard.value.salesChart.dates },
      yAxis: { type: 'value', axisLabel: { formatter: '{value}' } },
      series: [{ data: dashboard.value.salesChart.values, type: 'line', smooth: true, areaStyle: { color: 'rgba(255,106,0,0.1)' }, lineStyle: { color: '#ff6a00', width: 3 }, itemStyle: { color: '#ff6a00' } }]
    })
    charts.push(salesChart)
  }

  if (orderChartRef.value) {
    const orderChart = echarts.init(orderChartRef.value)
    orderChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{ type: 'pie', radius: ['40%', '70%'], data: dashboard.value.orderChart.statuses.map((s, i) => ({ name: s, value: dashboard.value.orderChart.values[i] })), itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 } }]
    })
    charts.push(orderChart)
  }
}

async function fetchDashboard() {
  loading.value = true
  try {
    const data = await getDashboardStats()
    dashboard.value = {
      stats: {
        todaySales: data.stats?.todaySales ?? 0,
        todayOrders: data.stats?.todayOrders ?? 0,
        totalSales: data.stats?.totalSales ?? 0,
        totalOrders: data.stats?.totalOrders ?? 0,
        pendingShipment: data.stats?.pendingShipment ?? 0,
        pendingRefunds: data.stats?.pendingRefunds ?? 0,
        productCount: data.stats?.productCount ?? 0,
        lowStockCount: data.stats?.lowStockCount ?? 0
      },
      salesChart: data.salesChart || { dates: [], values: [] },
      orderChart: data.orderChart || { statuses: [], values: [] },
      topProducts: data.topProducts || []
    }
    // 更新统计卡片
    statCards[0].value = `¥${Number(dashboard.value.stats.todaySales).toLocaleString()}`
    statCards[0].sub = `总销售额 ¥${Number(dashboard.value.stats.totalSales).toLocaleString()}`
    statCards[1].value = dashboard.value.stats.todayOrders
    statCards[1].sub = `总订单 ${dashboard.value.stats.totalOrders}`
    statCards[2].value = dashboard.value.stats.productCount
    statCards[2].sub = `库存预警 ${dashboard.value.stats.lowStockCount}`
    statCards[3].value = dashboard.value.stats.pendingShipment
    statCards[3].sub = `待退款 ${dashboard.value.stats.pendingRefunds}`
    // 重新渲染图表
    charts.forEach(c => c.dispose())
    charts = []
    setTimeout(initCharts, 100)
  } catch (err) {
    if (err?.type !== 'NETWORK_ERROR') {
      ElMessage.error('获取仪表盘数据失败')
    }
  } finally {
    loading.value = false
  }
}

function handleResize() { charts.forEach(c => c.resize()) }

onMounted(() => {
  fetchDashboard()
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => { window.removeEventListener('resize', handleResize); charts.forEach(c => c.dispose()) })
</script>

<style scoped>
.dashboard-view { display: flex; flex-direction: column; gap: 24px; }

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card { border-radius: 12px; padding: 24px; display: flex; align-items: center; gap: 16px; color: #fff; box-shadow: var(--shadow-md); }
.stat-icon { font-size: 40px; }
.stat-value { font-size: 28px; font-weight: 800; }
.stat-title { font-size: 14px; opacity: 0.9; }
.stat-sub { font-size: 12px; opacity: 0.7; margin-top: 4px; }

.charts-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.chart-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: var(--shadow-sm); }
.chart-card.full { width: 100%; }
.chart-card h3 { font-size: 16px; font-weight: 700; margin-bottom: 16px; }
.chart-box { height: 300px; }

.pending-section { background: #fff; border-radius: 8px; padding: 20px; box-shadow: var(--shadow-sm); }
.pending-section h3 { font-size: 16px; font-weight: 700; margin-bottom: 16px; }
.pending-grid { display: flex; gap: 16px; }
.pending-item { display: flex; align-items: center; gap: 8px; padding: 12px 20px; background: #fff7e6; border-radius: 8px; cursor: pointer; transition: all 0.2s; border: 1px solid #ffe58f; }
.pending-item:hover { box-shadow: var(--shadow-md); }
.pending-icon { font-size: 20px; }
.pending-text { font-size: 14px; color: var(--merchant-text); }
.pending-badge { background: var(--merchant-danger); color: #fff; font-size: 12px; font-weight: 700; padding: 2px 8px; border-radius: 10px; }
</style>
