<template>
  <div class="user-center-page">
    <div class="container">
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>个人中心</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="user-content">
        <aside class="user-sidebar">
          <div class="user-info">
            <el-avatar :size="64" :src="userInfo.avatar">
              {{ userInfo.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="user-name">{{ userInfo.username }}</div>
            <div class="user-level">{{ getRoleText }}</div>
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
          <div class="welcome-section">
            <h2>欢迎回来，{{ userInfo.username }}</h2>
            <p>上次登录时间：{{ userInfo.lastLoginTime || '暂无记录' }}</p>
          </div>

          <div class="order-stats">
            <div class="stat-item" @click="goToOrders('pending')">
              <div class="stat-count">{{ orderStats.pending }}</div>
              <div class="stat-label">待付款</div>
            </div>
            <div class="stat-item" @click="goToOrders('paid')">
              <div class="stat-count">{{ orderStats.paid }}</div>
              <div class="stat-label">待发货</div>
            </div>
            <div class="stat-item" @click="goToOrders('shipped')">
              <div class="stat-count">{{ orderStats.shipped }}</div>
              <div class="stat-label">待收货</div>
            </div>
            <div class="stat-item" @click="goToOrders('completed')">
              <div class="stat-count">{{ orderStats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>

          <div class="recent-orders">
            <div class="section-header">
              <h3>最近订单</h3>
              <router-link to="/user/orders">查看全部</router-link>
            </div>
            <div class="order-list" v-loading="loading">
              <div class="empty-tip" v-if="recentOrders.length === 0">
                暂无订单记录
              </div>
              <div class="order-item" v-for="order in recentOrders" :key="order.id">
                <div class="order-header">
                  <span class="order-no">订单号：{{ order.orderNo }}</span>
                  <span class="order-time">{{ order.createTime }}</span>
                  <span class="order-status" :class="'status-' + order.status">
                    {{ getStatusText(order.status) }}
                  </span>
                </div>
                <div class="order-products">
                  <div class="product-item" v-for="item in order.items?.slice(0, 3)" :key="item.id">
                    <img :src="item.productImage || defaultImage" :alt="item.productName" />
                  </div>
                  <span v-if="order.items?.length > 3" class="more">
                    +{{ order.items.length - 3 }}
                  </span>
                </div>
                <div class="order-footer">
                  <span class="order-amount">共{{ order.items?.length || 0 }}件商品，实付：<em>¥{{ order.payAmount }}</em></span>
                  <el-button type="primary" size="small" @click="viewOrder(order.orderNo)">
                    查看详情
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Edit, List, Location } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { orderApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%23f5f5f5" width="100" height="100"/%3E%3Ctext x="50" y="50" text-anchor="middle" dy=".3em" fill="%23ccc" font-size="14"%3E暂无图片%3C/text%3E%3C/svg%3E'

const loading = ref(false)
const activeMenu = computed(() => router.currentRoute.value.path)
const userInfo = computed(() => userStore.userInfo)

const orderStats = ref({
  pending: 0,
  paid: 0,
  shipped: 0,
  completed: 0
})

const recentOrders = ref([])

const getStatusText = (status) => {
  const statusMap = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

const getRoleText = computed(() => {
  if (userInfo.value?.role === 1) {
    return '管理员'
  }
  return '普通会员'
})

const goToOrders = (status) => {
  router.push({ path: '/user/orders', query: { status } })
}

const viewOrder = (orderNo) => {
  router.push(`/user/order/${orderNo}`)
}

const fetchOrderStats = async () => {
  try {
    const res = await orderApi.getOrderList({ page: 1, size: 100 })
    if (res.code === 200) {
      const orders = res.data.records || res.data.list || []
      orderStats.value = {
        pending: orders.filter(o => o.status === 0).length,
        paid: orders.filter(o => o.status === 1).length,
        shipped: orders.filter(o => o.status === 2).length,
        completed: orders.filter(o => o.status === 3).length
      }
      recentOrders.value = orders.slice(0, 5)
    }
  } catch (error) {
    console.error('获取订单统计失败', error)
  }
}

onMounted(() => {
  userStore.getUserInfo()
  fetchOrderStats()
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

.welcome-section {
  margin-bottom: 24px;

  h2 {
    font-size: 20px;
    color: #262626;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: #8c8c8c;
  }
}

.order-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;

  .stat-item {
    flex: 1;
    background: #fafafa;
    border-radius: 8px;
    padding: 20px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: rgba(196, 30, 58, 0.05);
    }

    .stat-count {
      font-size: 28px;
      font-weight: 600;
      color: #C41E3A;
      margin-bottom: 8px;
    }

    .stat-label {
      font-size: 14px;
      color: #595959;
    }
  }
}

.recent-orders {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      font-size: 16px;
      color: #262626;
    }

    a {
      font-size: 14px;
      color: #C41E3A;
    }
  }
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #8c8c8c;
}

.order-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;

  .order-header {
    background: #fafafa;
    padding: 12px 16px;
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 14px;

    .order-no {
      color: #595959;
    }

    .order-time {
      color: #8c8c8c;
    }

    .order-status {
      margin-left: auto;
      font-weight: 500;

      &.status-0 { color: #C41E3A; }
      &.status-1 { color: #E6A23C; }
      &.status-2 { color: #409EFF; }
      &.status-3 { color: #67C23A; }
      &.status-4 { color: #8c8c8c; }
    }
  }

  .order-products {
    padding: 16px;
    display: flex;
    align-items: center;
    gap: 8px;

    .product-item {
      width: 60px;
      height: 60px;
      border-radius: 4px;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .more {
      font-size: 14px;
      color: #8c8c8c;
    }
  }

  .order-footer {
    padding: 12px 16px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .order-amount {
      font-size: 14px;
      color: #595959;

      em {
        font-style: normal;
        font-size: 16px;
        font-weight: 600;
        color: #C41E3A;
      }
    }
  }
}
</style>
