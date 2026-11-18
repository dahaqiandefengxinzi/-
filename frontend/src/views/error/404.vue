<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-message">抱歉，您访问的页面不存在</div>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
.error-page {
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.error-content {
  text-align: center;
  padding: 40px;

  .error-code {
    font-size: 120px;
    font-weight: 700;
    color: #C41E3A;
    line-height: 1;
    margin-bottom: 20px;
    text-shadow: 2px 2px 4px rgba(196, 30, 58, 0.2);
  }

  .error-message {
    font-size: 24px;
    color: #595959;
    margin-bottom: 40px;
  }

  .error-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
  }
}
</style>
