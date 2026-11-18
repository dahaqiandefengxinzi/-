<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <router-link to="/admin" class="logo">
          <span class="logo-text">ShopLab</span>
          <span class="logo-sub">管理后台</span>
        </router-link>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
      >
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/categories">
          <el-icon><Grid /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/banners">
          <el-icon><Picture /></el-icon>
          <span>Banner管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/captcha">
          <el-icon><Key /></el-icon>
          <span>验证码配置</span>
        </el-menu-item>
      </el-menu>
    </aside>
    
    <div class="main-container">
      <header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32">
                {{ userInfo?.nickname?.charAt(0) || userInfo?.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">
                  <el-icon><House /></el-icon>
                  返回前台
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  DataAnalysis, 
  Goods, 
  Grid,
  Picture,
  List, 
  User, 
  ArrowDown, 
  House, 
  SwitchButton,
  Key
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const userInfo = computed(() => userStore.userInfo)

const handleCommand = (command) => {
  switch (command) {
    case 'home':
      router.push('/')
      break
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  background: #262626;
  flex-shrink: 0;
  
  .sidebar-header {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid #363636;
    
    .logo {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .logo-text {
        font-size: 20px;
        font-weight: bold;
        color: #C41E3A;
      }
      
      .logo-sub {
        font-size: 12px;
        color: #8c8c8c;
      }
    }
  }
  
  .sidebar-menu {
    border-right: none;
    background: transparent;
    
    :deep(.el-menu-item) {
      color: #bfbfbf;
      
      &:hover {
        background: #363636;
        color: #fff;
      }
      
      &.is-active {
        background: #C41E3A;
        color: #fff;
      }
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.admin-header {
  height: 64px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  
  .header-right {
    .user-dropdown {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      
      .username {
        max-width: 80px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
