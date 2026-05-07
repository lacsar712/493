<template>
  <div class="tcm-page-container" v-loading="loading">
    <div class="page-header centered-header">
       <div class="page-title">
          <el-icon class="title-icon"><PartlyCloudy /></el-icon>
          <span class="calligraphy-font">体质辨识问卷</span>
          <el-icon class="title-icon"><PartlyCloudy /></el-icon>
       </div>
       <p class="page-subtitle">请根据您最近三个月的实际感受，如实回答以下问题</p>
    </div>

    <div class="questionnaire-content" v-if="!submitted">
        <el-form label-position="top">
            <transition-group name="list" tag="div">
                <div 
                  v-for="(q, index) in questions" 
                  :key="q.id" 
                  class="tcm-question-card" 
                  :class="{ error: showUnanswered && !answers[q.id] }"
                  :ref="el => setQuestionRef(q.id, el)"
                >
                    <div class="question-header">
                        <span class="question-number">Question {{ index + 1 }}</span>
                    </div>
                    <h3 class="question-text">{{ q.content }}</h3>
                    
                    <div class="tcm-options-group">
                        <div 
                            class="tcm-option-item" 
                            :class="{ active: answers[q.id] === 'A' }"
                            @click="answers[q.id] = 'A'"
                        >
                            <div class="option-marker"></div>
                            <span class="option-label">{{ q.optionA }}</span>
                        </div>
                        <div 
                            class="tcm-option-item" 
                            :class="{ active: answers[q.id] === 'B' }"
                            @click="answers[q.id] = 'B'"
                        >
                            <div class="option-marker"></div>
                            <span class="option-label">{{ q.optionB }}</span>
                        </div>
                        <div 
                            class="tcm-option-item" 
                            :class="{ active: answers[q.id] === 'C' }"
                            @click="answers[q.id] = 'C'"
                        >
                            <div class="option-marker"></div>
                            <span class="option-label">{{ q.optionC }}</span>
                        </div>
                    </div>
                </div>
            </transition-group>
            
            <div class="form-actions">
                <el-button 
                    type="primary" 
                    @click="handleSubmit" 
                    class="tcm-btn-primary submit-btn" 
                    :disabled="loading"
                >
                    提交问卷
                </el-button>
            </div>
        </el-form>
    </div>

    <div v-else class="result-container">
        <div class="tcm-result-card">
            <div class="result-header">
                <el-icon size="48" color="var(--tcm-primary)"><Trophy /></el-icon>
                <h2>测评完成</h2>
            </div>
            
            <div class="result-content">
                <p class="result-label">您的体质判定为</p>
                <div class="result-type">{{ result?.bodyType }}</div>
                
                <div class="tcm-divider-dashed"></div>
                
                <div class="result-suggestion">
                    <h3><el-icon><Reading /></el-icon> 调理建议</h3>
                    <p>建议您保持良好的作息习惯，饮食清淡，避免过度劳累。具体调理方案请咨询专业医师。</p>
                </div>
            </div>

            <div class="result-actions">
                <el-button type="primary" @click="$router.push('/patient/profile')" class="tcm-btn-primary">
                    返回个人档案
                </el-button>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'
import { PartlyCloudy, Trophy, Reading } from '@element-plus/icons-vue'

const questions = ref([])
const answers = ref({})
const loading = ref(false)
const submitted = ref(false)
const result = ref(null)
const showUnanswered = ref(false)
const questionRefs = ref({})

const setQuestionRef = (id, el) => {
  if (el) questionRefs.value[id] = el
}

const fetchQuestions = async () => {
  loading.value = true
  try {
    const res = await request.get('/tcm/questions')
    questions.value = res
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
    // Check if all answered
    const unansweredIds = questions.value.filter(q => !answers.value[q.id]).map(q => q.id)
    if (unansweredIds.length > 0) {
        showUnanswered.value = true
        ElMessage.warning(`您还有 ${unansweredIds.length} 道题未作答`)
        const first = unansweredIds[0]
        const el = questionRefs.value[first]
        if (el && typeof el.scrollIntoView === 'function') {
          el.scrollIntoView({ behavior: 'smooth', block: 'center' })
        }
        return
    }

    loading.value = true
    try {
        const res = await request.post('/tcm/submit', { answers: answers.value })
        result.value = res
        submitted.value = true
    } finally {
        loading.value = false
    }
}

onMounted(fetchQuestions)
</script>

<style scoped>
.tcm-page-container {
    padding: 24px;
    background-color: #F9F7F2; /* Rice paper color */
    min-height: calc(100vh - 60px);
}

.centered-header {
    text-align: center;
    margin-bottom: 32px;
}

.page-title {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    font-size: 28px;
    color: var(--tcm-primary);
    margin-bottom: 8px;
}

.calligraphy-font {
    font-family: "KaiTi", "STKaiti", serif; /* Fallback to standard serif */
    font-weight: bold;
}

.page-subtitle {
    color: var(--tcm-text-secondary);
    font-size: 14px;
}

.questionnaire-content {
    max-width: 800px;
    margin: 0 auto;
}

.tcm-question-card {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;
    border: 1px solid transparent;
}

.tcm-question-card.error {
    border-color: #f56c6c;
    background-color: #fff7f7;
}

.tcm-question-card:hover {
    box-shadow: 0 4px 16px rgba(42, 107, 103, 0.1);
    border-color: rgba(42, 107, 103, 0.2);
}

.question-header {
    margin-bottom: 12px;
}

.question-number {
    font-size: 12px;
    color: var(--tcm-primary-light);
    font-weight: bold;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.question-text {
    font-size: 18px;
    color: var(--tcm-text-primary);
    margin: 0 0 24px 0;
    line-height: 1.5;
}

/* Custom Option Styles */
.tcm-options-group {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.tcm-option-item {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    border: 1px solid #eee;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s;
    background-color: #fafafa;
}

.tcm-option-item:hover {
    border-color: var(--tcm-primary-light);
    background-color: #fff;
}

.tcm-option-item.active {
    border-color: var(--tcm-primary);
    background-color: var(--tcm-bg-light);
}

.option-marker {
    width: 18px;
    height: 18px;
    border-radius: 50%;
    border: 2px solid #ccc;
    margin-right: 12px;
    position: relative;
    transition: all 0.2s;
}

.tcm-option-item.active .option-marker {
    border-color: var(--tcm-primary);
    background-color: #fff;
}

.tcm-option-item.active .option-marker::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background-color: var(--tcm-primary);
}

.option-label {
    font-size: 15px;
    color: var(--tcm-text-primary);
}

.form-actions {
    display: flex;
    justify-content: center;
    margin-top: 40px;
    margin-bottom: 40px;
}

.submit-btn {
    width: 200px;
    height: 48px;
    font-size: 16px;
    letter-spacing: 2px;
}

/* Result Page Styles */
.result-container {
    max-width: 600px;
    margin: 40px auto;
}

.tcm-result-card {
    background: #fff;
    border-radius: 8px;
    padding: 40px;
    text-align: center;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    background-image: linear-gradient(to bottom, #fff 0%, #F9F7F2 100%);
    border: 1px solid #E0Dcd0;
}

.result-header {
    margin-bottom: 24px;
}

.result-header h2 {
    color: var(--tcm-text-primary);
    margin-top: 16px;
    font-family: "KaiTi", serif;
}

.result-label {
    color: var(--tcm-text-secondary);
    font-size: 14px;
    margin-bottom: 8px;
}

.result-type {
    font-size: 36px;
    color: var(--tcm-primary);
    font-weight: bold;
    margin-bottom: 24px;
    font-family: "KaiTi", serif;
}

.result-suggestion {
    text-align: left;
    background: rgba(255, 255, 255, 0.6);
    padding: 20px;
    border-radius: 4px;
    border: 1px dashed var(--tcm-border);
    margin-top: 24px;
}

.result-suggestion h3 {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--tcm-primary);
    margin-bottom: 12px;
    font-size: 16px;
}

.result-suggestion p {
    color: var(--tcm-text-secondary);
    line-height: 1.8;
    text-indent: 2em;
}

.result-actions {
    margin-top: 32px;
}

/* Animations */
.list-move,
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(30px);
}
</style>
