<template>
  <div class="auth-page">
    <div class="auth-background">
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
    </div>
    
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <div class="logo-wrapper">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 7H4C2.89543 7 2 7.89543 2 9V19C2 20.1046 2.89543 21 4 21H20C21.1046 21 22 20.1046 22 19V9C22 7.89543 21.1046 7 20 7Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 7V5C16 3.89543 15.1046 3 14 3H10C8.89543 3 8 3.89543 8 5V7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="14" r="2" stroke="currentColor" stroke-width="2"/>
              </svg>
            </div>
          </div>
          <h2>欢迎回来</h2>
          <p>登录您的 ShopLab 账户</p>
        </div>
        
        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          label-position="top"
          @submit.prevent="handleLogin"
          class="auth-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              size="large"
            />
          </el-form-item>
          
          <Captcha ref="captchaRef" @verified="onCaptchaVerified" @error="onCaptchaError" />
          
          <el-form-item class="remember-row">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <a href="#" class="forgot-link">忘记密码？</a>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              class="submit-btn"
              native-type="submit"
              size="large"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="auth-divider">
          <span>或</span>
        </div>
        
        <div class="social-login">
          <el-button class="social-btn" size="large">
            <svg viewBox="0 0 24 24" width="20" height="20">
              <path fill="#07C160" d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.407-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982z"/>
            </svg>
            微信登录
          </el-button>
        </div>
        
        <div class="auth-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
      
      <div class="auth-brand">
        <h1>ShopLab</h1>
        <p>品质生活，从这里开始</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import Captcha from '@/components/Captcha/index.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const captchaRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const captchaVerified = ref(false)
const captchaData = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const loadRememberedCredentials = () => {
  const remembered = localStorage.getItem('rememberMe')
  if (remembered === 'true') {
    rememberMe.value = true
    form.username = localStorage.getItem('rememberedUsername') || ''
    form.password = localStorage.getItem('rememberedPassword') || ''
  }
}

const saveCredentials = () => {
  if (rememberMe.value) {
    localStorage.setItem('rememberMe', 'true')
    localStorage.setItem('rememberedUsername', form.username)
    localStorage.setItem('rememberedPassword', form.password)
  } else {
    localStorage.removeItem('rememberMe')
    localStorage.removeItem('rememberedUsername')
    localStorage.removeItem('rememberedPassword')
  }
}

const onCaptchaVerified = (data) => {
  captchaVerified.value = true
  captchaData.value = data
}

const onCaptchaError = () => {
  captchaVerified.value = false
  captchaData.value = null
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  // 检查验证码是否启用
  if (captchaRef.value && captchaRef.value.isEnabled()) {
    // 如果正在验证中，等待验证完成
    if (captchaRef.value.isVerifying && captchaRef.value.isVerifying()) {
      ElMessage.info('验证码验证中，请稍候...')
      return
    }
    // 如果还未验证，主动触发验证
    if (!captchaVerified.value) {
      const verifySuccess = await captchaRef.value.doVerify()
      if (!verifySuccess) {
        return
      }
    }
  }
  
  loading.value = true
  try {
    const loginData = {
      username: form.username,
      password: form.password
    }
    
    if (captchaData.value) {
      loginData.captchaId = captchaData.value.captchaId
      loginData.captchaCode = captchaData.value.code
      loginData.sliderX = captchaData.value.sliderX
      loginData.clickPoints = captchaData.value.clickPoints
    }
    
    await userStore.login(loginData)
    saveCredentials()
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    console.error('login error:', e)
    if (captchaRef.value && captchaRef.value.reset) {
      captchaRef.value.reset()
      captchaVerified.value = false
      captchaData.value = null
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRememberedCredentials()
})
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .auth-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    overflow: hidden;
    
    .floating-shapes {
      position: absolute;
      width: 100%;
      height: 100%;
      
      .shape {
        position: absolute;
        border-radius: 50%;
        opacity: 0.1;
        animation: float 20s infinite ease-in-out;
      }
      
      .shape-1 {
        width: 400px;
        height: 400px;
        background: #fff;
        top: -100px;
        left: -100px;
        animation-delay: 0s;
      }
      
      .shape-2 {
        width: 300px;
        height: 300px;
        background: #fff;
        bottom: -50px;
        right: -50px;
        animation-delay: -5s;
      }
      
      .shape-3 {
        width: 200px;
        height: 200px;
        background: #fff;
        top: 50%;
        right: 10%;
        animation-delay: -10s;
      }
      
      .shape-4 {
        width: 150px;
        height: 150px;
        background: #fff;
        bottom: 20%;
        left: 10%;
        animation-delay: -15s;
      }
    }
  }
  
  @keyframes float {
    0%, 100% {
      transform: translateY(0) rotate(0deg);
    }
    25% {
      transform: translateY(-20px) rotate(5deg);
    }
    50% {
      transform: translateY(0) rotate(0deg);
    }
    75% {
      transform: translateY(20px) rotate(-5deg);
    }
  }
  
  .auth-container {
    width: 100%;
    max-width: 440px;
    padding: 20px;
    position: relative;
    z-index: 1;
  }
  
  .auth-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 24px;
    padding: 48px 40px;
    box-shadow: 
      0 25px 50px -12px rgba(0, 0, 0, 0.25),
      0 0 0 1px rgba(255, 255, 255, 0.1);
    
    .auth-header {
      text-align: center;
      margin-bottom: 36px;
      
      .logo-wrapper {
        margin-bottom: 20px;
        
        .logo-icon {
          width: 64px;
          height: 64px;
          margin: 0 auto;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          
          svg {
            width: 32px;
            height: 32px;
          }
        }
      }
      
      h2 {
        font-size: 28px;
        font-weight: 600;
        color: #1a1a2e;
        margin-bottom: 8px;
      }
      
      p {
        color: #6b7280;
        font-size: 15px;
      }
    }
    
    .auth-form {
      .remember-row {
        :deep(.el-form-item__content) {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        
        .forgot-link {
          color: #667eea;
          font-size: 14px;
          text-decoration: none;
          
          &:hover {
            color: #764ba2;
          }
        }
      }
      
      :deep(.el-input__wrapper) {
        border-radius: 12px;
        box-shadow: 0 0 0 1px #e5e7eb;
        transition: all 0.3s;
        
        &:hover {
          box-shadow: 0 0 0 1px #667eea;
        }
        
        &.is-focus {
          box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2), 0 0 0 1px #667eea;
        }
      }
      
      :deep(.el-form-item__label) {
        font-weight: 500;
        color: #374151;
      }
    }
    
    .submit-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 12px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 20px -5px rgba(102, 126, 234, 0.4);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
    
    .auth-divider {
      display: flex;
      align-items: center;
      margin: 28px 0;
      
      &::before,
      &::after {
        content: '';
        flex: 1;
        height: 1px;
        background: #e5e7eb;
      }
      
      span {
        padding: 0 16px;
        color: #9ca3af;
        font-size: 14px;
      }
    }
    
    .social-login {
      .social-btn {
        width: 100%;
        height: 48px;
        border-radius: 12px;
        border: 1px solid #e5e7eb;
        background: #fff;
        font-size: 15px;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        transition: all 0.3s;
        
        &:hover {
          border-color: #07C160;
          background: rgba(7, 193, 96, 0.05);
        }
      }
    }
    
    .auth-footer {
      text-align: center;
      margin-top: 28px;
      color: #6b7280;
      font-size: 14px;
      
      a {
        color: #667eea;
        font-weight: 500;
        margin-left: 4px;
        text-decoration: none;
        
        &:hover {
          color: #764ba2;
        }
      }
    }
  }
  
  .auth-brand {
    text-align: center;
    margin-top: 32px;
    color: #fff;
    
    h1 {
      font-size: 24px;
      font-weight: 700;
      margin-bottom: 8px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
    
    p {
      font-size: 14px;
      opacity: 0.9;
    }
  }
}

@media (max-width: 480px) {
  .auth-page {
    .auth-card {
      padding: 32px 24px;
      
      .auth-header {
        h2 {
          font-size: 24px;
        }
      }
    }
  }
}
</style>
