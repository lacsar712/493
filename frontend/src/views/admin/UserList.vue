<template>
  <div class="tcm-page-container">
    <!-- Header / Filter Area -->
    <div class="tcm-filter-header">
      <div class="filter-left">
        <div class="page-title">
          <el-icon class="title-icon"><UserFilled /></el-icon>
          <span>账号管理</span>
        </div>
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索用户名..." 
          class="tcm-search-input"
          clearable
        >
           <template #append>
             <el-button :icon="Search" />
           </template>
        </el-input>
      </div>
      <div class="filter-right">
        <el-button type="primary" class="tcm-btn-primary" @click="handleAdd">
          <el-icon class="el-icon--left"><Plus /></el-icon>新增账号
        </el-button>
      </div>
    </div>

    <div class="tcm-divider-dashed"></div>
    
    <!-- Table Area -->
    <div class="tcm-table-container">
      <el-table 
        :data="filteredUsers" 
        style="width: 100%" 
        v-loading="loading" 
        class="tcm-table"
        :header-cell-style="{ background: 'var(--tcm-primary-light)', color: 'var(--tcm-primary)' }"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120">
          <template #default="scope">
            <span class="username-text">{{ scope.row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" min-width="100">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)" effect="light" class="tcm-tag">
              {{ formatRole(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180">
          <template #default="scope">
             <span class="time-text">{{ scope.row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- Pagination (Mockup for visual completeness) -->
      <div class="tcm-pagination">
        <el-pagination background layout="prev, pager, next" :total="users.length" />
      </div>
    </div>

    <!-- Dialog -->
    <el-dialog 
      v-model="dialogVisible" 
      title="新增账号" 
      width="500px"
      class="tcm-dialog"
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="dialog-header">
          <span class="dialog-title">新增账号</span>
        </div>
      </template>
      
      <el-form :model="form" :rules="formRules" label-width="80px" class="tcm-form" ref="formRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="医生" value="DOCTOR" />
            <el-option label="患者" value="PATIENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联患者" v-if="form.role === 'PATIENT'" prop="patientId">
            <el-select 
              v-model="form.patientId" 
              placeholder="请选择关联患者" 
              style="width: 100%"
              filterable
              clearable
            >
              <el-option 
                v-for="item in patients" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id" 
              >
                <span style="float: left">{{ item.name }}</span>
                <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">ID: {{ item.id }}</span>
              </el-option>
            </el-select>
        </el-form-item>
        <el-form-item label="关联ID" v-if="form.role === 'DOCTOR'">
            <el-input v-model="form.doctorId" placeholder="关联医生ID (选填)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" class="tcm-btn-secondary" :disabled="submitting">取消</el-button>
          <el-button type="primary" @click="submitUser" class="tcm-btn-primary" :loading="submitting" :disabled="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, UserFilled, Edit, Delete } from '@element-plus/icons-vue'

const users = ref([])
const submitting = ref(false)
const patients = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const searchQuery = ref('')
const isEdit = ref(false)
const form = ref({
  id: null,
  username: '',
  password: '',
  role: 'PATIENT',
  patientId: '',
  doctorId: ''
})
const formRef = ref()
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20', trigger: 'blur' }
  ],
  password: [
    {
      validator: (rule, value, callback) => {
        if (isEdit.value) return callback()
        if (!value || value.length < 6) return callback(new Error('密码至少 6 位'))
        callback()
      },
      trigger: 'blur'
    }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  patientId: [
    {
      validator: (rule, value, callback) => {
        if (form.value.role !== 'PATIENT') return callback()
        if (!value) return callback(new Error('请选择关联患者'))
        callback()
      },
      trigger: 'change'
    }
  ]
}

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  return users.value.filter(u => u.username.toLowerCase().includes(searchQuery.value.toLowerCase()))
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/users')
    users.value = res
  } finally {
    loading.value = false
  }
}

const fetchPatients = async () => {
  try {
    const res = await request.get('/patients')
    patients.value = res
  } catch (e) {
    console.error('Failed to fetch patients', e)
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    id: null,
    username: '',
    password: '',
    role: 'PATIENT',
    patientId: '',
    doctorId: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { ...row, password: '' } // Don't show password hash
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该账号吗？此操作不可恢复。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await request.delete(`/admin/users/${row.id}`)
        ElMessage.success('删除成功')
        fetchUsers()
      } catch (e) {
        // handled
      }
    })
    .catch(() => {})
}

const submitUser = async () => {
  submitting.value = true
  try {
    const ok = await formRef.value.validate().catch(() => false)
    if (!ok) {
      submitting.value = false
      return
    }
    if (isEdit.value) {
      await request.put(`/admin/users/${form.value.id}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/users/create', form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchUsers()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

const getRoleTagType = (role) => {
  const map = {
    'ADMIN': 'danger',
    'DOCTOR': 'success',
    'PATIENT': 'info'
  }
  return map[role] || 'info'
}

const formatRole = (role) => {
  const map = {
    'ADMIN': '管理员',
    'DOCTOR': '医生',
    'PATIENT': '患者'
  }
  return map[role] || role
}

onMounted(() => {
  fetchUsers()
  fetchPatients()
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
}

.title-icon {
  margin-right: 8px;
  font-size: 24px;
}

.page-title span {
  white-space: nowrap;
}

.tcm-search-input, .search-input {
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

.username-text {
  font-weight: 500;
  color: #333;
}

.time-text {
  color: #666;
  font-size: 13px;
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

/* Override Element UI specific styles within this component scope if needed */
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #E0E0E0 inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--tcm-primary-hover) inset !important;
}

:deep(.el-button--primary) {
  background-color: var(--tcm-primary);
  border-color: var(--tcm-primary);
}
:deep(.el-button--primary:hover) {
  background-color: var(--tcm-primary-hover);
  border-color: var(--tcm-primary-hover);
}
</style>
