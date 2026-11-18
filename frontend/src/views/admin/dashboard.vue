<template>
  <div class="dashboard-page">
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(196, 30, 58, 0.1)">
          <el-icon :size="28" color="#C41E3A"><Goods /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.productCount }}</div>
          <div class="stat-label">商品总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(64, 158, 255, 0.1)">
          <el-icon :size="28" color="#409EFF"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.orderCount }}</div>
          <div class="stat-label">订单总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(103, 194, 58, 0.1)">
          <el-icon :size="28" color="#67C23A"><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(230, 162, 60, 0.1)">
          <el-icon :size="28" color="#E6A23C"><Money /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">¥{{ stats.totalSales }}</div>
          <div class="stat-label">销售总额</div>
        </div>
      </div>
    </div>

    <div class="content-row">
      <div class="chart-card">
        <div class="card-header">
          <h3>订单状态分布</h3>
        </div>
        <div class="card-body">
          <div class="order-status-list">
            <div class="status-item">
              <span class="status-label">待付款</span>
              <el-progress
                :percentage="getPercentage(stats.pendingOrders, stats.orderCount)"
                :stroke-width="12"
                color="#C41E3A"
              />
              <span class="status-count">{{ stats.pendingOrders }}</span>
            </div>
            <div class="status-item">
              <span class="status-label">待发货</span>
              <el-progress
                :percentage="getPercentage(stats.paidOrders, stats.orderCount)"
                :stroke-width="12"
                color="#E6A23C"
              />
              <span class="status-count">{{ stats.paidOrders }}</span>
            </div>
            <div class="status-item">
              <span class="status-label">待收货</span>
              <el-progress
                :percentage="getPercentage(stats.shippedOrders, stats.orderCount)"
                :stroke-width="12"
                color="#409EFF"
              />
              <span class="status-count">{{ stats.shippedOrders }}</span>
            </div>
            <div class="status-item">
              <span class="status-label">已完成</span>
              <el-progress
                :percentage="getPercentage(stats.completedOrders, stats.orderCount)"
                :stroke-width="12"
                color="#67C23A"
              />
              <span class="status-count">{{ stats.completedOrders }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="recent-card">
        <div class="card-header">
          <h3>最新订单</h3>
          <router-link to="/admin/orders">查看全部</router-link>
        </div>
        <div class="card-body">
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="orderSn" label="订单号" width="150" />
            <el-table-column label="收货人" width="80">
              <template #default="{ row }">
                {{ getReceiverName(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="payAmount" label="金额" width="100">
              <template #default="{ row }">
                <span class="price">¥{{ row.payAmount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <div class="card-header">
        <h3>快捷操作</h3>
      </div>
      <div class="action-list">
        <router-link to="/admin/products" class="action-item">
          <el-icon :size="24"><Goods /></el-icon>
          <span>商品管理</span>
        </router-link>
        <router-link to="/admin/orders" class="action-item">
          <el-icon :size="24"><Document /></el-icon>
          <span>订单管理</span>
        </router-link>
        <router-link to="/admin/users" class="action-item">
          <el-icon :size="24"><User /></el-icon>
          <span>用户管理</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Goods, Document, User, Money } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const stats = ref({
  productCount: 0,
  orderCount: 0,
  userCount: 0,
  totalSales: '0.00',
  pendingOrders: 0,
  paidOrders: 0,
  shippedOrders: 0,
  completedOrders: 0
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

const getStatusType = (status) => {
  const typeMap = {
    0: 'danger',
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'info'
  }
  return typeMap[status] || 'info'
}

const getPercentage = (value, total) => {
  if (total === 0) return 0
  return Math.round((value / total) * 100)
}

const getReceiverName = (order) => {
  if (order.shippingAddress) {
    try {
      const addr = JSON.parse(order.shippingAddress)
      return addr.name || '-'
    } catch (e) {
      return '-'
    }
  }
  return '-'
}

const fetchDashboardData = async () => {
  try {
    const res = await adminApi.getDashboardStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }

  try {
    const res = await adminApi.getOrderList({ page: 1, size: 5 })
    if (res.code === 200) {
      recentOrders.value = res.data.records || res.data.list || []
    }
  } catch (error) {
    console.error('获取订单列表失败', error)
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  padding: 0;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .stat-info {
    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #262626;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 14px;
      color: #8c8c8c;
    }
  }
}

.content-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card,
.recent-card,
.quick-actions {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;

  .card-header {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      font-size: 16px;
      font-weight: 500;
      color: #262626;
    }

    a {
      font-size: 14px;
      color: #C41E3A;
    }
  }

  .card-body {
    padding: 20px;
  }
}

.order-status-list {
  .status-item {
    display: flex;
    align-items: center;
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }

    .status-label {
      width: 60px;
      font-size: 14px;
      color: #595959;
    }

    .el-progress {
      flex: 1;
      margin: 0 12px;
    }

    .status-count {
      width: 40px;
      text-align: right;
      font-size: 14px;
      color: #262626;
    }
  }
}

.price {
  color: #C41E3A;
  font-weight: 500;
}

.action-list {
  display: flex;
  gap: 20px;
  padding: 20px;
}

.action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
  color: #595959;
  transition: all 0.3s;

  &:hover {
    background: rgba(196, 30, 58, 0.05);
    color: #C41E3A;
  }

  .el-icon {
    margin-bottom: 8px;
  }

  span {
    font-size: 14px;
  }
}
</style>
