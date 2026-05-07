<template>
  <div class="tcm-page-container">
    <div class="tcm-filter-header">
      <div class="page-title">
        <el-icon class="title-icon"><Timer /></el-icon>
        <span>登录日志</span>
      </div>
    </div>

    <div class="tcm-divider-dashed"></div>

    <div class="tcm-table-container">
      <el-table 
        :data="pagedLogs" 
        style="width: 100%" 
        v-loading="loading" 
        class="tcm-table"
        :header-cell-style="{ background: 'var(--tcm-primary-light)', color: 'var(--tcm-primary)' }"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="role" label="角色" width="120">
            <template #default="scope">
                <el-tag :type="getRoleType(scope.row.role)">{{ formatRole(scope.row.role) }}</el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="loginTime" label="登录时间" min-width="180" sortable>
            <template #default="scope">
                {{ formatTime(scope.row.loginTime) }}
            </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" min-width="150" />
      </el-table>

      <div class="tcm-pagination">
        <el-pagination 
            background 
            layout="prev, pager, next" 
            :total="logs.length" 
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'
import { Timer } from '@element-plus/icons-vue'

const logs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 15

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get('/auth/logs')
        logs.value = res || []
    } finally {
        loading.value = false
    }
}

const pagedLogs = computed(() => {
    const start = (currentPage.value - 1) * pageSize
    const end = start + pageSize
    return logs.value.slice(start, end)
})

const handlePageChange = (page) => {
    currentPage.value = page
}

const getRoleType = (role) => {
    switch(role) {
        case 'ADMIN': return 'danger'
        case 'DOCTOR': return 'success'
        case 'PATIENT': return 'warning'
        default: return 'info'
    }
}

const formatRole = (role) => {
    switch(role) {
        case 'ADMIN': return '管理员'
        case 'DOCTOR': return '医生'
        case 'PATIENT': return '患者'
        default: return role
    }
}

const formatTime = (timeStr) => {
    if (!timeStr) return '-'
    return new Date(timeStr).toLocaleString()
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
    align-items: center;
    margin-bottom: 24px;
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
