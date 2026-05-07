import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue')
    },
    {
      path: '/',
      component: () => import('@/views/Layout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue') // Placeholder
        },
        // Admin Routes
        {
            path: 'admin/users',
            name: 'UserManagement',
            component: () => import('@/views/admin/UserList.vue'),
            meta: { role: 'ADMIN' }
        },
        {
            path: 'admin/patients',
            name: 'AdminPatientList',
            component: () => import('@/views/admin/PatientList.vue'),
            meta: { role: 'ADMIN' }
        },
        {
            path: 'admin/records',
            name: 'AdminHealthRecordList',
            component: () => import('@/views/admin/HealthRecordList.vue'),
            meta: { role: 'ADMIN' }
        },
        {
            path: 'admin/questions',
            name: 'AdminQuestionList',
            component: () => import('@/views/admin/QuestionList.vue'),
            meta: { role: 'ADMIN' }
        },
        {
            path: 'admin/logs',
            name: 'AdminLoginLogList',
            component: () => import('@/views/admin/LoginLogList.vue'),
            meta: { role: 'ADMIN' }
        },
        // Doctor Routes
        {
            path: 'doctor/patients',
            name: 'MyPatients',
            component: () => import('@/views/doctor/MyPatients.vue'),
            meta: { role: 'DOCTOR' }
        },
        {
             path: 'doctor/record/:patientId',
             name: 'CreateRecord',
             component: () => import('@/views/doctor/HealthRecordForm.vue'),
             meta: { role: 'DOCTOR' }
        },
        // Patient Routes
        {
             path: 'patient/profile',
             name: 'MyProfile',
             component: () => import('@/views/patient/Profile.vue'),
             meta: { role: 'PATIENT' }
        },
        {
             path: 'patient/questionnaire',
             name: 'Questionnaire',
             component: () => import('@/views/patient/Questionnaire.vue'),
             meta: { role: 'PATIENT' }
        },
         {
             path: 'patient/records',
             name: 'MyRecords',
             component: () => import('@/views/patient/MyRecords.vue'),
             meta: { role: 'PATIENT' }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.name !== 'Login' && to.name !== 'Register' && !userStore.token) {
    next({ name: 'Login' })
  } else if (to.meta.role && to.meta.role !== userStore.role) {
    // Simple role check. If user is ADMIN, maybe they can access others?
    // Prompt says "Strict data isolation", implies role isolation too.
    // We stick to exact match or basic hierarchy if needed. 
    // For now, exact match.
    // Wait, if I am ADMIN, I shouldn't see Patient pages.
    next(false) // Or redirect to 403
  } else {
    next()
  }
})

export default router
