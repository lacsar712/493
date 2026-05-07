<template>
  <div class="tcm-page-container">
    <!-- Header / Filter -->
    <div class="tcm-filter-header">
      <div class="filter-left">
        <div class="page-title">
          <el-icon class="title-icon"><Management /></el-icon>
          <span>患者管理</span>
        </div>
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索患者姓名..." 
          class="tcm-search-input"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
          @input="handleSearchInput"
        >
           <template #append>
             <el-button :icon="Search" @click="handleSearch" />
           </template>
        </el-input>
      </div>
      <div class="filter-right">
        <el-button type="primary" class="tcm-btn-primary" @click="handleAdd">
          <el-icon class="el-icon--left"><Plus /></el-icon>新增患者
        </el-button>
      </div>
    </div>

    <div class="tcm-divider-dashed"></div>

    <!-- Table -->
    <div class="tcm-table-container">
      <el-table 
        :data="patients" 
        style="width: 100%" 
        v-loading="loading"
        class="tcm-table"
        :header-cell-style="{ background: 'var(--tcm-primary-light)', color: 'var(--tcm-primary)' }"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="姓名" min-width="100">
          <template #default="scope">
            <span class="patient-name">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.gender === '男' || scope.row.gender === 'Male' ? '' : 'danger'" effect="plain" size="small">
              {{ scope.row.gender === 'Male' ? '男' : (scope.row.gender === 'Female' ? '女' : scope.row.gender) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" align="center" />
        <el-table-column prop="phone" label="电话" min-width="120" />
        <el-table-column prop="doctorName" label="主治医生姓名" align="center">
            <template #default="scope">
                {{ scope.row.doctorName || scope.row.mainDoctorId }}
            </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewProfile(scope.row.id)">
              <el-icon class="el-icon--left"><Document /></el-icon>档案
            </el-button>
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              <el-icon class="el-icon--left"><Edit /></el-icon>编辑
            </el-button>
            <el-popconfirm title="确定删除该患者吗？" @confirm="handleDelete(scope.row)">
              <template #reference>
                <el-button type="danger" size="small">
                  <el-icon class="el-icon--left"><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="tcm-pagination">
        <el-pagination 
          background 
          layout="prev, pager, next" 
          :total="total" 
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- Profile Dialog -->
    <el-dialog 
      v-model="profileVisible" 
      title="患者档案" 
      width="600px"
      class="tcm-dialog"
    >
      <template #header>
        <div class="dialog-header">
          <span class="dialog-title">患者档案详情</span>
        </div>
      </template>

      <div v-if="currentProfile" class="profile-content">
        <el-tabs v-model="activeTab" class="tcm-tabs">
          <!-- Basic Info Tab -->
          <el-tab-pane label="基础信息" name="basic">
             <div class="tab-content">
                <div class="info-grid">
                  <div class="info-item">
                    <span class="label">姓名：</span>
                    <span class="value">{{ currentProfile.patient.name }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">性别：</span>
                    <span class="value">{{ currentProfile.patient.gender }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">年龄：</span>
                    <span class="value">{{ currentProfile.patient.age }}岁</span>
                  </div>
                  <div class="info-item">
                    <span class="label">联系电话：</span>
                    <span class="value">{{ currentProfile.patient.phone }}</span>
                  </div>
                </div>
             </div>
          </el-tab-pane>

          <!-- Constitution Tab -->
          <el-tab-pane label="体质结果" name="tcm">
             <div class="tab-content">
               <div v-if="currentProfile.lastTcmResult" class="tcm-result-card">
                  <div class="result-header">
                    <span class="tcm-label">当前体质：</span>
                    <el-tag effect="dark" color="var(--tcm-primary)" style="border:none; font-size: 14px;">
                      {{ currentProfile.lastTcmResult.bodyType }}
                    </el-tag>
                  </div>
                  <p class="tcm-time">检测时间：{{ currentProfile.lastTcmResult.createTime }}</p>
               </div>
               <div v-else class="empty-state">
                 <el-empty description="暂无体质检测记录" :image-size="60" />
               </div>
             </div>
          </el-tab-pane>

          <!-- Health Record Tab -->
          <el-tab-pane label="健康记录" name="record">
             <div class="tab-content">
               <el-table :data="patientRecords" style="width: 100%" size="small" border>
                  <el-table-column prop="visitDate" label="就诊日期" width="110" />
                  <el-table-column prop="bodyType" label="体质" width="80" />
                  <el-table-column prop="complaint" label="主诉" show-overflow-tooltip />
                  <el-table-column prop="diagnosis" label="诊断" show-overflow-tooltip />
                  <el-table-column prop="suggestion" label="建议" show-overflow-tooltip />
               </el-table>
               <div v-if="patientRecords.length === 0" class="empty-state">
                 <el-empty description="暂无健康记录" :image-size="60" />
               </div>
             </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- Patient Edit/Add Dialog -->
    <el-dialog v-model="patientDialogVisible" :title="isEdit ? '编辑患者' : '新增患者'" width="500px">
        <el-form :model="patientForm" :rules="patientFormRules" label-width="80px" ref="patientFormRef">
            <el-form-item label="姓名" prop="name">
                <el-input v-model="patientForm.name" />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
                <el-select v-model="patientForm.gender">
                    <el-option label="男" value="男" />
                    <el-option label="女" value="女" />
                </el-select>
            </el-form-item>
            <el-form-item label="年龄" prop="age">
                <el-input v-model="patientForm.age" type="number" />
            </el-form-item>
            <el-form-item label="电话" prop="phone">
                <el-input v-model="patientForm.phone" />
            </el-form-item>
            <el-row :gutter="20">
                <el-col :span="12">
                    <el-form-item label="身高(cm)" prop="height">
                        <el-input v-model="patientForm.height" type="number" />
                    </el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item label="体重(kg)" prop="weight">
                        <el-input v-model="patientForm.weight" type="number" />
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item label="血压" prop="bloodPressure">
                <el-input v-model="patientForm.bloodPressure" placeholder="例如: 120/80" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="patientDialogVisible = false" :disabled="submitting">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="submitting">保存</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { Search, Document, Management, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const patients = ref([])
const loading = ref(false)
const submitting = ref(false)
const profileVisible = ref(false)
const patientDialogVisible = ref(false)
const currentProfile = ref(null)
const patientRecords = ref([])
const searchQuery = ref('')
const activeTab = ref('basic')
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const isEdit = ref(false)
let searchTimer = null

const patientForm = ref({
  id: null,
  name: '',
  gender: '男',
  age: '',
  phone: '',
  height: '',
  weight: '',
  bloodPressure: '',
  mainDoctorId: ''
})
const patientFormRef = ref()
const patientFormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 1, max: 50, message: '姓名长度不超过50', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [
    {
      validator: (rule, value, callback) => {
        if (value === '' || value === null || value === undefined) return callback()
        const num = Number(value)
        if (Number.isNaN(num) || num < 1 || num > 120) return callback(new Error('年龄范围 1-120'))
        callback()
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      pattern: /^[0-9\-+]{6,20}$/,
      message: '电话格式不正确',
      trigger: 'blur'
    }
  ],
  height: [
    {
      validator: (rule, value, callback) => {
        if (value === '' || value === null || value === undefined) return callback()
        const num = Number(value)
        if (Number.isNaN(num) || num < 0) return callback(new Error('身高需为非负数'))
        callback()
      },
      trigger: 'blur'
    }
  ],
  weight: [
    {
      validator: (rule, value, callback) => {
        if (value === '' || value === null || value === undefined) return callback()
        const num = Number(value)
        if (Number.isNaN(num) || num < 0) return callback(new Error('体重需为非负数'))
        callback()
      },
      trigger: 'blur'
    }
  ],
  bloodPressure: [
    {
      pattern: /^(\d{2,3})\/(\d{2,3})$/,
      message: '血压格式如 120/80',
      trigger: 'blur'
    }
  ]
}

const fetchPatients = async () => {
  loading.value = true
  try {
    const res = await request.get('/patients/page', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        name: searchQuery.value || undefined
      }
    })
    // Spring Page response structure: { content: [], totalElements: 0, ... }
    // Adjusted based on how 'request' interceptor handles response. 
    // Assuming request.get returns 'data' field of axios response, which is Result object.
    // If Result.success(page) is returned, then res is the Page object.
    if (res && res.content) {
        patients.value = res.content
        total.value = res.totalElements
    } else {
        patients.value = []
        total.value = 0
    }
  } catch (e) {
    console.error(e)
    patients.value = []
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchPatients()
}

const handleSearch = () => {
  currentPage.value = 1
  fetchPatients()
}

const handleSearchInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    handleSearch()
  }, 300)
}

const handleAdd = () => {
    isEdit.value = false
    patientForm.value = {
        id: null,
        name: '',
        gender: '男',
        age: '',
        phone: '',
        height: '',
        weight: '',
        bloodPressure: '',
        mainDoctorId: ''
    }
    patientDialogVisible.value = true
}

const handleEdit = (row) => {
    isEdit.value = true
    patientForm.value = { ...row }
    patientDialogVisible.value = true
}

const handleDelete = async (row) => {
    try {
        await request.delete(`/patients/${row.id}`)
        ElMessage.success('删除成功')
        // If current page is empty after delete, go to prev page
        if (patients.value.length === 1 && currentPage.value > 1) {
            currentPage.value--
        }
        fetchPatients()
    } catch (e) {
        // Error handled by interceptor
    }
}

const handleSubmit = async () => {
    submitting.value = true
    try {
        const ok = await patientFormRef.value.validate().catch(() => false)
        if (!ok) {
            submitting.value = false
            return
        }
        if (isEdit.value) {
            await request.put(`/patients/${patientForm.value.id}`, patientForm.value)
            ElMessage.success('更新成功')
            fetchPatients()
        } else {
            await request.post('/patients', patientForm.value)
            ElMessage.success('创建成功')
            currentPage.value = 1 // Reset to first page to see the new item
            fetchPatients()
        }
        patientDialogVisible.value = false
    } catch (e) {
        // Error handled by interceptor
    } finally {
        submitting.value = false
    }
}

const viewProfile = async (id) => {
    try {
      const res = await request.get(`/patients/${id}/profile`)
      currentProfile.value = res
      
      try {
          const records = await request.get('/admin/records', { params: { patientId: id } })
          patientRecords.value = records || []
      } catch (e) {
          patientRecords.value = []
      }

      activeTab.value = 'basic'
      profileVisible.value = true
    } catch (e) {
      console.error(e)
    }
}

const tableRowClassName = ({ row, rowIndex }) => {
  // Add hover effect logic if needed beyond CSS
  return ''
}

onMounted(fetchPatients)
</script>

<style scoped>
.tcm-page-container {
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
/* Ensure the text inside title never wraps */
.page-title span {
  white-space: nowrap;
}

.search-input {
  width: 240px;
}

.tcm-divider-dashed {
  height: 1px;
  background-image: linear-gradient(to right, var(--tcm-primary-light) 50%, transparent 50%);
  background-size: 10px 1px;
  background-repeat: repeat-x;
  margin: 0 0 24px 0;
}

.tcm-table-container {
  background: #fff;
  border-radius: 4px;
}

.patient-name {
  font-weight: 600;
  color: #333;
}

.tcm-pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.dialog-header {
  border-bottom: 1px solid var(--tcm-primary-light);
  padding-bottom: 15px;
  margin-bottom: -10px;
}

.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--tcm-primary);
}

/* Tabs Styles */
.tcm-tabs {
  min-height: 200px;
}

:deep(.el-tabs__item.is-active) {
  color: var(--tcm-primary);
}

:deep(.el-tabs__active-bar) {
  background-color: var(--tcm-primary);
}

:deep(.el-tabs__item:hover) {
  color: var(--tcm-primary-hover);
}

.tab-content {
  padding: 10px 0;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  color: #999;
  width: 80px;
}

.value {
  color: #333;
  font-weight: 500;
}

.tcm-result-card, .record-card {
  background-color: #F9F7F2; /* Rice paper color */
  padding: 20px;
  border-radius: 8px;
  border: 1px dashed #DCDCDC;
}

.result-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.tcm-label {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-right: 10px;
}

.tcm-time {
  color: #999;
  font-size: 12px;
}

.record-item {
  margin-bottom: 15px;
}

.record-item:last-child {
  margin-bottom: 0;
}

.text-content {
  margin-top: 5px;
  color: #666;
  line-height: 1.6;
}

.suggestion {
  color: var(--tcm-primary); /* Highlight suggestion */
}
</style>
