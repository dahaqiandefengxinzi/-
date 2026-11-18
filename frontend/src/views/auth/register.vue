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
          <h2>创建账户</h2>
          <p>加入 ShopLab，开启购物之旅</p>
        </div>
        
        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          label-position="top"
          @submit.prevent="handleRegister"
          class="auth-form"
        >
          <div class="registration-methods" role="tablist" aria-label="注册方式">
            <button type="button" role="tab" data-testid="registration-phone-tab" :aria-selected="method === 'phone'" @click="selectMethod('phone')">手机号注册</button>
            <button type="button" role="tab" data-testid="registration-email-tab" :aria-selected="method === 'email'" @click="selectMethod('email')">邮箱注册</button>
            <button type="button" role="tab" :aria-selected="method === 'username'" @click="selectMethod('username')">用户名注册</button>
          </div>
          <el-form-item v-if="method === 'phone'" label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Phone" size="large" />
          </el-form-item>
          <el-form-item v-if="method === 'email'" label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" :prefix-icon="Message" size="large" />
          </el-form-item>
          <el-form-item v-if="method !== 'username'" label="验证码" prop="code">
            <div class="verification-row">
              <el-input v-model="form.code" placeholder="请输入6位验证码" maxlength="6" inputmode="numeric" size="large" data-testid="registration-code" />
              <el-button data-testid="registration-send-code" :disabled="sending || remaining > 0" :loading="sending" @click="sendCode">
                <span v-if="remaining > 0" data-testid="registration-countdown">{{ remaining }}秒后重新获取</span>
                <span v-else>获取验证码</span>
              </el-button>
            </div>
          </el-form-item>
          <el-form-item v-if="method === 'username'" label="用户名" prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          
          <div class="form-row">
            <el-form-item label="密码" prop="password" class="half-width">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                size="large"
              />
            </el-form-item>
            
            <el-form-item v-if="method === 'username'" label="确认密码" prop="confirmPassword" class="half-width">
              <el-input 
                v-model="form.confirmPassword" 
                type="password" 
                placeholder="请再次输入密码"
                :prefix-icon="Lock"
                show-password
                size="large"
              />
            </el-form-item>
          </div>
          
          <div v-if="method === 'username'" class="form-row">
            <el-form-item label="邮箱" prop="email" class="half-width">
              <el-input 
                v-model="form.email" 
                placeholder="请输入邮箱"
                :prefix-icon="Message"
                size="large"
              />
            </el-form-item>
            
            <el-form-item label="手机号" prop="phone" class="half-width">
              <el-input 
                v-model="form.phone" 
                placeholder="请输入手机号"
                :prefix-icon="Phone"
                size="large"
              />
            </el-form-item>
          </div>
          
          <el-form-item v-if="method === 'username'" class="agreement-row">
            <el-checkbox v-model="agreed">
              我已阅读并同意
              <a href="#" @click.prevent>《用户协议》</a>
              和
              <a href="#" @click.prevent>《隐私政策》</a>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              class="submit-btn"
              native-type="submit"
              size="large"
              :disabled="method === 'username' && !agreed"
              data-testid="registration-submit"
            >
              {{ loading ? '注册中...' : '立即注册' }}
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
            微信注册
          </el-button>
        </div>
        
        <div class="auth-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
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
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const agreed = ref(false)
const method = ref('phone')
const sending = ref(false)
const remaining = ref(0)
const cooldowns = new Map()
let timer
const tick = () => {
  const id = method.value === 'phone' ? form.phone.trim() : form.email.trim().toLowerCase()
  remaining.value = Math.max(0, Math.ceil(((cooldowns.get(method.value + ':' + id) || 0) - Date.now()) / 1000))
}
const selectMethod = (value) => {
  method.value = value
  form.code = ''
  formRef.value?.clearValidate()
  tick()
}
const sendCode = async () => {
  const channel = method.value
  if (!['phone', 'email'].includes(channel)) return
  const valid = await formRef.value.validateField(channel).then(() => true).catch(() => false)
  if (!valid) return
  sending.value = true
  try {
    const identifier = (channel === 'phone' ? form.phone : form.email).trim()
    const result = await authApi.registrationCode({ method: channel, identifier })
    cooldowns.set(channel + ':' + identifier.toLowerCase(), Date.now() + result.data.resendAfterSeconds * 1000)
    tick()
    if (!timer) timer = setInterval(tick, 250)
    ElMessage.success('验证码已发送')
  } catch {
    // 请求拦截器已向用户显示服务端错误，保留重试能力。
  } finally { sending.value = false }
}
onUnmounted(() => clearInterval(timer))

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  code: ''
})

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 8) {
    callback(new Error('密码长度至少8位'))
  } else if (!/[a-zA-Z]/.test(value)) {
    callback(new Error('密码必须包含字母'))
  } else if (!/\d/.test(value)) {
    callback(new Error('密码必须包含数字'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '请输入6位数字验证码', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (method.value === 'username' && !agreed.value) {
    ElMessage.warning('请先同意用户协议和隐私政策')
    return
  }
  
  loading.value = true
  try {
    await userStore.register(method.value === 'username' ? {
      username: form.username,
      password: form.password,
      email: form.email,
      phone: form.phone
    } : {
      method: method.value, code: form.code, password: form.password,
      [method.value]: method.value === 'phone' ? form.phone : form.email
    })
    ElMessage.success({ message: '注册成功', duration: 5000, customClass: 'registration-success' })
    router.push('/')
  } catch (e) {
    console.error('register error:', e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.registration-methods {
  display: flex; gap: 8px; margin-bottom: 24px;
  button { flex: 1; border: 0; border-radius: 10px; padding: 12px 4px; background: #f1f3f8; color: #606675; cursor: pointer; }
  button[aria-selected="true"] { background: #283b6a; color: white; }
}
.verification-row { display: flex; gap: 10px; width: 100%; align-items: center; }
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
    max-width: 520px;
    padding: 20px;
    position: relative;
    z-index: 1;
  }
  
  .auth-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 24px;
    padding: 40px 36px;
    box-shadow: 
      0 25px 50px -12px rgba(0, 0, 0, 0.25),
      0 0 0 1px rgba(255, 255, 255, 0.1);
    
    .auth-header {
      text-align: center;
      margin-bottom: 28px;
      
      .logo-wrapper {
        margin-bottom: 16px;
        
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
        font-size: 26px;
        font-weight: 600;
        color: #1a1a2e;
        margin-bottom: 6px;
      }
      
      p {
        color: #6b7280;
        font-size: 14px;
      }
    }
    
    .auth-form {
      .form-row {
        display: flex;
        gap: 16px;
        
        .half-width {
          flex: 1;
        }
      }
      
      .agreement-row {
        margin-bottom: 16px;
        
        :deep(.el-checkbox__label) {
          font-size: 13px;
          color: #6b7280;
          
          a {
            color: #667eea;
            text-decoration: none;
            
            &:hover {
              color: #764ba2;
            }
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
        font-size: 14px;
        padding-bottom: 6px;
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
      
      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 10px 20px -5px rgba(102, 126, 234, 0.4);
      }
      
      &:active:not(:disabled) {
        transform: translateY(0);
      }
      
      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }
    
    .auth-divider {
      display: flex;
      align-items: center;
      margin: 24px 0;
      
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
      margin-top: 24px;
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
    margin-top: 28px;
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

@media (max-width: 540px) {
  .auth-page {
    .auth-card {
      padding: 32px 24px;
      
      .auth-header {
        h2 {
          font-size: 22px;
        }
      }
      
      .auth-form {
        .form-row {
          flex-direction: column;
          gap: 0;
        }
      }
    }
  }
}
</style>
