import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const computedBase = (() => {
  const envBase = typeof import.meta !== 'undefined' ? import.meta.env?.VITE_API_BASE : undefined
  return envBase || '/api'
})()

const request = axios.create({
  baseURL: computedBase,
  timeout: 8000
})

// Request interceptor
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor
request.interceptors.response.use(
  response => {
    const res = response.data
    // If backend returns standard Result object
    if (res.code && res.code !== 200) {
      ElMessage.closeAll()
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const isLogin = response.config && typeof response.config.url === 'string' && response.config.url.includes('/auth/login')
        if (!isLogin) {
          const userStore = useUserStore()
          userStore.logout()
          window.location.reload()
        }
      }
      return Promise.reject({
        isBusinessError: true,
        code: res.code,
        message: res.message,
        config: response.config
      })
    }
    return res.data // Return actual data
  },
  error => {
    // Handle HTTP errors
    if (error.response) {
       if (error.response.status === 401 || error.response.status === 403) {
           const isLogin = error.config && typeof error.config.url === 'string' && error.config.url.includes('/auth/login')
           ElMessage.closeAll()
           if (isLogin) {
             ElMessage.error(error.response.data?.message || '账号或密码错误')
           } else {
             ElMessage.error('权限不足或登录过期')
           }
       } else {
           const msg = error.response.data?.message || `请求错误（${error.response.status}）`
           ElMessage.error(msg)
       }
    } else {
        ElMessage.error('网络错误或网关不可用')
    }
    return Promise.reject(error)
  }
)

export default request
