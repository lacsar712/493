<template>
  <div class="tcm-history-container">
    <div class="tcm-filter-header">
      <div class="filter-left">
        <div class="page-title">
          <el-icon class="title-icon"><TrendCharts /></el-icon>
          <span>体质历史对比</span>
        </div>
        <el-select
          v-model="selectedPatientId"
          placeholder="选择患者"
          class="patient-select"
          clearable
          @change="handlePatientChange"
        >
          <el-option
            v-for="patient in patients"
            :key="patient.id"
            :label="patient.name + ' - ' + (patient.gender || '') + ', ' + (patient.age || '') + '岁'"
            :value="patient.id"
          />
        </el-select>
      </div>
      <div class="filter-right">
        <el-tooltip :disabled="totalRecords >= 2" :content="totalRecords < 2 ? '历史记录不足2条，无法对比' : ''" placement="top">
          <el-button
            type="primary"
            :disabled="selectedRecords.length < 2 || totalRecords < 2"
            @click="handleCompare"
          >
            <el-icon><DataAnalysis /></el-icon>
            开始对比（{{ selectedRecords.length }}/3）
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <div class="tcm-divider-dashed"></div>

    <div v-if="!selectedPatientId" class="select-patient-tip">
      <el-alert type="info" :closable="false">
        请先选择要查看体质历史的患者。
      </el-alert>
    </div>

    <div v-else-if="totalRecords < 2 && totalRecords > 0" class="tip-info">
      <el-alert type="info" :closable="false">
        当前患者仅有 {{ totalRecords }} 条历史记录，至少需要 2 条才能进行对比分析。
      </el-alert>
    </div>

    <div v-if="selectedPatientId" class="content-wrapper">
      <div class="list-section">
        <div v-if="loading" class="loading-wrapper">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="records.length > 0" class="history-list">
          <el-table
            :data="records"
            @selection-change="handleSelectionChange"
            style="width: 100%"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="bodyType" label="体质类型" min-width="120">
              <template #default="{ row }">
                <el-tag :type="getBodyTypeTag(row.bodyType)" size="small">
                  {{ row.bodyType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="得分明细" min-width="300">
              <template #default="{ row }">
                <div class="score-detail">
                  <span
                    v-for="(score, dim) in parseScores(row.scoreJson)"
                    :key="dim"
                    class="score-tag"
                  >
                    <span class="dim-name">{{ dim }}</span>
                    <span class="dim-score">{{ score }}</span>
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="测评时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createdTime) }}
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              background
              layout="total, prev, pager, next, jumper"
              :page-size="pageSize"
              :total="totalRecords"
              :current-page="currentPage"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <div v-else class="empty-state">
          <el-empty description="该患者暂无体质测评记录" :image-size="120">
            <template #image>
              <el-icon :size="60" color="var(--tcm-border)"><Document /></el-icon>
            </template>
          </el-empty>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="compareDialogVisible"
      title="体质变化对比分析"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="compareLoading" class="chart-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div v-else>
        <div class="compare-legend">
          <div
            v-for="(item, index) in compareResults"
            :key="item.id"
            class="legend-item"
          >
            <span class="legend-dot" :style="{ background: chartColors[index] }"></span>
            <span class="legend-label">{{ formatDateTime(item.createdTime) }}</span>
            <span class="legend-bodytype">{{ item.bodyType }}</span>
          </div>
        </div>
        <div ref="radarChartRef" class="radar-chart"></div>
      </div>
      <template #footer>
        <el-button @click="compareDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import * as echarts from 'echarts'
import {
  TrendCharts,
  DataAnalysis,
  Document,
  Loading
} from '@element-plus/icons-vue'

const patients = ref([])
const selectedPatientId = ref(null)
const loading = ref(false)
const compareLoading = ref(false)
const records = ref([])
const selectedRecords = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalRecords = ref(0)
const compareDialogVisible = ref(false)
const compareResults = ref([])
const radarChartRef = ref(null)
let radarChart = null

const chartColors = ['#2A6B67', '#E67E22', '#9B59B6']

const nineDimensions = ['平和', '气虚', '阳虚', '阴虚', '痰湿', '湿热', '血瘀', '气郁', '特禀']

const fetchPatients = async () => {
  try {
    const res = await request.get('/patients/page', {
      params: {
        page: 1,
        size: 100
      }
    })
    patients.value = res.content || []
  } catch (e) {
    console.error(e)
  }
}

const fetchHistory = async () => {
  if (!selectedPatientId.value) {
    records.value = []
    totalRecords.value = 0
    return
  }

  loading.value = true
  try {
    const res = await request.get('/tcm/results/history', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        patientId: selectedPatientId.value
      }
    })
    records.value = res.content || []
    totalRecords.value = res.totalElements || 0
    if (currentPage.value > Math.ceil(totalRecords.value / pageSize.value) && totalRecords.value > 0) {
      currentPage.value = Math.ceil(totalRecords.value / pageSize.value)
      fetchHistory()
    }
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const handlePatientChange = () => {
  currentPage.value = 1
  selectedRecords.value = []
  fetchHistory()
}

const handleSelectionChange = (selection) => {
  if (selection.length > 3) {
    ElMessage.warning('最多只能选择 3 条记录进行对比')
    return
  }
  selectedRecords.value = selection
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchHistory()
}

const parseScores = (scoreJson) => {
  try {
    const scores = JSON.parse(scoreJson)
    const result = {}
    Object.keys(scores).forEach(key => {
      if (key !== 'Total') {
        result[key] = scores[key]
      }
    })
    return result
  } catch (e) {
    return {}
  }
}

const getBodyTypeTag = (bodyType) => {
  if (bodyType && bodyType.includes('平和')) return 'success'
  if (bodyType && bodyType.includes('气虚')) return 'warning'
  if (bodyType && bodyType.includes('痰湿')) return 'info'
  return ''
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const handleCompare = async () => {
  if (selectedRecords.value.length < 2) {
    ElMessage.warning('请至少选择 2 条记录进行对比')
    return
  }
  if (selectedRecords.value.length > 3) {
    ElMessage.warning('最多只能选择 3 条记录进行对比')
    return
  }

  compareLoading.value = true
  compareDialogVisible.value = true
  compareResults.value = []

  try {
    const promises = selectedRecords.value.map(record =>
      request.get(`/tcm/results/${record.id}`)
    )
    const results = await Promise.all(promises)
    compareResults.value = results.sort((a, b) => new Date(a.createdTime) - new Date(b.createdTime))

    await nextTick()
    initRadarChart()
  } catch (e) {
  } finally {
    compareLoading.value = false
  }
}

const getMaxScore = () => {
  let max = 20
  compareResults.value.forEach(result => {
    const scores = parseScores(result.scoreJson)
    Object.values(scores).forEach(score => {
      if (score > max) max = score
    })
  })
  return Math.ceil(max / 5) * 5
}

const initRadarChart = () => {
  if (!radarChartRef.value) return

  if (radarChart) {
    radarChart.dispose()
  }

  radarChart = echarts.init(radarChartRef.value)

  const maxScore = getMaxScore()
  const indicator = nineDimensions.map(dim => ({
    name: dim,
    max: maxScore
  }))

  const seriesData = compareResults.value.map((result, index) => {
    const scores = parseScores(result.scoreJson)
    const value = nineDimensions.map(dim => scores[dim] || 0)
    return {
      value,
      name: formatDateTime(result.createdTime),
      lineStyle: {
        width: 2
      },
      areaStyle: {
        opacity: 0.15
      },
      itemStyle: {
        color: chartColors[index]
      }
    }
  })

  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      show: false
    },
    radar: {
      indicator,
      shape: 'polygon',
      splitNumber: 5,
      axisName: {
        color: '#333',
        fontSize: 13,
        fontWeight: 500
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.1)'
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(42, 107, 103, 0.02)', 'rgba(42, 107, 103, 0.05)']
        }
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.15)'
        }
      }
    },
    series: [
      {
        type: 'radar',
        data: seriesData
      }
    ]
  }

  radarChart.setOption(option)
}

const resizeHandler = () => {
  radarChart && radarChart.resize()
}

onMounted(() => {
  fetchPatients()
  window.addEventListener('resize', resizeHandler)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeHandler)
  if (radarChart) {
    radarChart.dispose()
  }
})
</script>

<style scoped>
.tcm-history-container {
  padding: 24px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  min-height: calc(100vh - 100px);
}

.tcm-filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 600;
  color: var(--tcm-primary);
  white-space: nowrap;
  flex-shrink: 0;
}

.title-icon {
  margin-right: 8px;
  font-size: 24px;
}

.patient-select {
  width: 300px;
}

.select-patient-tip {
  margin-bottom: 16px;
}

.tip-info {
  margin-bottom: 16px;
}

.content-wrapper {
  margin-top: 20px;
}

.loading-wrapper {
  padding: 20px;
}

.history-list {
  background: #fff;
  border-radius: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.score-detail {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.score-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.dim-name {
  color: #666;
}

.dim-score {
  font-weight: 600;
  color: var(--tcm-primary);
}

.empty-state {
  padding: 60px 0;
}

.chart-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
  gap: 8px;
  color: #999;
}

.compare-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #eee;
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

.legend-label {
  font-size: 13px;
  color: #666;
}

.legend-bodytype {
  font-size: 13px;
  font-weight: 500;
  color: var(--tcm-text-primary);
}

.radar-chart {
  height: 450px;
  width: 100%;
}

.tcm-divider-dashed {
  height: 1px;
  background-image: linear-gradient(to right, var(--tcm-primary-light) 50%, transparent 50%);
  background-size: 10px 1px;
  background-repeat: repeat-x;
  margin: 0 0 24px 0;
}
</style>
