import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const role = ref(localStorage.getItem('role') || '')
  const username = ref(localStorage.getItem('username') || '')
  const userId = ref(localStorage.getItem('userId') || '')

  function setLoginInfo(info) {
    token.value = info.token
    role.value = info.role
    username.value = info.username
    userId.value = info.id
    
    localStorage.setItem('token', info.token)
    localStorage.setItem('role', info.role)
    localStorage.setItem('username', info.username)
    localStorage.setItem('userId', info.id)
  }

  function logout() {
    token.value = ''
    role.value = ''
    username.value = ''
    userId.value = ''
    
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('username')
    localStorage.removeItem('userId')
  }

  return { token, role, username, userId, setLoginInfo, logout }
})
