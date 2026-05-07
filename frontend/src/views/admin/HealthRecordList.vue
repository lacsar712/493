<template>
  <div class="tcm-page-container">
    <div class="tcm-filter-header">
      <div class="filter-left">
        <div class="page-title">
          <el-icon class="title-icon"><Document /></el-icon>
          <span>健康档案管理</span>
        </div>
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索诊断结果/患者..." 
          class="tcm-search-input"
          clearable
          @input="handleSearchInput"
        >
           <template #append>
             <el-button :icon="Search" />
           </template>
        </el-input>
      </div>
    </div>

    <div class="tcm-divider-dashed"></div>
    
    <div class="tcm-table-container">
      <el-table 
        :data="pagedRecords" 
        style="width: 100%" 
        v-loading="loading" 
        class="tcm-table"
        :header-cell-style="{ background: 'var(--tcm-primary-light)', color: 'var(--tcm-primary)' }"
      >
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column label="患者" min-width="100">
          <template #default="scope">
             <span class="highlight-text">{{ getPatientName(scope.row.patientId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="负责医生" min-width="100">
          <template #default="scope">
             <span>{{ getDoctorName(scope.row.doctorId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="visitDate" label="就诊日期" width="120" sortable />
        <el-table-column prop="bodyType" label="体质类型" width="100">
           <template #default="scope">
              <el-tag v-if="scope.row.bodyType" size="small">{{ scope.row.bodyType }}</el-tag>
              <span v-else>-</span>
           </template>
        </el-table-column>
        <el-table-column prop="diagnosis" label="诊断结果" min-width="150" show-overflow-tooltip />
        <el-table-column prop="suggestion" label="医嘱建议" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="260" align="center">
          <template #default="scope">
            <el-button type="info" size="small" @click="handleView(scope.row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- Pagination -->
      <div class="tcm-pagination">
        <el-pagination 
          background 
          layout="prev, pager, next" 
          :total="filteredRecords.length" 
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- View Dialog -->
    <el-dialog v-model="viewDialogVisible" title="档案详情" width="600px">
        <el-descriptions :column="1" border v-if="currentRecord">
            <el-descriptions-item label="患者姓名">{{ getPatientName(currentRecord.patientId) }}</el-descriptions-item>
            <el-descriptions-item label="负责医生">{{ getDoctorName(currentRecord.doctorId) }}</el-descriptions-item>
            <el-descriptions-item label="就诊日期">{{ currentRecord.visitDate }}</el-descriptions-item>
            <el-descriptions-item label="体质类型">
                <el-tag v-if="currentRecord.bodyType" size="small">{{ currentRecord.bodyType }}</el-tag>
                <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="主诉症状">{{ currentRecord.complaint || '无' }}</el-descriptions-item>
            <el-descriptions-item label="诊断结果">{{ currentRecord.diagnosis || '无' }}</el-descriptions-item>
            <el-descriptions-item label="医嘱建议">{{ currentRecord.suggestion || '无' }}</el-descriptions-item>
        </el-descriptions>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="viewDialogVisible = false">关闭</el-button>
            </span>
        </template>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" title="编辑档案" width="600px" :close-on-click-modal="false">
        <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px" v-if="currentRecord">
            <el-form-item label="患者">
                <el-input :model-value="getPatientName(currentRecord.patientId)" disabled />
            </el-form-item>
             <el-form-item label="医生">
                <el-input :model-value="getDoctorName(currentRecord.doctorId)" disabled />
            </el-form-item>
            <el-form-item label="就诊日期">
                <el-date-picker v-model="form.visitDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
            <el-form-item label="体质类型">
                <el-select v-model="form.bodyType" placeholder="请选择">
                    <el-option label="平和体质" value="平和体质" />
                    <el-option label="气虚体质" value="气虚体质" />
                    <el-option label="痰湿体质" value="痰湿体质" />
                </el-select>
            </el-form-item>
            <el-form-item label="主诉">
                <el-input v-model="form.complaint" type="textarea" :rows="2" maxlength="500" show-word-limit />
            </el-form-item>
            <el-form-item label="诊断">
                <el-input v-model="form.diagnosis" type="textarea" :rows="2" maxlength="500" show-word-limit />
            </el-form-item>
            <el-form-item label="建议">
                <el-input v-model="form.suggestion" type="textarea" :rows="3" maxlength="1000" show-word-limit />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialogVisible = false" :disabled="submitting">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="submitting">保存</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import request from '@/api/request'
import { Document, Search, Edit, Delete, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const records = ref([])
const patients = ref({})
const doctors = ref({})
const loading = ref(false)
const searchQuery = ref('')
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const submitting = ref(false)
const currentRecord = ref(null)

// Pagination
const currentPage = ref(1)
const pageSize = ref(10)
let searchTimer = null

const form = reactive({
    visitDate: '',
    bodyType: '',
    complaint: '',
    diagnosis: '',
    suggestion: ''
})
const formRef = ref()
const formRules = {
  bodyType: [{ min: 0, max: 50, message: '体质类型长度不超过50', trigger: 'change' }],
  complaint: [{ min: 0, max: 500, message: '主诉长度不超过500', trigger: 'blur' }],
  diagnosis: [{ min: 0, max: 500, message: '诊断长度不超过500', trigger: 'blur' }],
  suggestion: [{ min: 0, max: 1000, message: '建议长度不超过1000', trigger: 'blur' }]
}

const fetchData = async () => {
    loading.value = true
    try {
        const [recordRes, patientRes, userRes] = await Promise.all([
            request.get('/admin/records'),
            request.get('/patients'),
            request.get('/admin/users')
        ])
        
        records.value = recordRes || []
        
        // Map Patients
        if (patientRes) {
            patientRes.forEach(p => {
                patients.value[p.id] = p.name
            })
        }
        
        // Map Doctors (Users)
        if (userRes) {
            userRes.forEach(u => {
                doctors.value[u.id] = u.username 
            })
        }
    } finally {
        loading.value = false
    }
}

const getPatientName = (id) => patients.value[id] || `Unknown(${id})`
const getDoctorName = (id) => doctors.value[id] || `Unknown(${id})`

const filteredRecords = computed(() => {
    if (!searchQuery.value) return records.value
    const q = searchQuery.value.toLowerCase()
    return records.value.filter(r => 
        (r.diagnosis && r.diagnosis.toLowerCase().includes(q)) ||
        (getPatientName(r.patientId).includes(q))
    )
})

const handleSearchInput = () => {
    if (searchTimer) clearTimeout(searchTimer)
    searchTimer = setTimeout(() => {
        currentPage.value = 1
    }, 300)
}
const pagedRecords = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredRecords.value.slice(start, end)
})

const handleView = (row) => {
    currentRecord.value = row
    viewDialogVisible.value = true
}

const handleEdit = (row) => {
    currentRecord.value = row
    form.visitDate = row.visitDate
    form.bodyType = row.bodyType
    form.complaint = row.complaint
    form.diagnosis = row.diagnosis
    form.suggestion = row.suggestion
    dialogVisible.value = true
}

const handleDelete = (row) => {
    ElMessageBox.confirm('确定要删除该条健康记录吗？此操作不可恢复。', '提示', {
        type: 'warning'
    }).then(async () => {
        await request.delete(`/records/${row.id}`)
        ElMessage.success('删除成功')
        fetchData()
    })
}

const handleSubmit = async () => {
    submitting.value = true
    try {
        const ok = await formRef.value.validate().catch(() => false)
        if (!ok) {
            submitting.value = false
            return
        }
        if (!form.complaint && !form.diagnosis) {
            ElMessage.warning('请至少填写主诉或诊断信息')
            submitting.value = false
            return
        }
        await request.put(`/records/${currentRecord.value.id}`, form)
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchData()
    } finally {
        submitting.value = false
    }
}

const handlePageChange = (page) => {
    currentPage.value = page
}

onMounted(fetchData)
</script>

<style scoped>
.tcm-page-container {
    padding: 24px;
    background-color: #fff;
    min-height: calc(100vh - 60px);
}

.tcm-filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.filter-left {
    display: flex;
    align-items: center;
    gap: 24px;
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

.highlight-text {
    font-weight: 500;
    color: var(--tcm-text-primary);
}
.page-title span {
    white-space: nowrap;
}
</style>
