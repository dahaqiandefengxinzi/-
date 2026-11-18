<template>
  <div class="main-layout">
    <header class="header">
      <div class="header-container">
        <div class="header-left">
          <router-link to="/" class="logo">
            <span class="logo-text">ShopLab</span>
          </router-link>
        </div>
        
        <div class="header-center">
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品"
              @keyup.enter="handleSearch"
              clearable
            >
              <template #append>
                <el-button :icon="Search" @click="handleSearch" />
              </template>
            </el-input>
          </div>
        </div>
        
        <div class="header-right">
          <router-link to="/cart" class="nav-item cart-item">
            <el-badge :value="cartCount" :hidden="!cartCount">
              <el-icon :size="22"><ShoppingCart /></el-icon>
            </el-badge>
            <span>购物车</span>
          </router-link>
          
          <template v-if="isLoggedIn">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar :size="32" :src="userInfo?.avatar">
                  {{ userInfo?.nickname?.charAt(0) || userInfo?.username?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="user">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <el-icon><List /></el-icon>
                    我的订单
                  </el-dropdown-item>
                  <el-dropdown-item command="address">
                    <el-icon><Location /></el-icon>
                    收货地址
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" command="admin">
                    <el-icon><Setting /></el-icon>
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-item">登录</router-link>
            <router-link to="/register" class="nav-item register-btn">注册</router-link>
          </template>
        </div>
      </div>
    </header>
    
    <main class="main-content">
      <router-view />
    </main>
    
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-links">
          <a href="https://testfather.cn" target="_blank">关于我们</a>
          <a href="https://testfather.cn" target="_blank">联系我们</a>
          <a href="https://testfather.cn" target="_blank">帮助中心</a>
          <a href="https://testfather.cn" target="_blank">隐私政策</a>
        </div>
        <div class="footer-copyright">
          © 2026 ShopLab 电商平台 版权归「狂师.AI进化社」所有
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { 
  Search, 
  ShoppingCart, 
  ArrowDown, 
  User, 
  List, 
  Location,
  SwitchButton,
  Setting 
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)
const cartCount = computed(() => cartStore.totalCount)
const isAdmin = computed(() => userStore.isAdmin)

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Search', query: { q: searchKeyword.value.trim() } })
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'user':
      router.push('/user')
      break
    case 'orders':
      router.push('/user/orders')
      break
    case 'address':
      router.push('/user/address')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}

onMounted(() => {
  if (isLoggedIn.value) {
    cartStore.getCartList()
  }
})
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
  
  .header-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  
  .header-left {
    .logo {
      display: flex;
      align-items: center;
      
      .logo-text {
        font-size: 24px;
        font-weight: bold;
        color: #C41E3A;
      }
    }
  }
  
  .header-center {
    flex: 1;
    max-width: 500px;
    margin: 0 40px;
    
    .search-box {
      :deep(.el-input__wrapper) {
        border-radius: 20px 0 0 20px;
      }
      
      :deep(.el-input-group__append) {
        border-radius: 0 20px 20px 0;
        background: #C41E3A;
        border-color: #C41E3A;
        
        .el-button {
          color: #fff;
        }
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .nav-item {
      color: #262626;
      font-size: 14px;
      transition: color 0.3s;
      
      &:hover {
        color: #C41E3A;
      }
      
      &.cart-item {
        display: flex;
        align-items: center;
        gap: 4px;
      }
      
      &.register-btn {
        padding: 6px 16px;
        background: #C41E3A;
        color: #fff;
        border-radius: 4px;
        
        &:hover {
          background: #E8505B;
          color: #fff;
        }
      }
    }
    
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

.main-content {
  flex: 1;
  background: #f5f5f5;
}

.footer {
  background: #262626;
  color: #fff;
  padding: 30px 0;
  
  .footer-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    text-align: center;
  }
  
  .footer-links {
    margin-bottom: 16px;
    
    a {
      color: #bfbfbf;
      margin: 0 20px;
      font-size: 14px;
      
      &:hover {
        color: #fff;
      }
    }
  }
  
  .footer-copyright {
    color: #8c8c8c;
    font-size: 12px;
  }
}
</style>
