import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, userApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 1 || userInfo.value?.role === 'admin')
  
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }
  
  const login = async (loginData) => {
    const res = await authApi.login(loginData)
    setToken(res.data.token)
    setUserInfo(res.data.user)
    return res
  }
  
  const register = async (registerData) => {
    const res = await authApi.register(registerData)
    setToken(res.data.token)
    setUserInfo(res.data.user)
    return res
  }
  
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (e) {
      // 静默处理登出错误，不显示提示
      console.log('logout error (ignored):', e.message)
    }
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
  
  const getUserInfo = async () => {
    const res = await userApi.getInfo()
    setUserInfo(res.data)
    return res
  }
  
  const updateUserInfo = async (data) => {
    const res = await userApi.updateInfo(data)
    await getUserInfo()
    return res
  }
  
  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    setToken,
    setUserInfo,
    login,
    register,
    logout,
    getUserInfo,
    updateUserInfo
  }
})
