<template>
  <div class="tcm-page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon"><TrendCharts /></el-icon>
        <span>我的体质历史</span>
      </div>
    </div>

    <div class="tcm-divider-dashed"></div>

    <div class="history-content">
      <div class="history-list-section">
        <div class="list-header">
          <span class="list-label">历史测评记录</span>
          <span class="list-hint" v-if="records.length >= 2">（勾选最多3条记录进行对比）</span>
          <span class="list-hint warning" v-else-if="records.length > 0">（至少需要2条记录才能对比）</span>
        </div>

        <el-table
          :data="records"
          style="width: 100%"
          v-loading="loading"
          class="tcm-table"
          :header-cell-style="{ background: 'var(--tcm-primary-light)', color: 'var(--tcm-primary)' }"
          @selection-change="handleSelectionChange"
          :row-key="row => row.id"
          ref="tableRef"
        >
          <el-table-column type="selection" width="55" :selectable="() => records.length >= 2" />
          <el-table-column prop="id" label="序号" width="80" align="center" />
          <el-table-column prop="bodyType" label="体质类型" min-width="120">
            <template #default="scope">
              <el-tag effect="dark" color="var(--tcm-primary)" style="border:none;">{{ scope.row.bodyType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="测评时间" min-width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createdTime) }}
            </template>
          </el-table-column>
          <el-table-column label="体质得分" min-width="200">
            <template #default="scope">
              <div class="score-tags">
                <el-tag
                  v-for="(val, key) in parseScoreJson(scope.row.scoreJson)"
                  :key="key"
                  size="small"
                  effect="plain"
                  class="score-tag"
                >
                  {{ key }}: {{ val }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="list-footer">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage + 1"
            @current-change="handlePageChange"
          />
          <div class="compare-action">
            <el-button
              type="primary"
              class="tcm-btn-primary"
              :disabled="selectedRows.length < 2 || selectedRows.length > 3"
              @click="handleCompare"
            >
              <el-icon class="el-icon--left"><DataAnalysis /></el-icon>
              开始对比
            </el-button>
            <span class="compare-hint" v-if="selectedRows.length === 1">请至少选择2条记录</span>
            <span class="compare-hint" v-if="selectedRows.length > 3">最多选择3条记录</span>
            <span class="compare-hint" v-if="selectedRows.length >= 2 && selectedRows.length <= 3">
              已选{{ selectedRows.length }}条记录
            </span>
          </div>
        </div>
      </div>

      <div class="chart-section" v-if="showChart">
        <div class="chart-header">
          <span class="chart-title">体质得分雷达对比图</span>
        </div>
        <div ref="radarChartRef" class="chart-container"></div>
        <div class="chart-legend">
          <div
            v-for="(item, index) in selectedRows"
            :key="item.id"
            class="legend-item"
          >
            <span class="legend-dot" :style="{ backgroundColor: legendColors[index] }"></span>
            <span class="legend-text">{{ formatDateTime(item.createdTime) }} - {{ item.bodyType }}</span>
          </div>
        </div>
      </div>

      <div class="no-data-tip" v-if="records.length === 0 && !loading">
        <el-empty description="暂无体质测评记录，请先完成体质问卷" :image-size="100" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { TrendCharts, DataAnalysis } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/api/request'

const userStore = useUserStore()

const records = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)
const selectedRows = ref([])
const showChart = ref(false)
const tableRef = ref(null)
const radarChartRef = ref(null)
let radarChart = null

const legendColors = ['#2A6B67', '#E6A23C', '#F56C6C']

const DIMENSIONS = ['平和', '气虚', '痰湿', '阳虚', '阴虚', '血瘀', '湿热', '气郁', '特禀']

const fetchHistory = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    const res = await request.get('/tcm/history', { params })
    if (res) {
      records.value = res.content || []
      total.value = res.totalElements || 0
    }
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page - 1
  selectedRows.value = []
  showChart.value = false
  fetchHistory()
}

const handleSelectionChange = (selection) => {
  if (selection.length > 3) {
    const lastSelected = selection[selection.length - 1]
    selection.splice(selection.length - 2, 1)
    if (tableRef.value) {
      const excess = selection.find(
        row => !selectedRows.value.some(s => s.id === row.id)
      )
      if (excess) {
        tableRef.value.toggleRowSelection(excess, false)
      }
    }
    selectedRows.value = selection.slice(-3)
  } else {
    selectedRows.value = selection
  }
  showChart.value = false
}

const handleCompare = () => {
  if (selectedRows.value.length < 2) {
    ElMessage.warning('请至少选择2条记录进行对比')
    return
  }
  if (selectedRows.value.length > 3) {
    ElMessage.warning('最多选择3条记录进行对比')
    return
  }
  showChart.value = true
  nextTick(() => {
    renderRadarChart()
  })
}

const parseScoreJson = (scoreJson) => {
  if (!scoreJson) return {}
  try {
    const parsed = JSON.parse(scoreJson)
    const result = {}
    for (const dim of DIMENSIONS) {
      if (parsed[dim] !== undefined) {
        result[dim] = parsed[dim]
      }
    }
    return result
  } catch {
    return {}
  }
}

const getMaxScore = () => {
  let maxVal = 30
  for (const row of selectedRows.value) {
    const scores = parseScoreJson(row.scoreJson)
    for (const val of Object.values(scores)) {
      if (val > maxVal) maxVal = val
    }
  }
  return Math.ceil(maxVal / 10) * 10
}

const renderRadarChart = () => {
  if (!radarChartRef.value) return

  if (radarChart) {
    radarChart.dispose()
  }

  radarChart = echarts.init(radarChartRef.value)

  const indicator = DIMENSIONS.map(dim => ({
    name: dim,
    max: getMaxScore()
  }))

  const series = selectedRows.value.map((row, index) => {
    const scores = parseScoreJson(row.scoreJson)
    return {
      value: DIMENSIONS.map(dim => scores[dim] || 0),
      name: formatDateTime(row.createdTime),
      lineStyle: { color: legendColors[index], width: 2 },
      itemStyle: { color: legendColors[index] },
      areaStyle: { color: legendColors[index], opacity: 0.15 }
    }
  })

  radarChart.setOption({
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: indicator,
      shape: 'polygon',
      splitNumber: 5,
      axisName: {
        color: '#333',
        fontSize: 13,
        fontWeight: 'bold'
      },
      splitLine: {
        lineStyle: { color: '#ddd' }
      },
      splitArea: {
        areaStyle: {
          color: ['#F9F7F2', '#fff']
        }
      },
      axisLine: {
        lineStyle: { color: '#bbb' }
      }
    },
    series: [
      {
        type: 'radar',
        data: series
      }
    ]
  })
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}`
}

const resizeHandler = () => {
  radarChart && radarChart.resize()
}

watch(showChart, (val) => {
  if (val) {
    nextTick(() => {
      window.addEventListener('resize', resizeHandler)
    })
  } else {
    window.removeEventListener('resize', resizeHandler)
  }
})

onMounted(fetchHistory)

onUnmounted(() => {
  window.removeEventListener('resize', resizeHandler)
  radarChart && radarChart.dispose()
})
</script>

<style scoped>
.tcm-page-container {
  padding: 24px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  min-height: calc(100vh - 100px);
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 600;
  color: var(--tcm-primary);
}

.title-icon {
  margin-right: 8px;
  font-size: 24px;
}

.tcm-divider-dashed {
  height: 1px;
  background-image: linear-gradient(to right, var(--tcm-primary-light) 50%, transparent 50%);
  background-size: 10px 1px;
  background-repeat: repeat-x;
  margin: 0 0 24px 0;
}

.list-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.list-label {
  font-size: 16px;
  font-weight: 600;
  color: var(--tcm-text-primary);
}

.list-hint {
  font-size: 13px;
  color: var(--tcm-text-secondary);
  margin-left: 8px;
}

.list-hint.warning {
  color: #E6A23C;
}

.score-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.score-tag {
  margin: 0;
}

.list-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.compare-action {
  display: flex;
  align-items: center;
  gap: 12px;
}

.compare-hint {
  font-size: 13px;
  color: var(--tcm-text-secondary);
}

.chart-section {
  margin-top: 32px;
  background: #F9F7F2;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #E0Dcd0;
}

.chart-header {
  margin-bottom: 16px;
  border-bottom: 1px dashed #ddd;
  padding-bottom: 12px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--tcm-primary);
}

.chart-container {
  height: 400px;
  width: 100%;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-text {
  font-size: 13px;
  color: var(--tcm-text-secondary);
}

.no-data-tip {
  padding: 60px 0;
}
</style>
