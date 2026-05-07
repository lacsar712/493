<template>
  <div class="tcm-page-container">
    <div class="tcm-filter-header">
      <div class="filter-left">
        <div class="page-title">
          <el-icon class="title-icon"><List /></el-icon>
          <span>体质问卷题库</span>
        </div>
      </div>
      <div class="filter-right">
        <el-button type="primary" class="tcm-btn-primary" @click="handleAdd">
          <el-icon class="el-icon--left"><Plus /></el-icon>新增题目
        </el-button>
      </div>
    </div>

    <div class="tcm-divider-dashed"></div>
    
    <div class="tcm-table-container">
      <el-table 
        :data="questions" 
        style="width: 100%" 
        v-loading="loading" 
        class="tcm-table"
        :header-cell-style="{ background: 'var(--tcm-primary-light)', color: 'var(--tcm-primary)' }"
        border
      >
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="content" label="题目内容" min-width="200" />
        <el-table-column label="选项A" min-width="120">
           <template #default="scope">
              {{ scope.row.optionA }} ({{ scope.row.optionAScore }}分)
           </template>
        </el-table-column>
        <el-table-column label="选项B" min-width="120">
           <template #default="scope">
              {{ scope.row.optionB }} ({{ scope.row.optionBScore }}分)
           </template>
        </el-table-column>
        <el-table-column label="选项C" min-width="120">
           <template #default="scope">
              {{ scope.row.optionC }} ({{ scope.row.optionCScore }}分)
           </template>
        </el-table-column>
        <el-table-column prop="dimension" label="所属维度" width="100" align="center">
           <template #default="scope">
              <el-tag effect="plain">{{ scope.row.dimension }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
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
    </div>

    <!-- Edit/Add Dialog -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑题目' : '新增题目'" 
      width="600px"
      :close-on-click-modal="false"
    >
        <el-form :model="form" :rules="formRules" label-width="80px" class="tcm-form" ref="formRef">
            <el-form-item label="题目内容" prop="content">
                <el-input v-model="form.content" type="textarea" :rows="2" placeholder="请输入问题描述" maxlength="255" show-word-limit />
            </el-form-item>
            <el-row :gutter="20">
                <el-col :span="16">
                    <el-form-item label="选项A" prop="optionA">
                        <el-input v-model="form.optionA" placeholder="选项描述" maxlength="100" show-word-limit />
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item label="分值" prop="optionAScore">
                        <el-input-number v-model="form.optionAScore" :min="0" :max="20" />
                    </el-form-item>
                </el-col>
            </el-row>
             <el-row :gutter="20">
                <el-col :span="16">
                    <el-form-item label="选项B" prop="optionB">
                        <el-input v-model="form.optionB" placeholder="选项描述" maxlength="100" show-word-limit />
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item label="分值" prop="optionBScore">
                        <el-input-number v-model="form.optionBScore" :min="0" :max="20" />
                    </el-form-item>
                </el-col>
            </el-row>
             <el-row :gutter="20">
                <el-col :span="16">
                    <el-form-item label="选项C" prop="optionC">
                        <el-input v-model="form.optionC" placeholder="选项描述" maxlength="100" show-word-limit />
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item label="分值" prop="optionCScore">
                        <el-input-number v-model="form.optionCScore" :min="0" :max="20" />
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item label="所属维度" prop="dimension">
                <el-select v-model="form.dimension" placeholder="请选择">
                    <el-option label="平和" value="平和" />
                    <el-option label="气虚" value="气虚" />
                    <el-option label="痰湿" value="痰湿" />
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialogVisible = false" :disabled="submitting">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="submitting">确定</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import request from '@/api/request'
import { List, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const questions = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)

const form = reactive({
    id: null,
    content: '',
    optionA: '',
    optionAScore: 0,
    optionB: '',
    optionBScore: 0,
    optionC: '',
    optionCScore: 0,
    dimension: '平和'
})
const formRef = ref()
const formRules = {
  content: [
    { required: true, message: '请输入题目内容', trigger: 'blur' },
    { min: 1, max: 255, message: '题目长度不超过255', trigger: 'blur' }
  ],
  optionA: [{ min: 0, max: 100, message: '选项A长度不超过100', trigger: 'blur' }],
  optionB: [{ min: 0, max: 100, message: '选项B长度不超过100', trigger: 'blur' }],
  optionC: [{ min: 0, max: 100, message: '选项C长度不超过100', trigger: 'blur' }],
  optionAScore: [
    {
      validator: (rule, value, callback) => {
        const v = Number(value)
        if (Number.isNaN(v) || v < 0 || v > 20) return callback(new Error('分值范围 0-20'))
        callback()
      },
      trigger: 'change'
    }
  ],
  optionBScore: [
    {
      validator: (rule, value, callback) => {
        const v = Number(value)
        if (Number.isNaN(v) || v < 0 || v > 20) return callback(new Error('分值范围 0-20'))
        callback()
      },
      trigger: 'change'
    }
  ],
  optionCScore: [
    {
      validator: (rule, value, callback) => {
        const v = Number(value)
        if (Number.isNaN(v) || v < 0 || v > 20) return callback(new Error('分值范围 0-20'))
        callback()
      },
      trigger: 'change'
    }
  ],
  dimension: [{ required: true, message: '请选择所属维度', trigger: 'change' }]
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

const handleAdd = () => {
    isEdit.value = false
    form.id = null
    form.content = ''
    form.optionA = '从不'
    form.optionAScore = 0
    form.optionB = '有时'
    form.optionBScore = 5
    form.optionC = '经常'
    form.optionCScore = 10
    form.dimension = '气虚'
    dialogVisible.value = true
}

const handleEdit = (row) => {
    isEdit.value = true
    Object.assign(form, row)
    dialogVisible.value = true
}

const handleDelete = (row) => {
    ElMessageBox.confirm('确定要删除该题目吗？此操作不可恢复。', '提示', {
        type: 'warning'
    }).then(async () => {
        await request.delete(`/tcm/questions/${row.id}`)
        ElMessage.success('删除成功')
        fetchQuestions()
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
        if (isEdit.value) {
            await request.put(`/tcm/questions/${form.id}`, form)
            ElMessage.success('更新成功')
        } else {
            await request.post('/tcm/questions', form)
            ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchQuestions()
    } finally {
        submitting.value = false
    }
}

onMounted(fetchQuestions)
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
</style>
