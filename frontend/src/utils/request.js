import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    
    if (res.code === 200) {
      return res
    }
    
    const silent = response.config?.silent
    
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push({ name: 'Login' })
      if (!silent) {
        ElMessage.error('登录已过期，请重新登录')
      }
      return Promise.reject(new Error(res.message || '未授权'))
    }
    
    if (res.code === 403) {
      if (!silent) {
        ElMessage.error('没有权限访问')
      }
      return Promise.reject(new Error(res.message || '无权限'))
    }
    
    if (!silent) {
      ElMessage.error(res.message || '请求失败')
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    const silent = error.config?.silent
    
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      if (status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push({ name: 'Login' })
        if (!silent) {
          ElMessage.error(data?.message || '未授权，请登录')
        }
      } else if (status === 403) {
        if (!silent) {
          ElMessage.error(data?.message || '拒绝访问')
        }
      } else if (!silent) {
        ElMessage.error(data?.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      if (!silent) {
        ElMessage.error('请求超时，请稍后重试')
      }
    } else if (!silent) {
      ElMessage.error('网络错误，请稍后重试')
    }
    
    return Promise.reject(error)
  }
)

export default request
