<template>
  <div class="captcha-config-page">
    <div class="page-header">
      <h2>验证码配置</h2>
    </div>

    <el-card class="config-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>基础设置</span>
          <el-tag :type="config.enabled ? 'success' : 'info'">
            {{ config.enabled ? '已开启' : '已关闭' }}
          </el-tag>
        </div>
      </template>

      <el-form :model="config" label-width="140px" class="config-form">
        <el-form-item label="启用验证码">
          <el-switch
            v-model="config.enabled"
            active-text="开启"
            inactive-text="关闭"
          />
          <div class="form-tip">开启后，用户登录时需要完成验证码验证</div>
        </el-form-item>

        <el-form-item label="验证码类型" v-if="config.enabled">
          <el-radio-group v-model="config.captchaType">
            <el-radio value="GRAPH">图形验证码</el-radio>
            <el-radio value="SLIDE">滑动拼图</el-radio>
            <el-radio value="CLICK_TEXT">文字点选</el-radio>
            <el-radio value="CALC">计算题</el-radio>
          </el-radio-group>
          <div class="form-tip">
            <template v-if="config.captchaType === 'GRAPH'">
              传统图形验证码，用户输入图片中的字符
            </template>
            <template v-else-if="config.captchaType === 'SLIDE'">
              滑动拼图验证，用户拖动滑块完成拼图
            </template>
            <template v-else-if="config.captchaType === 'CLICK_TEXT'">
              文字点选验证，用户按顺序点击指定文字
            </template>
            <template v-else-if="config.captchaType === 'CALC'">
              计算题验证码，用户输入计算结果
            </template>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="config-card" v-if="config.enabled">
      <template #header>
        <span>安全策略</span>
      </template>

      <el-form :model="config" label-width="140px" class="config-form">
        <el-form-item label="验证码有效期">
          <el-input-number
            v-model="config.expireTime"
            :min="60"
            :max="1800"
            :step="60"
          />
          <span class="unit">秒</span>
          <div class="form-tip">验证码生成后的有效时间，建议300秒（5分钟）</div>
        </el-form-item>

        <el-form-item label="最大错误次数">
          <el-input-number
            v-model="config.maxRetryCount"
            :min="1"
            :max="20"
          />
          <span class="unit">次</span>
          <div class="form-tip">验证失败超过此次数后锁定，建议5次</div>
        </el-form-item>

        <el-form-item label="锁定时间">
          <el-input-number
            v-model="config.lockTime"
            :min="60"
            :max="3600"
            :step="60"
          />
          <span class="unit">秒</span>
          <div class="form-tip">错误次数超限后的锁定时间，建议300秒（5分钟）</div>
        </el-form-item>

        <el-form-item label="刷新间隔">
          <el-input-number
            v-model="config.refreshInterval"
            :min="10"
            :max="300"
            :step="10"
          />
          <span class="unit">秒</span>
          <div class="form-tip">两次获取验证码的最小间隔，建议60秒</div>
        </el-form-item>

        <el-form-item label="IP限流次数">
          <el-input-number
            v-model="config.ipLimitCount"
            :min="5"
            :max="100"
            :step="5"
          />
          <span class="unit">次/小时</span>
          <div class="form-tip">同一IP每小时最大验证次数，建议10次</div>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="config-card" v-if="config.enabled && configSaved">
      <template #header>
        <span>效果预览</span>
      </template>

      <div class="preview-container">
        <div class="preview-label">当前验证码类型预览：</div>
        <div class="preview-content">
          <GraphCaptcha v-if="config.captchaType === 'GRAPH'" ref="previewRef" />
          <SlideCaptcha v-else-if="config.captchaType === 'SLIDE'" ref="previewRef" />
          <ClickTextCaptcha v-else-if="config.captchaType === 'CLICK_TEXT'" ref="previewRef" />
          <CalcCaptcha v-else-if="config.captchaType === 'CALC'" ref="previewRef" />
        </div>
      </div>
    </el-card>

    <el-card class="config-card" v-else-if="config.enabled && !configSaved">
      <template #header>
        <span>效果预览</span>
      </template>
      <div class="preview-tip">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>请先保存配置后再预览验证码效果</template>
        </el-alert>
      </div>
    </el-card>

    <div class="action-bar">
      <el-button @click="fetchConfig">重置</el-button>
      <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { captchaApi } from '@/api/captcha'
import GraphCaptcha from '@/components/Captcha/GraphCaptcha.vue'
import SlideCaptcha from '@/components/Captcha/SlideCaptcha.vue'
import ClickTextCaptcha from '@/components/Captcha/ClickTextCaptcha.vue'
import CalcCaptcha from '@/components/Captcha/CalcCaptcha.vue'

const loading = ref(false)
const saving = ref(false)
const previewRef = ref(null)
const configSaved = ref(false)

const config = reactive({
  enabled: false,
  captchaType: 'GRAPH',
  expireTime: 300,
  maxRetryCount: 5,
  lockTime: 300,
  refreshInterval: 60,
  ipLimitCount: 10
})

const fetchConfig = async () => {
  loading.value = true
  try {
    const res = await captchaApi.getConfig()
    if (res.data) {
      Object.assign(config, res.data)
      // 将 Integer 类型的 enabled 转换为 Boolean
      config.enabled = !!res.data.enabled
      configSaved.value = config.enabled
    }
  } catch (e) {
    console.error('获取配置失败:', e)
    ElMessage.error('获取配置失败')
  } finally {
    loading.value = false
  }
}

const saveConfig = async () => {
  saving.value = true
  try {
    await captchaApi.updateConfig(config)
    configSaved.value = true
    ElMessage.success('保存成功')
  } catch (e) {
    console.error('保存配置失败:', e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchConfig()
})
</script>

<style lang="scss" scoped>
.captcha-config-page {
  .page-header {
    margin-bottom: 20px;

    h2 {
      font-size: 18px;
      color: #262626;
    }
  }

  .config-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
  }

  .config-form {
    .unit {
      margin-left: 8px;
      color: #606266;
    }

    .form-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }

  .preview-container {
    .preview-label {
      font-size: 14px;
      color: #606266;
      margin-bottom: 16px;
    }

    .preview-content {
      max-width: 320px;
    }
  }

  .preview-tip {
    padding: 20px 0;
  }

  .action-bar {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 20px;
  }
}
</style>
