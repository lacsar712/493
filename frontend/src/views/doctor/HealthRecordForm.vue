<template>
  <div class="tcm-page-container">
    <div class="page-header">
       <div class="header-left">
          <el-button @click="$router.back()" circle class="back-btn">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <div class="page-title">
             <el-icon class="title-icon"><EditPen /></el-icon>
             <span>新增健康记录</span>
          </div>
       </div>
    </div>
    
    <div class="tcm-divider-dashed"></div>

    <div class="record-layout">
       <!-- Left Side: Patient Info -->
       <div class="patient-info-card">
          <div class="card-header-tcm">
             <el-icon><User /></el-icon>
             <span>患者信息</span>
          </div>
          <div v-if="patientInfo" class="info-content">
             <div class="info-row">
               <span class="info-label">姓名</span>
               <span class="info-val highlight">{{ patientInfo.name }}</span>
             </div>
             <div class="info-row">
               <span class="info-label">性别</span>
               <span class="info-val">{{ patientInfo.gender }}</span>
             </div>
             <div class="info-row">
               <span class="info-label">年龄</span>
               <span class="info-val">{{ patientInfo.age }}岁</span>
             </div>
             <div class="info-row">
               <span class="info-label">电话</span>
               <span class="info-val">{{ patientInfo.phone }}</span>
             </div>
             
             <div class="tcm-divider-mini"></div>
             
             <div v-if="lastTcmResult" class="tcm-status">
               <span class="info-label">当前体质</span>
               <el-tag effect="dark" color="var(--tcm-primary)" class="tcm-tag-block">
                 {{ lastTcmResult.bodyType }}
               </el-tag>
             </div>
          </div>
          <div v-else v-loading="loadingInfo" class="loading-placeholder">
            正在加载患者信息...
          </div>
       </div>

       <!-- Right Side: Form -->
       <div class="form-card">
          <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px" label-position="right" class="tcm-form">
             <el-form-item label="就诊日期">
                <el-date-picker 
                  v-model="form.visitDate" 
                  type="date" 
                  placeholder="选择日期" 
                  style="width: 100%" 
                  :disabled-date="disabledDate"
                  value-format="YYYY-MM-DD"
                />
             </el-form-item>
             
             <el-form-item label="体质类型">
                <el-select v-model="form.bodyType" placeholder="请选择体质类型" style="width: 100%">
                   <el-option label="平和体质" value="平和体质" />
                   <el-option label="气虚体质" value="气虚体质" />
                   <el-option label="阳虚体质" value="阳虚体质" />
                   <el-option label="阴虚体质" value="阴虚体质" />
                   <el-option label="痰湿体质" value="痰湿体质" />
                   <el-option label="湿热体质" value="湿热体质" />
                   <el-option label="血瘀体质" value="血瘀体质" />
                   <el-option label="气郁体质" value="气郁体质" />
                   <el-option label="特禀体质" value="特禀体质" />
                </el-select>
             </el-form-item>

             <el-form-item label="主诉症状">
                <el-input 
                  v-model="form.complaint" 
                  type="textarea" 
                  :rows="3" 
                  placeholder="请输入患者主要症状描述..." 
                  maxlength="500"
                  show-word-limit
                />
             </el-form-item>
             
             <el-form-item label="辨证诊断">
                <el-input 
                  v-model="form.diagnosis" 
                  type="textarea" 
                  :rows="3" 
                  placeholder="请输入中医辨证结果..." 
                  maxlength="500"
                  show-word-limit
                />
             </el-form-item>
             
             <el-form-item label="调理建议">
                <div class="suggestion-wrapper">
                   <el-input 
                      v-model="form.suggestion" 
                      type="textarea" 
                      :rows="6" 
                      class="suggestion-input"
                      placeholder="请输入健康调理建议（留空则自动根据体质生成）..." 
                      maxlength="1000"
                      show-word-limit
                   />
                   <div class="suggestion-bg-hint">中医脉案建议</div>
                </div>
                <div class="form-tip">
                   <el-icon><InfoFilled /></el-icon> 若为自动填充的体质默认建议，可在此基础上修改
                </div>
             </el-form-item>
             
             <div class="form-actions">
                <el-button @click="$router.back()" class="tcm-btn-secondary" :disabled="submitting">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="submitting" class="tcm-btn-primary">保存记录</el-button>
             </div>
          </el-form>
       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/request'
import { ElMessage } from 'element-plus'
import { ArrowLeft, EditPen, User, InfoFilled } from '@element-plus/icons-vue'

const suggestionMap = {
  '平和体质': '饮食均衡，规律作息，保持心情愉悦。',
  '气虚体质': '多吃补气食物（如山药、大枣），避免过度劳累，适度运动。',
  '痰湿体质': '饮食清淡，少食肥甘厚味，加强运动出汗，祛除湿气。',
  '阳虚体质': '注意保暖，多吃温热食物（如羊肉、生姜），避免生冷。',
  '阴虚体质': '滋阴润燥，多吃银耳、百合，避免熬夜和辛辣食物。',
  '湿热体质': '清热利湿，饮食清淡，少饮酒，保持环境干燥。',
  '血瘀体质': '活血化瘀，适度运动，保持心情舒畅，避免久坐。',
  '气郁体质': '疏肝理气，多听音乐，参加社交活动，调节情绪。',
  '特禀体质': '避免过敏原，增强体质，注意季节变化防护。'
}

const route = useRoute()
const router = useRouter()
const patientId = route.params.patientId
const recordId = route.query.recordId // For editing

const patientInfo = ref(null)
const lastTcmResult = ref(null)
const loadingInfo = ref(false)
const submitting = ref(false)
const isEdit = ref(false)

const form = ref({
  id: null,
  patientId: Number(patientId),
  visitDate: new Date().toISOString().split('T')[0],
  bodyType: '',
  complaint: '',
  diagnosis: '',
  suggestion: ''
})
const formRef = ref()
const formRules = {
  bodyType: [
    { min: 0, max: 50, message: '体质类型长度不超过50', trigger: 'change' }
  ],
  complaint: [
    { min: 0, max: 500, message: '主诉长度不超过500', trigger: 'blur' }
  ],
  diagnosis: [
    { min: 0, max: 500, message: '诊断长度不超过500', trigger: 'blur' }
  ],
  suggestion: [
    { min: 0, max: 1000, message: '建议长度不超过1000', trigger: 'blur' }
  ]
}

watch(() => form.value.bodyType, (newVal) => {
  if (newVal && (!form.value.suggestion || form.value.suggestion.trim() === '')) {
     const sug = suggestionMap[newVal]
     if (sug) {
        form.value.suggestion = sug
     }
  }
})

const fetchPatientInfo = async () => {
  loadingInfo.value = true
  try {
    const res = await request.get(`/patients/${patientId}/profile`)
    patientInfo.value = res.patient
    lastTcmResult.value = res.lastTcmResult
    
    // If adding new, autofill body type
    if (!isEdit.value && res.lastTcmResult && res.lastTcmResult.bodyType) {
        form.value.bodyType = res.lastTcmResult.bodyType
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取患者信息失败')
  } finally {
    loadingInfo.value = false
  }
}

const fetchRecord = async () => {
    if (!recordId) return
    isEdit.value = true
    try {
        // Need an endpoint to get single record? Or we can just find it in the patient records list?
        // Usually better to fetch by ID. Backend doesn't have explicit GET /records/:id for general use maybe?
        // Let's check backend.
        // Backend HealthRecordController doesn't have @GetMapping("/{id}").
        // But Admin can view all, Doctor can view own patient's.
        // We need to implement Get Single Record or reuse list.
        // Wait, I can't add backend endpoint easily without checking controller again.
        // Controller has: listRecords(@RequestParam patientId).
        // It does NOT have getRecordById.
        // I should add getRecordById to backend or filter from list.
        // Since I can modify backend, I will add getRecordById.
        // For now, let's assume I will add GET /api/records/:id
        const res = await request.get(`/records/${recordId}`) // Assuming I will add this
        form.value = { ...res }
    } catch (e) {
        console.error(e)
        ElMessage.error('获取记录失败')
    }
}

const handleSubmit = async () => {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  if (!form.value.complaint && !form.value.diagnosis) {
    ElMessage.warning('请至少填写主诉或诊断信息')
    return
  }
  
  submitting.value = true
  try {
    if (isEdit.value) {
        await request.put(`/records/${form.value.id}`, form.value)
        ElMessage.success('更新成功')
    } else {
        await request.post('/doctor/records', form.value)
        ElMessage.success('保存成功')
    }
    router.back()
  } catch (e) {
    // handled
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
    await fetchPatientInfo()
    if (recordId) {
        await fetchRecord()
    }
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

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.back-btn {
  border: none;
  background: transparent;
  font-size: 18px;
  color: #666;
}
.back-btn:hover {
  color: var(--tcm-primary);
  background: var(--tcm-primary-light);
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
}

.tcm-divider-dashed {
  height: 1px;
  background-image: linear-gradient(to right, var(--tcm-primary-light) 50%, transparent 50%);
  background-size: 10px 1px;
  background-repeat: repeat-x;
  margin: 0 0 24px 0;
}

.record-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.patient-info-card {
  width: 280px;
  background: #F9F7F2;
  border-radius: 8px;
  border: 1px solid #EAEAEA;
  overflow: hidden;
}

.card-header-tcm {
  background: var(--tcm-primary-light);
  padding: 15px 20px;
  color: var(--tcm-primary);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-content {
  padding: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 14px;
}

.info-label {
  color: #999;
}

.info-val {
  color: #333;
  font-weight: 500;
}

.info-val.highlight {
  color: var(--tcm-primary);
  font-weight: bold;
  font-size: 16px;
}

.tcm-divider-mini {
  height: 1px;
  background: #E0E0E0;
  margin: 15px 0;
}

.tcm-tag-block {
  width: 100%;
  text-align: center;
  margin-top: 8px;
  border: none;
}

.form-card {
  flex: 1;
  padding: 0 20px;
}

.suggestion-wrapper {
  position: relative;
}

.suggestion-bg-hint {
  position: absolute;
  bottom: 10px;
  right: 15px;
  font-size: 12px;
  color: rgba(0,0,0,0.1);
  pointer-events: none;
  font-family: "Kaiti", "STKaiti", serif;
  font-size: 24px;
}

:deep(.suggestion-input .el-textarea__inner) {
  background-color: #F9F7F2;
  border-color: #DCDCDC;
}
:deep(.suggestion-input .el-textarea__inner:focus) {
  background-color: #fff;
  border-color: var(--tcm-primary);
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 5px;
}

.form-actions {
  margin-top: 40px;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.loading-placeholder {
  padding: 40px;
  text-align: center;
  color: #999;
}
</style>
