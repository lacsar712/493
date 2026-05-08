<template>
  <el-container class="layout-container">
    <el-aside width="240px" class="aside">
      <div class="logo-container">
        <div class="logo-icon">
           <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="24" height="24">
              <path d="M12 3C7.02944 3 3 7.02944 3 12C3 16.9706 7.02944 21 12 21C16.9706 21 21 16.9706 21 12C21 7.02944 16.9706 3 12 3ZM12 5C15.866 5 19 8.13401 19 12C19 15.866 15.866 19 12 19C8.13401 19 5 15.866 5 12C5 8.13401 8.13401 5 12 5Z" fill="currentColor" fill-opacity="0.2"/>
              <path d="M12 6C12 6 14 10 16 12C18 14 12 18 12 18C12 18 6 14 8 12C10 10 12 6 12 6Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
        </div>
        <span class="logo-text">中医患者管理</span>
      </div>
      <el-menu 
        router 
        :default-active="$route.path"
        class="el-menu-vertical"
        background-color="transparent"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#fff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>首页概览</span>
        </el-menu-item>

        <template v-if="userStore.role === 'ADMIN'">
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>账号管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/patients">
            <el-icon><Management /></el-icon>
            <span>患者管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/records">
            <el-icon><Document /></el-icon>
            <span>档案管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/questions">
            <el-icon><List /></el-icon>
            <span>问卷题库</span>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Timer /></el-icon>
            <span>登录日志</span>
          </el-menu-item>
          <el-menu-item index="/admin/tcm-history">
            <el-icon><TrendCharts /></el-icon>
            <span>体质历史</span>
          </el-menu-item>
        </template>
        
        <template v-if="userStore.role === 'DOCTOR'">
          <el-menu-item index="/doctor/patients">
            <el-icon><FirstAidKit /></el-icon>
            <span>我的患者</span>
          </el-menu-item>
          <el-menu-item index="/doctor/tcm-history">
            <el-icon><TrendCharts /></el-icon>
            <span>体质历史</span>
          </el-menu-item>
        </template>
        
        <template v-if="userStore.role === 'PATIENT'">
          <el-menu-item index="/patient/profile">
            <el-icon><UserFilled /></el-icon>
            <span>个人档案</span>
          </el-menu-item>
          <el-menu-item index="/patient/questionnaire">
            <el-icon><List /></el-icon>
            <span>体质问卷</span>
          </el-menu-item>
          <el-menu-item index="/patient/tcm-history">
            <el-icon><TrendCharts /></el-icon>
            <span>体质历史</span>
          </el-menu-item>
          <el-menu-item index="/patient/records">
            <el-icon><Document /></el-icon>
            <span>健康记录</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
           <!-- Breadcrumb could go here -->
           <span class="current-route">{{ routeName }}</span>
        </div>
        <div class="header-right">
          <div class="user-profile">
             <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
             <span class="username">{{ userStore.username }}</span>
             <el-tag size="small" effect="plain" class="role-tag">
                {{ formatRole(userStore.role) }}
             </el-tag>
          </div>
          <el-divider direction="vertical" />
          <el-button type="text" class="logout-btn" @click="handleLogout">
            退出
          </el-button>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <div class="content-wrapper">
             <router-view v-slot="{ Component }">
                <transition name="fade-slide" mode="out-in">
                  <component :is="Component" />
                </transition>
             </router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'
import { User, Management, FirstAidKit, UserFilled, List, Document, Odometer, Timer, TrendCharts } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const routeName = computed(() => {
    const map = {
        'UserManagement': '账号管理',
        'AdminPatientList': '患者管理',
        'MyPatients': '我的患者',
        'CreateRecord': '新增记录',
        'MyProfile': '个人档案',
        'Questionnaire': '体质问卷',
        'MyRecords': '健康记录',
        'Dashboard': '首页',
        'PatientTcmHistory': '体质历史',
        'DoctorTcmHistory': '体质历史',
        'AdminTcmHistory': '体质历史'
    }
    return map[route.name] || '首页'
})

const formatRole = (role) => {
    const map = {
        'ADMIN': '管理员',
        'DOCTOR': '医师',
        'PATIENT': '患者'
    }
    return map[role] || role
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background: linear-gradient(180deg, var(--tcm-primary) 0%, #1e4d4a 100%);
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0,0,0,0.05);
  z-index: 10;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background-color: rgba(0,0,0,0.1);
}

.logo-icon {
  color: white;
  margin-right: 12px;
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.el-menu-vertical {
  border-right: none;
  margin-top: 10px;
}

.el-menu-item {
  margin: 4px 12px;
  border-radius: 4px;
  height: 48px;
  line-height: 48px;
}

.el-menu-item:hover {
  background-color: rgba(255,255,255,0.1) !important;
}

.el-menu-item.is-active {
  background-color: #fff !important;
  color: var(--tcm-primary) !important;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

:deep(.el-menu-item .el-icon) {
  margin-right: 8px;
}
:deep(.el-menu-item span) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header {
  background-color: #fff;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  z-index: 9;
}

.current-route {
  font-size: 18px;
  font-weight: 600;
  color: var(--tcm-text-primary);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-size: 14px;
  color: var(--tcm-text-primary);
  font-weight: 500;
}

.role-tag {
  font-weight: normal;
}

.main-content {
  background-color: var(--tcm-bg-paper);
  padding: 24px;
  overflow-y: auto;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
