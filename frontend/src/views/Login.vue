<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- Left Side: Hero Section -->
      <div class="login-left">
        <div class="hero-content">
          <div class="brand-logo">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="tcm-logo">
              <path d="M12 3C7.02944 3 3 7.02944 3 12C3 16.9706 7.02944 21 12 21C16.9706 21 21 16.9706 21 12C21 7.02944 16.9706 3 12 3ZM12 5C15.866 5 19 8.13401 19 12C19 15.866 15.866 19 12 19C8.13401 19 5 15.866 5 12C5 8.13401 8.13401 5 12 5Z" fill="currentColor" fill-opacity="0.2"/>
              <path d="M12 6C12 6 14 10 16 12C18 14 12 18 12 18C12 18 6 14 8 12C10 10 12 6 12 6Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 18V21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M12 3V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h1 class="hero-title">悬壶济世 · 中医管理</h1>
          <p class="hero-slogan">传承千年中医智慧，赋能现代健康管理</p>
          <div class="hero-features">
            <div class="feature-item">
              <el-icon><Monitor /></el-icon>
              <span>智能体质辨识</span>
            </div>
            <div class="feature-item">
              <el-icon><Notebook /></el-icon>
              <span>数字化健康档案</span>
            </div>
            <div class="feature-item">
              <el-icon><FirstAidKit /></el-icon>
              <span>个性化养生建议</span>
            </div>
          </div>
        </div>
        <!-- Dynamic Background Elements -->
        <div class="bg-decoration circle-1"></div>
        <div class="bg-decoration circle-2"></div>
      </div>

      <!-- Right Side: Login Form -->
      <div class="login-right">
        <div class="form-container">
          <div class="form-header">
            <h2>用户登录</h2>
            <p class="sub-text">欢迎回到中医特色患者管理系统</p>
          </div>
          
          <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin" class="login-form" size="large">
            <el-form-item prop="username" :error="usernameError">
              <el-input 
                v-model="form.username" 
                placeholder="请输入账号" 
                :prefix-icon="User"
                @input="clearErrors()"
              />
            </el-form-item>
            <el-form-item prop="password" :error="passwordError">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="请输入密码" 
                :prefix-icon="Lock"
                show-password
                @input="clearErrors()"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                native-type="submit" 
                :loading="loading" 
                class="login-btn"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <p>还没有账号？ <el-link type="primary" @click="$router.push('/register')">立即注册</el-link></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'
import { ElMessage } from 'element-plus'
import { User, Lock, Monitor, Notebook, FirstAidKit } from '@element-plus/icons-vue'

const form = ref({
  username: '',
  password: ''
})
const loading = ref(false)
const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const usernameError = ref('')
const passwordError = ref('')
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const clearErrors = () => {
  usernameError.value = ''
  passwordError.value = ''
}

const handleLogin = async () => {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  
  loading.value = true
  try {
    const res = await request.post('/auth/login', form.value)
    userStore.setLoginInfo(res)
    ElMessage.closeAll()
    ElMessage.success('登录成功')
    
    // Redirect to dashboard for all roles
    router.push('/dashboard')
  } catch (e) {
    if (e && e.isBusinessError && e.code === 401) {
      const msg = e.message || ''
      if (msg.includes('账号不存在')) {
        usernameError.value = msg
      } else if (msg.includes('密码错误')) {
        passwordError.value = msg
      } else {
        passwordError.value = msg || '账号或密码错误'
      }
    } else if (e && e.response && (e.response.status === 401 || e.response.status === 403)) {
      const msg = e.response.data?.message || ''
      if (msg.includes('账号不存在')) {
        usernameError.value = msg
      } else {
        passwordError.value = msg || '账号或密码错误'
      }
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  background-color: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.login-wrapper {
  display: flex;
  width: 1000px;
  height: 600px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* Left Side */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, var(--tcm-primary) 0%, #1e4d4a 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  padding: 40px;
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.tcm-logo {
  width: 80px;
  height: 80px;
  margin-bottom: 24px;
  color: rgba(255, 255, 255, 0.9);
}

.hero-title {
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 20px;
  letter-spacing: 4px;
  text-shadow: 0 4px 8px rgba(0,0,0,0.3);
  color: #fff;
}

.hero-slogan {
  font-size: 20px;
  opacity: 0.95;
  font-weight: 600;
  margin-bottom: 50px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
  letter-spacing: 1px;
}

.hero-features {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  opacity: 0.9;
}

.feature-item .el-icon {
  font-size: 24px;
  background: rgba(255, 255, 255, 0.1);
  padding: 12px;
  border-radius: 50%;
}

/* Background Decorations */
.bg-decoration {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -50px;
  left: -50px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -30px;
  right: -30px;
}

/* Right Side */
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--tcm-bg-paper);
}

.form-container {
  width: 360px;
  padding: 40px;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 24px;
  color: var(--tcm-primary);
  margin-bottom: 8px;
}

.sub-text {
  color: var(--tcm-text-secondary);
  font-size: 14px;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  letter-spacing: 4px;
  margin-top: 10px;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--tcm-text-regular);
}
</style>
