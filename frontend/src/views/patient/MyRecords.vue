<template>
  <div class="tcm-page-container">
    <div class="page-header">
       <div class="page-title">
          <el-icon class="title-icon"><Collection /></el-icon>
          <span>健康档案</span>
       </div>
    </div>
    
    <div class="tcm-divider-dashed"></div>

    <div class="records-timeline-container" v-if="records.length > 0">
        <el-timeline>
          <el-timeline-item
            v-for="(record, index) in records"
            :key="index"
            :timestamp="formatDate(record.visitDate)"
            placement="top"
            :type="index === 0 ? 'primary' : ''"
            :color="index === 0 ? 'var(--tcm-primary)' : '#ddd'"
            :hollow="index === 0"
          >
            <div class="tcm-record-card">
                <div class="record-card-spine"></div>
                <div class="record-content">
                    <div class="diagnosis-header">
                        <span class="diagnosis-label">诊断结果</span>
                        <h3 class="diagnosis-text">{{ record.diagnosis }}</h3>
                    </div>
                    
                    <div class="tcm-divider-mini"></div>
                    
                    <div class="record-detail-item">
                        <span class="detail-label"><el-icon><ChatDotRound /></el-icon> 主诉症状</span>
                        <p class="detail-text">{{ record.complaint }}</p>
                    </div>
                    
                    <div class="record-detail-item suggestion-box">
                        <span class="detail-label"><el-icon><FirstAidKit /></el-icon> 医嘱建议</span>
                        <p class="detail-text highlight">{{ record.suggestion }}</p>
                    </div>
                </div>
                <div class="card-corner-deco"></div>
            </div>
          </el-timeline-item>
        </el-timeline>
    </div>

    <div v-else class="empty-state">
        <el-empty description="暂无健康记录" :image-size="120">
            <template #image>
                <el-icon :size="60" color="var(--tcm-border)"><Notebook /></el-icon>
            </template>
        </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { Collection, ChatDotRound, FirstAidKit, Notebook } from '@element-plus/icons-vue'

const records = ref([])

const fetchRecords = async () => {
  try {
    const res = await request.get('/patient/records')
    records.value = res
  } catch (e) {
    // handled
  }
}

const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

onMounted(fetchRecords)
</script>

<style scoped>
.tcm-page-container {
    padding: 24px;
    background-color: #fff;
    min-height: calc(100vh - 60px);
}

.records-timeline-container {
    max-width: 800px;
    margin: 24px auto;
    padding: 0 20px;
}

/* TCM Record Card Styles */
.tcm-record-card {
    background: #F9F7F2; /* Rice paper background */
    border: 1px solid #E0Dcd0;
    border-radius: 4px;
    padding: 20px;
    position: relative;
    box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.05);
    margin-bottom: 24px;
    overflow: hidden;
}

.record-card-spine {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background-color: var(--tcm-primary-light);
    opacity: 0.5;
}

.record-content {
    padding-left: 12px;
}

.diagnosis-header {
    display: flex;
    align-items: baseline;
    gap: 12px;
    margin-bottom: 12px;
}

.diagnosis-label {
    font-size: 12px;
    color: #fff;
    background-color: var(--tcm-primary);
    padding: 2px 8px;
    border-radius: 4px;
}

.diagnosis-text {
    font-size: 18px;
    color: var(--tcm-text-primary);
    margin: 0;
    font-weight: bold;
}

.record-detail-item {
    margin-bottom: 12px;
}

.record-detail-item:last-child {
    margin-bottom: 0;
}

.detail-label {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: var(--tcm-text-secondary);
    margin-bottom: 4px;
}

.detail-text {
    font-size: 14px;
    color: var(--tcm-text-primary);
    line-height: 1.6;
    margin: 0;
    text-align: justify;
}

.suggestion-box {
    background-color: rgba(255, 255, 255, 0.5);
    padding: 12px;
    border-radius: 4px;
    border: 1px dashed var(--tcm-border);
    margin-top: 16px;
}

.detail-text.highlight {
    color: var(--tcm-primary);
    font-family: "KaiTi", serif;
}

.card-corner-deco {
    position: absolute;
    bottom: -10px;
    right: -10px;
    width: 40px;
    height: 40px;
    border: 1px solid var(--tcm-border);
    transform: rotate(45deg);
    opacity: 0.3;
}

.empty-state {
    padding: 60px 0;
}
</style>
