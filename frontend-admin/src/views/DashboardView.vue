<template>
  <div class="dashboard-view">
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
    <div class="chart-card full">
      <h3>🏷️ 分类销售占比</h3>
      <div ref="categoryChartRef" class="chart-box"></div>
    </div>

    <!-- 待处理事项 -->
    <div class="pending-section">
      <h3>⚠️ 待处理事项</h3>
      <div class="pending-grid">
        <div class="pending-item" @click="$router.push('/refunds')">
          <span class="pending-icon">⚖️</span>
          <span class="pending-text">待仲裁退款</span>
          <span class="pending-badge">{{ dashboard.stats.pendingRefunds }}</span>
        </div>
        <div class="pending-item" @click="$router.push('/product-audit')">
          <span class="pending-icon">📦</span>
          <span class="pending-text">待审核商品</span>
          <span class="pending-badge">{{ dashboard.stats.pendingAudits }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { mockDashboard } from '@/utils/mock-data'

const dashboard = ref(mockDashboard)
const salesChartRef = ref(null)
const orderChartRef = ref(null)
const categoryChartRef = ref(null)
let charts = []

const statCards = [
  { title: '总销售额', value: '¥1,286,432', sub: '今日 ¥32,567', icon: '💰', bg: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { title: '总订单数', value: '8,654', sub: '今日 234', icon: '📋', bg: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { title: '总用户数', value: '23,456', sub: '今日新增 56', icon: '👥', bg: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
  { title: '入驻商家', value: '128', sub: '待审核 3', icon: '🏪', bg: 'linear-gradient(135deg, #43e97b, #38f9d7)' }
]

function initCharts() {
  // 销售趋势
  const salesChart = echarts.init(salesChartRef.value)
  salesChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dashboard.value.salesChart.dates },
    yAxis: { type: 'value', axisLabel: { formatter: '{value}' } },
    series: [{ data: dashboard.value.salesChart.values, type: 'line', smooth: true, areaStyle: { color: 'rgba(24,144,255,0.1)' }, lineStyle: { color: '#1890ff', width: 3 }, itemStyle: { color: '#1890ff' } }]
  })
  charts.push(salesChart)

  // 订单状态
  const orderChart = echarts.init(orderChartRef.value)
  orderChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['40%', '70%'], data: dashboard.value.orderChart.statuses.map((s, i) => ({ name: s, value: dashboard.value.orderChart.values[i] })), itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 } }]
  })
  charts.push(orderChart)

  // 分类占比
  const catChart = echarts.init(categoryChartRef.value)
  catChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dashboard.value.categoryChart.names, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value' },
    series: [{ data: dashboard.value.categoryChart.values, type: 'bar', itemStyle: { color: '#1890ff', borderRadius: [4, 4, 0, 0] } }]
  })
  charts.push(catChart)
}

function handleResize() { charts.forEach(c => c.resize()) }

onMounted(() => { setTimeout(initCharts, 100); window.addEventListener('resize', handleResize) })
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
.pending-text { font-size: 14px; color: var(--admin-text); }
.pending-badge { background: var(--admin-danger); color: #fff; font-size: 12px; font-weight: 700; padding: 2px 8px; border-radius: 10px; }
</style>
