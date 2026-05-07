<template>
  <div class="tcm-page-container" v-loading="loading">
    <div class="page-header">
       <div class="page-title">
          <el-icon class="title-icon"><User /></el-icon>
          <span>个人档案</span>
       </div>
       <div class="filter-right">
        <el-button type="primary" @click="$router.push('/patient/questionnaire')" class="tcm-btn-secondary">
          <el-icon class="el-icon--left"><EditPen /></el-icon>体质辨识测评
        </el-button>
        <el-button v-if="canEdit && !editing" type="primary" @click="startEdit">编辑基础信息</el-button>
        <el-button v-if="editing" type="primary" @click="saveProfile" :disabled="loading">保存</el-button>
        <el-button v-if="editing" class="tcm-btn-secondary" @click="cancelEdit" :disabled="loading">取消</el-button>
      </div>
    </div>
    
    <div class="tcm-divider-dashed"></div>

    <div v-if="profile" class="profile-layout">
      <!-- Top: Basic Info -->
      <div class="info-section-card">
         <div class="info-avatar">
            <el-icon size="40"><UserFilled /></el-icon>
         </div>
         <div class="info-details">
            <div class="info-row main-row">
               <span class="name">{{ profile.patient.name }}</span>
               <el-tag effect="plain" round>{{ profile.patient.gender }}</el-tag>
               <span class="age">{{ profile.patient.age }}岁</span>
            </div>
            <div class="info-row sub-row">
               <el-icon><Phone /></el-icon>
               <template v-if="editing">
                 <el-input v-model="form.phone" placeholder="联系电话" style="max-width: 220px" />
               </template>
               <template v-else>
                 {{ profile.patient.phone }}
               </template>
            </div>
         </div>
         <div class="tcm-decoration-corner"></div>
      </div>

      <div class="cards-grid">
         <!-- Left: Constitution -->
         <div class="tcm-card constitution-card">
            <div class="card-header-tcm">
               <el-icon><Trophy /></el-icon>
               <span>我的体质</span>
            </div>
            <div class="card-content">
               <div v-if="profile.lastTcmResult" class="tcm-result-display">
                  <div class="tcm-type-badge">{{ profile.lastTcmResult.bodyType }}</div>
                  <p class="tcm-desc">
                    您的体质倾向于{{ profile.lastTcmResult.bodyType }}，建议根据医生指导进行调理。
                  </p>
                  <p class="tcm-date">测评时间: {{ formatTime(profile.lastTcmResult.createdTime) }}</p>
               </div>
               <div v-else class="empty-state">
                  <el-empty description="暂无体质测评记录" :image-size="80">
                    <el-button type="primary" @click="$router.push('/patient/questionnaire')">立即测评</el-button>
                  </el-empty>
               </div>
            </div>
         </div>

         <!-- Right: Health Record -->
         <div class="tcm-card health-card">
            <div class="card-header-tcm">
               <el-icon><FirstAidKit /></el-icon>
               <span>最近健康建议</span>
            </div>
            <div class="card-content">
               <div v-if="profile.lastHealthRecord" class="health-record-display">
                  <div class="record-block">
                     <span class="label">诊断结果</span>
                     <p class="text">{{ profile.lastHealthRecord.diagnosis }}</p>
                  </div>
                  <div class="record-block">
                     <span class="label">医嘱建议</span>
                     <p class="text highlight">{{ profile.lastHealthRecord.suggestion }}</p>
                  </div>
                  <div class="record-footer">
                     <span class="date">{{ profile.lastHealthRecord.visitDate }}</span>
                     <el-button type="primary" size="small" @click="$router.push('/patient/records')">查看更多 ></el-button>
                  </div>
               </div>
               <div v-else class="empty-state">
                  <el-empty description="暂无健康记录" :image-size="80" />
               </div>
            </div>
         </div>
         
         <div class="tcm-card metrics-card">
            <div class="card-header-tcm">
               <el-icon><FirstAidKit /></el-icon>
               <span>基础指标</span>
            </div>
            <div class="card-content">
               <div class="metrics-grid">
                  <div class="metric-item">
                     <span class="metric-label">身高</span>
                     <span class="metric-value" v-if="!editing">{{ profile.patient.height || '-' }} cm</span>
                     <el-input-number v-else v-model="form.height" :min="0" :max="300" controls-position="right" style="width: 200px" />
                  </div>
                  <div class="metric-item">
                     <span class="metric-label">体重</span>
                     <span class="metric-value" v-if="!editing">{{ profile.patient.weight || '-' }} kg</span>
                     <el-input-number v-else v-model="form.weight" :min="0" :max="500" controls-position="right" style="width: 200px" />
                  </div>
                  <div class="metric-item">
                     <span class="metric-label">血压</span>
                     <span class="metric-value" v-if="!editing">{{ profile.patient.bloodPressure || '-' }}</span>
                     <el-input v-else v-model="form.bloodPressure" placeholder="血压（如 120/80）" style="width: 220px" />
                  </div>
               </div>
            </div>
         </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/api/request'
import { User, EditPen, UserFilled, Phone, Trophy, FirstAidKit } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const profile = ref(null)
const loading = ref(false)
const editing = ref(false)
const form = ref({ phone: '', height: null, weight: null, bloodPressure: '' })
const userStore = useUserStore()
const canEdit = computed(() => userStore.role === 'PATIENT')

const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await request.get('/patient/profile')
    profile.value = res
    form.value.phone = res.patient?.phone || ''
    form.value.height = res.patient?.height ?? null
    form.value.weight = res.patient?.weight ?? null
    form.value.bloodPressure = res.patient?.bloodPressure || ''
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
    if (!time) return ''
    return new Date(time).toLocaleString()
}

onMounted(fetchProfile)

const startEdit = () => {
  editing.value = true
}
const cancelEdit = () => {
  editing.value = false
  if (profile.value) {
    form.value.phone = profile.value.patient?.phone || ''
    form.value.height = profile.value.patient?.height ?? null
    form.value.weight = profile.value.patient?.weight ?? null
    form.value.bloodPressure = profile.value.patient?.bloodPressure || ''
  }
}
const saveProfile = async () => {
  const phone = form.value.phone?.trim()
  const bp = form.value.bloodPressure?.trim()
  if (phone) {
    const ok = /^[0-9\-+]{6,20}$/.test(phone)
    if (!ok) {
      ElMessage.warning('电话格式不正确')
      return
    }
  }
  if (bp) {
    const ok = /^(\d{2,3})\/(\d{2,3})$/.test(bp)
    if (!ok) {
      ElMessage.warning('血压格式如 120/80')
      return
    }
  }
  loading.value = true
  try {
    await request.put('/patient/profile', {
      phone: phone || undefined,
      height: form.value.height ?? undefined,
      weight: form.value.weight ?? undefined,
      bloodPressure: bp || undefined
    })
    ElMessage.success('保存成功')
    editing.value = false
    await fetchProfile()
  } finally {
    loading.value = false
  }
}
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.profile-layout {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-section-card {
  background: linear-gradient(135deg, var(--tcm-primary-light) 0%, #fff 100%);
  padding: 30px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
  overflow: hidden;
  border: 1px solid #EAEAEA;
}

.info-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--tcm-primary);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.info-details {
  flex: 1;
}

.main-row {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.name {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.age {
  color: #666;
}

.sub-row {
  color: #666;
  display: flex;
  align-items: center;
  gap: 5px;
}

.tcm-decoration-corner {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, var(--tcm-primary) 10%, transparent 10.5%);
  background-size: 10px 10px;
  opacity: 0.1;
  transform: rotate(45deg);
}

.cards-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.tcm-card {
  background: #fff;
  border: 1px solid #EAEAEA;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.tcm-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  border-color: var(--tcm-primary-light);
}

.card-header-tcm {
  background: #F9F7F2;
  padding: 15px 20px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid #EAEAEA;
}

.card-content {
  padding: 28px 24px 32px;
  min-height: 200px;
}

.tcm-result-display {
  text-align: center;
}

.tcm-type-badge {
  display: inline-block;
  font-size: 24px;
  font-weight: bold;
  color: var(--tcm-primary);
  border: 2px solid var(--tcm-primary);
  padding: 10px 30px;
  border-radius: 50px;
  margin-bottom: 20px;
  font-family: "Kaiti", "STKaiti", serif;
}

.tcm-desc {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.6;
}

.tcm-date {
  color: #999;
  font-size: 12px;
}

.health-record-display {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.record-block .label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.record-block .text {
  color: #333;
  line-height: 1.6;
}

.record-block .text.highlight {
  color: var(--tcm-primary);
}

.record-footer {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px dashed #EAEAEA;
  padding-top: 15px;
}

.record-footer .date {
  color: #999;
  font-size: 12px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}
.metric-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 12px;
  background-color: #FAFAFA;
  border-radius: 6px;
  min-height: 72px;
}
.metric-label {
  color: #999;
  font-size: 12px;
  margin-bottom: 4px;
}
.metric-value {
  color: #333;
  font-weight: 600;
  display: block;
}
</style>
