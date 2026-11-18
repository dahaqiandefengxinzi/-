<template>
  <div class="slide-captcha">
    <div class="captcha-container" ref="containerRef">
      <div class="background-image" v-if="backgroundImage">
        <img :src="backgroundImage" alt="背景图" ref="bgImageRef" />
        <img 
          :src="sliderImage" 
          class="slider-block"
          :style="sliderBlockStyle"
          alt="滑块"
        />
      </div>
      <div class="loading-placeholder" v-else>
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
    </div>
    
    <div class="slider-track">
      <div class="slider-track-bg" :style="{ width: sliderOffset + 'px' }"></div>
      <div 
        class="slider-button"
        :style="{ left: sliderOffset + 'px' }"
        @mousedown="startDrag"
        @touchstart="startDrag"
      >
        <el-icon v-if="!verified"><DArrowRight /></el-icon>
        <el-icon v-else class="success"><Check /></el-icon>
      </div>
      <span class="slider-hint" v-if="sliderOffset === 0">向右滑动完成验证</span>
    </div>
    
    <div class="captcha-actions">
      <el-button type="primary" link size="small" @click="refresh">
        <el-icon><Refresh /></el-icon>
        换一张
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, DArrowRight, Check, Refresh } from '@element-plus/icons-vue'
import { captchaApi } from '@/api/captcha'

const props = defineProps({
  captchaId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:captchaId', 'verify'])

const containerRef = ref(null)
const bgImageRef = ref(null)
const backgroundImage = ref('')
const sliderImage = ref('')
const currentCaptchaId = ref('')
const sliderOffset = ref(0)
const isDragging = ref(false)
const startX = ref(0)
const verified = ref(false)
const loading = ref(false)
const sliderY = ref(0)

const sliderBlockStyle = computed(() => ({
  top: sliderY.value + 'px',
  left: sliderOffset.value + 'px',
  width: '60px',
  height: '60px'
}))

const generateCaptcha = async () => {
  loading.value = true
  verified.value = false
  sliderOffset.value = 0
  try {
    const res = await captchaApi.generate('SLIDE')
    backgroundImage.value = res.data.backgroundImage
    sliderImage.value = res.data.sliderImage
    currentCaptchaId.value = res.data.captchaId
    sliderY.value = res.data.sliderY || 20
    emit('update:captchaId', res.data.captchaId)
  } catch (e) {
    console.error('生成验证码失败:', e)
  } finally {
    loading.value = false
  }
}

const startDrag = (e) => {
  if (verified.value || loading.value) return
  
  isDragging.value = true
  startX.value = e.type === 'touchstart' 
    ? e.touches[0].clientX - sliderOffset.value 
    : e.clientX - sliderOffset.value
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', endDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', endDrag)
}

const onDrag = (e) => {
  if (!isDragging.value) return
  
  const clientX = e.type === 'touchmove' ? e.touches[0].clientX : e.clientX
  let offset = clientX - startX.value
  
  const maxWidth = containerRef.value ? containerRef.value.offsetWidth - 60 : 240
  offset = Math.max(0, Math.min(offset, maxWidth))
  
  sliderOffset.value = offset
}

const endDrag = () => {
  if (!isDragging.value) return
  
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', endDrag)
  
  emit('verify', {
    captchaId: currentCaptchaId.value,
    sliderX: Math.round(sliderOffset.value)
  })
}

const refresh = () => {
  if (!loading.value) {
    generateCaptcha()
  }
}

const setVerified = (success) => {
  verified.value = success
}

const reset = () => {
  generateCaptcha()
}

onMounted(() => {
  generateCaptcha()
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', endDrag)
})

defineExpose({
  setVerified,
  reset,
  captchaId: currentCaptchaId
})
</script>

<style lang="scss" scoped>
.slide-captcha {
  .captcha-container {
    width: 100%;
    height: 150px;
    border-radius: 8px;
    overflow: hidden;
    background: #f5f7fa;
    border: 1px solid #e4e7ed;
    position: relative;

    .background-image {
      width: 100%;
      height: 100%;
      position: relative;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }

      .slider-block {
        position: absolute;
        width: 60px;
        height: 60px;
        cursor: pointer;
        filter: drop-shadow(2px 2px 4px rgba(0, 0, 0, 0.3));
        transition: filter 0.2s;
        
        &:hover {
          filter: drop-shadow(3px 3px 6px rgba(0, 0, 0, 0.4));
        }
      }
    }

    .loading-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #909399;
      font-size: 14px;
    }
  }

  .slider-track {
    width: 100%;
    height: 40px;
    background: #f5f7fa;
    border-radius: 4px;
    margin-top: 12px;
    position: relative;
    border: 1px solid #e4e7ed;

    .slider-track-bg {
      height: 100%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 4px 0 0 4px;
      transition: width 0.1s;
    }

    .slider-button {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      width: 40px;
      height: 40px;
      background: #fff;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      color: #666;
      transition: all 0.2s;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

      &:hover {
        background: #667eea;
        color: #fff;
        border-color: #667eea;
      }

      .success {
        color: #67c23a;
      }
    }

    .slider-hint {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      color: #909399;
      font-size: 13px;
      pointer-events: none;
    }
  }

  .captcha-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 8px;
  }
}
</style>
