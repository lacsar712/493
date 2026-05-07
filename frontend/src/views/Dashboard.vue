<template>
  <div class="dashboard-container">
    <!-- Welcome Card -->
    <div class="welcome-card">
      <div class="welcome-header">
        <h2>欢迎回来，{{ userStore.username }}</h2>
        <p>当前角色: {{ formatRole(userStore.role) }}</p>
      </div>
      <div class="action-buttons">
         <el-button type="primary" size="large" @click="handleQuickAction">
           <el-icon><Plus /></el-icon> 快速开始
         </el-button>
      </div>
    </div>

    <!-- Header Stats -->
    <div class="stats-row">
      <div class="stat-card" v-for="(stat, index) in stats" :key="index">
        <div class="stat-icon" :style="{ background: stat.bgColor }">
          <el-icon :color="stat.iconColor"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <h3>体质类型分布</h3>
        </div>
        <div ref="pieChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>本周就诊趋势</h3>
        </div>
        <div ref="lineChartRef" class="chart-container"></div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import { User, FirstAidKit, Document, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'

const userStore = useUserStore()
const router = useRouter()

const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null

const stats = ref([
  { label: '总患者数', value: '—', icon: User, bgColor: '#E8F5E9', iconColor: '#2E7D32' },
  { label: '今日就诊', value: '—', icon: FirstAidKit, bgColor: '#E3F2FD', iconColor: '#1565C0' },
  { label: '健康档案', value: '—', icon: Document, bgColor: '#FFF3E0', iconColor: '#EF6C00' },
  { label: '体质辨识', value: '—', icon: Document, bgColor: '#F3E5F5', iconColor: '#7B1FA2' },
])

const formatRole = (role) => {
    const map = {
        'ADMIN': '管理员',
        'DOCTOR': '医师',
        'PATIENT': '患者'
    }
    return map[role] || role
}

const handleQuickAction = () => {
    if (userStore.role === 'DOCTOR') router.push('/doctor/patients')
    else if (userStore.role === 'PATIENT') router.push('/patient/profile')
    else router.push('/admin/users')
}

const initCharts = () => {
  // Pie Chart - Constitution Types
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', left: 'center' },
      series: [
        {
          name: '体质类型',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: 20, fontWeight: 'bold' }
          },
          labelLine: { show: false },
          data: []
        }
      ]
    })
  }

  // Line Chart - Trends
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
    lineChart.setOption({
      grid: { top: 30, right: 20, bottom: 30, left: 40 },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        axisLine: { lineStyle: { color: '#999' } }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { type: 'dashed', color: '#eee' } }
      },
      series: [
        {
          data: [],
          type: 'line',
          smooth: true,
          symbolSize: 8,
          itemStyle: { color: '#2A6B67' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(42, 107, 103, 0.3)' },
              { offset: 1, color: 'rgba(42, 107, 103, 0.05)' }
            ])
          }
        }
      ]
    })
  }
}

const resizeHandler = () => {
  pieChart && pieChart.resize()
  lineChart && lineChart.resize()
}

const applyStatsToUI = (data) => {
  stats.value[0].value = String(data.totalPatients ?? '—')
  stats.value[1].value = String(data.todayVisits ?? '—')
  stats.value[2].value = String(data.healthRecords ?? '—')
  stats.value[3].value = String(data.tcmResults ?? '—')
  if (pieChart) {
    pieChart.setOption({
      series: [{ data: (data.bodyTypeDistribution || []).map(item => ({ name: item.name, value: item.value })) }]
    })
  }
  if (lineChart) {
    lineChart.setOption({
      xAxis: { data: data.weeklyLabels || ['周一','周二','周三','周四','周五','周六','周日'] },
      series: [{ data: data.weeklyVisits || [] }]
    })
  }
}

const fetchStats = async () => {
  try {
    const res = await request.get('/stats')
    applyStatsToUI(res || {})
  } catch (e) {
    // handled by interceptor
  }
}

onMounted(() => {
  initCharts()
  fetchStats()
  window.addEventListener('resize', resizeHandler)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeHandler)
  pieChart && pieChart.dispose()
  lineChart && lineChart.dispose()
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Stats Row */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--tcm-text-primary);
}

.stat-label {
  font-size: 14px;
  color: var(--tcm-text-secondary);
  margin-top: 4px;
}

/* Charts Row */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.chart-header {
  margin-bottom: 20px;
  border-bottom: 1px dashed #eee;
  padding-bottom: 12px;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--tcm-text-primary);
}

.chart-container {
  height: 300px;
  width: 100%;
}

/* Welcome Card */
.welcome-card {
  background: linear-gradient(135deg, var(--tcm-primary) 0%, #1e4d4a 100%);
  border-radius: 12px;
  padding: 32px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 24px rgba(42, 107, 103, 0.2);
}

.welcome-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
}

.welcome-header p {
  margin: 0;
  opacity: 0.9;
}

/* Responsive */
@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
