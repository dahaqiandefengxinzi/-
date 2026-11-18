<template>
  <div class="captcha-wrapper" v-if="enabled">
    <div class="captcha-header">
      <span class="captcha-label">安全验证</span>
      <el-tag size="small" type="info">{{ typeLabel }}</el-tag>
    </div>
    
    <div class="captcha-content">
      <GraphCaptcha
        v-if="captchaType === 'GRAPH'"
        ref="captchaRef"
        @verify="handleVerify"
      />
      <SlideCaptcha
        v-else-if="captchaType === 'SLIDE'"
        ref="captchaRef"
        @verify="handleVerify"
      />
      <ClickTextCaptcha
        v-else-if="captchaType === 'CLICK_TEXT'"
        ref="captchaRef"
        @verify="handleVerify"
      />
      <CalcCaptcha
        v-else-if="captchaType === 'CALC'"
        ref="captchaRef"
        @verify="handleVerify"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { captchaApi } from '@/api/captcha'
import GraphCaptcha from './GraphCaptcha.vue'
import SlideCaptcha from './SlideCaptcha.vue'
import ClickTextCaptcha from './ClickTextCaptcha.vue'
import CalcCaptcha from './CalcCaptcha.vue'

const emit = defineEmits(['verified', 'error'])

const enabled = ref(false)
const captchaType = ref('GRAPH')
const captchaRef = ref(null)
const verified = ref(false)
const verifyData = ref(null)
const verifying = ref(false)

const typeLabel = computed(() => {
  const labels = {
    GRAPH: '图形验证码',
    SLIDE: '滑动拼图',
    CLICK_TEXT: '文字点选',
    CALC: '计算题'
  }
  return labels[captchaType.value] || '验证码'
})

const checkEnabled = async () => {
  try {
    const res = await captchaApi.isEnabled()
    enabled.value = res.data.enabled
    captchaType.value = res.data.captchaType || 'GRAPH'
  } catch (e) {
    console.error('检查验证码状态失败:', e)
    enabled.value = false
  }
}

const handleVerify = async (data) => {
  verifying.value = true
  try {
    // 转换 clickPoints 格式：[{x, y}] -> [[x, y]]
    let clickPointsArray = null
    if (data.clickPoints && data.clickPoints.length > 0) {
      clickPointsArray = data.clickPoints.map(p => [p.x, p.y])
    }
    
    // 转换数据格式以匹配后端期望的字段名
    const requestData = {
      captchaId: data.captchaId,
      captchaType: captchaType.value,
      captchaCode: data.code,
      sliderX: data.sliderX,
      clickPoints: clickPointsArray
    }
    console.log('发送验证请求:', requestData)
    const res = await captchaApi.verify(requestData)
    if (res.data.success) {
      verified.value = true
      verifyData.value = data
      // 不在这里显示成功提示，由调用方决定是否显示
      emit('verified', data)
      
      if (captchaRef.value && captchaRef.value.setVerified) {
        captchaRef.value.setVerified(true)
      }
    } else {
      ElMessage.error('验证失败，请重试')
      emit('error', new Error('验证失败'))
      reset()
    }
  } catch (e) {
    console.error('验证失败:', e)
    emit('error', e)
    reset()
  } finally {
    verifying.value = false
  }
}

// 主动触发验证（用于登录时自动验证）
const doVerify = async () => {
  if (verified.value) return true
  if (!captchaRef.value) return false
  
  // 获取验证码数据
  console.log('captchaRef.value:', captchaRef.value)
  console.log('captchaRef.value.captchaId:', captchaRef.value.captchaId)
  
  const captchaId = captchaRef.value.captchaId?.value !== undefined 
    ? captchaRef.value.captchaId.value 
    : captchaRef.value.captchaId
  
  console.log('获取到的 captchaId:', captchaId)
  
  const data = {
    captchaId: captchaId,
    code: captchaRef.value.getCode?.(),
    sliderX: captchaRef.value.sliderX,
    clickPoints: captchaRef.value.clickPoints
  }
  
  console.log('验证码数据:', data)
  
  if (!data.captchaId) {
    ElMessage.warning('验证码未加载，请刷新页面')
    return false
  }
  
  // 图形验证码和计算题需要检查是否输入
  if ((captchaType.value === 'GRAPH' || captchaType.value === 'CALC') && !data.code) {
    ElMessage.warning('请输入验证码')
    return false
  }
  
  await handleVerify(data)
  return verified.value
}

const reset = () => {
  verified.value = false
  verifyData.value = null
  verifying.value = false
  if (captchaRef.value && captchaRef.value.reset) {
    captchaRef.value.reset()
  }
}

const getVerifyData = () => {
  if (!verified.value) return null
  return verifyData.value
}

const isEnabled = () => enabled.value

const isVerifying = () => verifying.value

const getCaptchaType = () => captchaType.value

onMounted(() => {
  checkEnabled()
})

defineExpose({
  reset,
  getVerifyData,
  isEnabled,
  isVerifying,
  getCaptchaType,
  verified,
  doVerify
})
</script>

<style lang="scss" scoped>
.captcha-wrapper {
  margin-top: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 12px;
  border: 1px solid #f0f0f0;

  .captcha-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .captcha-label {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }
  }

  .captcha-content {
    width: 100%;
  }
}
</style>
