<template>
  <div class="graph-captcha">
    <div class="captcha-image" @click="refresh">
      <img v-if="imageBase64" :src="imageBase64" alt="验证码" />
      <div v-else class="loading-placeholder">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
    </div>
    <el-input
      v-model="inputCode"
      placeholder="请输入验证码"
      size="large"
      clearable
      @keyup.enter="handleVerify"
    >
      <template #suffix>
        <el-button type="primary" link @click="refresh">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </template>
    </el-input>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Refresh } from '@element-plus/icons-vue'
import { captchaApi } from '@/api/captcha'

const props = defineProps({
  captchaId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:captchaId', 'verify'])

const imageBase64 = ref('')
const inputCode = ref('')
const currentCaptchaId = ref('')
const loading = ref(false)

const generateCaptcha = async () => {
  loading.value = true
  try {
    const res = await captchaApi.generate('GRAPH')
    imageBase64.value = res.data.captchaImage || `data:image/png;base64,${res.data.imageBase64}`
    currentCaptchaId.value = res.data.captchaId
    emit('update:captchaId', res.data.captchaId)
    inputCode.value = ''
  } catch (e) {
    console.error('生成验证码失败:', e)
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  if (!loading.value) {
    generateCaptcha()
  }
}

const handleVerify = () => {
  if (!inputCode.value) {
    ElMessage.warning('请输入验证码')
    return
  }
  emit('verify', {
    captchaId: currentCaptchaId.value,
    code: inputCode.value
  })
}

const getCode = () => inputCode.value

const reset = () => {
  inputCode.value = ''
  generateCaptcha()
}

onMounted(() => {
  generateCaptcha()
})

defineExpose({
  getCode,
  reset,
  captchaId: currentCaptchaId
})
</script>

<style lang="scss" scoped>
.graph-captcha {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .captcha-image {
    width: 100%;
    height: 50px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    background: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #e4e7ed;
    transition: all 0.3s;

    &:hover {
      border-color: #667eea;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }

    .loading-placeholder {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #909399;
      font-size: 14px;
    }
  }
}
</style>
