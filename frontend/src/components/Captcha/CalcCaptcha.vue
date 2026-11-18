<template>
  <div class="calc-captcha">
    <div class="captcha-display">
      <div class="calc-image" @click="refresh">
        <img v-if="imageBase64" :src="imageBase64" alt="计算题验证码" />
        <div v-else class="loading-placeholder">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
      </div>
    </div>
    <div class="input-row">
      <span class="equal-sign">=</span>
      <el-input
        v-model="inputResult"
        placeholder="请输入计算结果"
        size="large"
        type="number"
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
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
const inputResult = ref('')
const currentCaptchaId = ref('')
const loading = ref(false)

const generateCaptcha = async () => {
  loading.value = true
  try {
    const res = await captchaApi.generate('CALC')
    // 后端返回 captchaImage，前端使用 imageBase64
    imageBase64.value = res.data.captchaImage
    currentCaptchaId.value = res.data.captchaId
    emit('update:captchaId', res.data.captchaId)
    inputResult.value = ''
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
  if (!inputResult.value) {
    ElMessage.warning('请输入计算结果')
    return
  }
  emit('verify', {
    captchaId: currentCaptchaId.value,
    code: inputResult.value
  })
}

const getCode = () => inputResult.value

const reset = () => {
  inputResult.value = ''
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
.calc-captcha {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .captcha-display {
    .calc-image {
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

  .input-row {
    display: flex;
    align-items: center;
    gap: 12px;

    .equal-sign {
      font-size: 24px;
      font-weight: bold;
      color: #667eea;
    }

    .el-input {
      flex: 1;
    }
  }
}
</style>
