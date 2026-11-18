<template>
  <div class="user-center-page">
    <div class="container">
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/user' }">个人中心</el-breadcrumb-item>
          <el-breadcrumb-item>我的订单</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="user-content">
        <aside class="user-sidebar">
          <div class="user-info">
            <el-avatar :size="64" :src="userInfo.avatar">
              {{ userInfo.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="user-name">{{ userInfo.username }}</div>
            <div class="user-level">普通会员</div>
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
          <div class="page-title">
            <h2>我的订单</h2>
          </div>

          <div class="order-tabs">
            <el-radio-group v-model="currentStatus" @change="handleTabChange">
              <el-radio-button value="">全部订单</el-radio-button>
              <el-radio-button value="0">待付款</el-radio-button>
              <el-radio-button value="1">待发货</el-radio-button>
              <el-radio-button value="2">待收货</el-radio-button>
              <el-radio-button value="3">已完成</el-radio-button>
            </el-radio-group>
          </div>

          <div class="order-list" v-loading="loading">
            <div class="empty-tip" v-if="orderList.length === 0">
              <el-empty description="暂无订单" />
            </div>
            <div class="order-item" v-for="order in orderList" :key="order.id">
              <div class="order-header">
                <span class="order-no">订单号：{{ order.orderSn }}</span>
                <span class="order-time">{{ order.createTime }}</span>
                <span class="order-status" :class="'status-' + order.status">
                  {{ getStatusText(order.status) }}
                </span>
              </div>
              <div class="order-body">
                <div class="product-list">
                  <div class="product-item" v-for="item in order.orderItems" :key="item.id">
                    <img :src="item.productImage || defaultImage" :alt="item.productName" />
                    <div class="product-info">
                      <div class="product-name">{{ item.productName }}</div>
                      <div class="product-meta">
                        <span class="price">¥{{ item.productPrice }}</span>
                        <span class="quantity">x{{ item.quantity }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="order-summary">
                  <div class="total-amount">
                    共{{ order.orderItems?.length || 0 }}件商品
                    <br />
                    实付：<em>¥{{ order.payAmount }}</em>
                  </div>
                </div>
              </div>
              <div class="order-footer">
                <el-button size="small" @click="viewOrder(order.orderSn)">查看详情</el-button>
                <el-button
                  v-if="order.status === 0"
                  type="primary"
                  size="small"
                  @click="goPay(order.orderSn)"
                >
                  立即付款
                </el-button>
                <el-button
                  v-if="order.status === 2"
                  type="primary"
                  size="small"
                  @click="confirmReceive(order.orderSn)"
                >
                  确认收货
                </el-button>
                <el-button
                  v-if="order.status === 0"
                  size="small"
                  @click="cancelOrder(order.orderSn)"
                >
                  取消订单
                </el-button>
              </div>
            </div>
          </div>

          <div class="pagination" v-if="total > pageSize">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="fetchOrders"
            />
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Edit, List, Location } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { orderApi } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%23f5f5f5" width="100" height="100"/%3E%3Ctext x="50" y="50" text-anchor="middle" dy=".3em" fill="%23ccc" font-size="14"%3E暂无图片%3C/text%3E%3C/svg%3E'

const activeMenu = computed(() => '/user/orders')
const userInfo = computed(() => userStore.userInfo)

const loading = ref(false)
const currentStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orderList = ref([])

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

const handleTabChange = () => {
  currentPage.value = 1
  fetchOrders()
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (currentStatus.value !== '') {
      params.status = currentStatus.value
    }
    const res = await orderApi.getOrderList(params)
    if (res.code === 200) {
      orderList.value = res.data.records || res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const viewOrder = (orderSn) => {
  router.push(`/user/order/${orderSn}`)
}

const goPay = (orderSn) => {
  router.push(`/order/pay/${orderSn}`)
}

const cancelOrder = async (orderSn) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await orderApi.cancelByNo(orderSn)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

const confirmReceive = async (orderSn) => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await orderApi.receiveByNo(orderSn)
    if (res.code === 200) {
      ElMessage.success('已确认收货')
      fetchOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认收货失败')
    }
  }
}

onMounted(() => {
  if (route.query.status) {
    currentStatus.value = route.query.status
  }
  fetchOrders()
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

.page-title {
  margin-bottom: 20px;

  h2 {
    font-size: 18px;
    color: #262626;
  }
}

.order-tabs {
  margin-bottom: 20px;
}

.empty-tip {
  padding: 40px 0;
}

.order-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 16px;
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

  .order-body {
    padding: 16px;
    display: flex;
    justify-content: space-between;

    .product-list {
      flex: 1;

      .product-item {
        display: flex;
        margin-bottom: 12px;

        &:last-child {
          margin-bottom: 0;
        }

        img {
          width: 80px;
          height: 80px;
          object-fit: cover;
          border-radius: 4px;
          margin-right: 12px;
        }

        .product-info {
          flex: 1;

          .product-name {
            font-size: 14px;
            color: #262626;
            margin-bottom: 8px;
            line-height: 1.4;
          }

          .product-meta {
            .price {
              color: #C41E3A;
              margin-right: 12px;
            }

            .quantity {
              color: #8c8c8c;
            }
          }
        }
      }
    }

    .order-summary {
      width: 150px;
      text-align: right;

      .total-amount {
        font-size: 14px;
        color: #595959;
        line-height: 1.8;

        em {
          font-style: normal;
          font-size: 18px;
          font-weight: 600;
          color: #C41E3A;
        }
      }
    }
  }

  .order-footer {
    padding: 12px 16px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
