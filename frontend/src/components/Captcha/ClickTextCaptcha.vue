<template>
  <div class="click-text-captcha">
    <div class="captcha-container" ref="containerRef">
      <div class="captcha-image" v-if="backgroundImage" @click="handleClick">
        <img :src="backgroundImage" alt="验证码背景" />
        <div 
          v-for="(point, index) in clickPoints" 
          :key="index"
          class="click-point"
          :style="{ left: point.x + 'px', top: point.y + 'px' }"
        >
          {{ index + 1 }}
        </div>
      </div>
      <div class="loading-placeholder" v-else>
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
    </div>
    
    <div class="captcha-footer">
      <div class="captcha-hint">
        <span>点击：</span>
        <span class="hint-text">{{ hintText }}</span>
      </div>
      <div class="captcha-actions">
        <el-button type="primary" link size="small" @click="refresh">
          <el-icon><Refresh /></el-icon>
          换一张
        </el-button>
        <el-button type="primary" link size="small" @click="clearPoints">
          <el-icon><Delete /></el-icon>
          清除
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Refresh, Delete } from '@element-plus/icons-vue'
import { captchaApi } from '@/api/captcha'

const props = defineProps({
  captchaId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:captchaId', 'verify'])

const containerRef = ref(null)
const backgroundImage = ref('')
const currentCaptchaId = ref('')
const hintText = ref('')
const clickPoints = ref([])
const loading = ref(false)
const targetCount = ref(3)

const generateCaptcha = async () => {
  loading.value = true
  clickPoints.value = []
  try {
    const res = await captchaApi.generate('CLICK_TEXT')
    // 后端返回 captchaImage，前端使用 backgroundImage
    backgroundImage.value = res.data.captchaImage
    currentCaptchaId.value = res.data.captchaId
    // 后端返回 tipText，前端使用 hintText
    hintText.value = res.data.tipText || ''
    targetCount.value = res.data.targetCount || 3
    emit('update:captchaId', res.data.captchaId)
  } catch (e) {
    console.error('生成验证码失败:', e)
  } finally {
    loading.value = false
  }
}

const handleClick = (e) => {
  if (loading.value) return
  
  // 获取图片容器的位置
  const container = e.currentTarget
  const rect = container.getBoundingClientRect()
  const x = Math.round(e.clientX - rect.left)
  const y = Math.round(e.clientY - rect.top)
  
  console.log('点击位置:', x, y, '目标数量:', targetCount.value, '已点击:', clickPoints.value.length)
  
  if (clickPoints.value.length < targetCount.value) {
    clickPoints.value.push({ x, y })
    
    if (clickPoints.value.length === targetCount.value) {
      emit('verify', {
        captchaId: currentCaptchaId.value,
        clickPoints: clickPoints.value
      })
    }
  }
}

const clearPoints = () => {
  clickPoints.value = []
}

const refresh = () => {
  if (!loading.value) {
    generateCaptcha()
  }
}

const setVerified = (success) => {
  if (!success) {
    clearPoints()
  }
}

const reset = () => {
  generateCaptcha()
}

onMounted(() => {
  generateCaptcha()
})

defineExpose({
  setVerified,
  reset,
  captchaId: currentCaptchaId
})
</script>

<style lang="scss" scoped>
.click-text-captcha {
  .captcha-container {
    width: 100%;
    height: 80px;
    border-radius: 8px;
    overflow: hidden;
    background: #f5f7fa;
    border: 1px solid #e4e7ed;
    position: relative;

    .captcha-image {
      width: 100%;
      height: 100%;
      position: relative;
      cursor: crosshair;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .click-point {
        position: absolute;
        width: 18px;
        height: 18px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 10px;
        font-weight: bold;
        transform: translate(-50%, -50%);
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
        animation: pointPop 0.2s ease-out;
      }
    }

    .loading-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 6px;
      color: #909399;
      font-size: 12px;
    }
  }

  .captcha-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 6px;
  }

  .captcha-hint {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;

    span:first-child {
      color: #606266;
    }

    .hint-text {
      color: #667eea;
      font-weight: 500;
      letter-spacing: 1px;
    }
  }

  .captcha-actions {
    display: flex;
    gap: 8px;
  }
}

@keyframes pointPop {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 0;
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
}
</style>
