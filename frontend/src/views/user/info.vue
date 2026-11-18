<template>
  <div class="user-center-page">
    <div class="container">
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/user' }">个人中心</el-breadcrumb-item>
          <el-breadcrumb-item>个人信息</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="user-content">
        <aside class="user-sidebar">
          <div class="user-info">
            <el-avatar :size="64" :src="userInfo.avatar">
              {{ userInfo.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="user-name">{{ userInfo.username }}</div>
            <div class="user-level">普通会员</div>
          </div>
          <el-menu :default-active="activeMenu" router>
            <el-menu-item index="/user">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
            <el-menu-item index="/user/info">
              <el-icon><Edit /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="/user/orders">
              <el-icon><List /></el-icon>
              <span>我的订单</span>
            </el-menu-item>
            <el-menu-item index="/user/address">
              <el-icon><Location /></el-icon>
              <span>收货地址</span>
            </el-menu-item>
          </el-menu>
        </aside>

        <main class="user-main">
          <div class="page-title">
            <h2>个人信息</h2>
          </div>

          <el-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            label-width="100px"
            class="info-form"
            v-loading="loading"
          >
            <el-form-item label="用户名">
              <el-input v-model="formData.username" disabled />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="formData.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="formData.gender">
                <el-radio :value="0">保密</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="生日">
              <el-date-picker
                v-model="formData.birthday"
                type="date"
                placeholder="选择生日"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <div class="password-section">
            <h3>修改密码</h3>
            <el-form
              ref="pwdFormRef"
              :model="pwdForm"
              :rules="pwdRules"
              label-width="100px"
              class="pwd-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handlePasswordSubmit" :loading="pwdSubmitting">
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Edit, List, Location } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'

const userStore = useUserStore()

const activeMenu = computed(() => '/user/info')
const userInfo = computed(() => userStore.userInfo)

const loading = ref(false)
const submitting = ref(false)
const pwdSubmitting = ref(false)
const formRef = ref(null)
const pwdFormRef = ref(null)

const formData = reactive({
  username: '',
  email: '',
  phone: '',
  nickname: '',
  gender: 0,
  birthday: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await userApi.updateUserInfo(formData)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      userStore.getUserInfo()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const handlePasswordSubmit = async () => {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdSubmitting.value = true
  try {
    const res = await userApi.updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      pwdFormRef.value.resetFields()
    }
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    pwdSubmitting.value = false
  }
}

onMounted(async () => {
  await userStore.getUserInfo()
  Object.assign(formData, {
    username: userInfo.value.username,
    email: userInfo.value.email || '',
    phone: userInfo.value.phone || '',
    nickname: userInfo.value.nickname || '',
    gender: userInfo.value.gender || 0,
    birthday: userInfo.value.birthday || ''
  })
})
</script>

<style lang="scss" scoped>
.user-center-page {
  padding: 20px 0 40px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  padding: 16px 0;
}

.user-content {
  display: flex;
  gap: 20px;
}

.user-sidebar {
  width: 220px;
  flex-shrink: 0;

  .user-info {
    background: linear-gradient(135deg, #C41E3A 0%, #9B1B30 100%);
    border-radius: 8px;
    padding: 24px;
    text-align: center;
    color: #fff;
    margin-bottom: 12px;

    .el-avatar {
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
      font-size: 24px;
      margin-bottom: 12px;
    }

    .user-name {
      font-size: 18px;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .user-level {
      font-size: 12px;
      opacity: 0.8;
    }
  }

  .el-menu {
    background: #fff;
    border-radius: 8px;
    border: none;

    .el-menu-item {
      height: 48px;
      line-height: 48px;

      &.is-active {
        background: rgba(196, 30, 58, 0.1);
        color: #C41E3A;
        border-right: 3px solid #C41E3A;
      }
    }
  }
}

.user-main {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.page-title {
  margin-bottom: 24px;

  h2 {
    font-size: 18px;
    color: #262626;
  }
}

.info-form,
.pwd-form {
  max-width: 500px;
}

.password-section {
  h3 {
    font-size: 16px;
    color: #262626;
    margin-bottom: 20px;
  }
}
</style>
