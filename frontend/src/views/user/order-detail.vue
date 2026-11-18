<template>
  <div class="user-center-page">
    <div class="container">
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/user' }">个人中心</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/user/orders' }">我的订单</el-breadcrumb-item>
          <el-breadcrumb-item>订单详情</el-breadcrumb-item>
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

        <main class="user-main" v-loading="loading">
          <div class="page-title">
            <h2>订单详情</h2>
          </div>

          <div class="order-status-card">
            <div class="status-icon" :class="'status-' + orderInfo.status">
              <el-icon :size="48">
                <component :is="getStatusIcon(orderInfo.status)" />
              </el-icon>
            </div>
            <div class="status-info">
              <div class="status-text">{{ getStatusText(orderInfo.status) }}</div>
              <div class="status-desc">{{ getStatusDesc(orderInfo.status) }}</div>
            </div>
            <div class="status-actions">
              <el-button
                v-if="orderInfo.status === 0"
                type="primary"
                @click="goPay"
              >
                立即付款
              </el-button>
              <el-button
                v-if="orderInfo.status === 0"
                @click="cancelOrder"
              >
                取消订单
              </el-button>
              <el-button
                v-if="orderInfo.status === 2"
                type="primary"
                @click="confirmReceive"
              >
                确认收货
              </el-button>
            </div>
          </div>

          <div class="order-progress" v-if="orderInfo.status >= 1 && orderInfo.status <= 3">
            <el-steps :active="orderInfo.status" align-center>
              <el-step title="提交订单" :description="orderInfo.createTime" />
              <el-step title="付款成功" :description="orderInfo.payTime" />
              <el-step title="商品发货" :description="orderInfo.deliveryTime" />
              <el-step title="确认收货" :description="orderInfo.receiveTime" />
            </el-steps>
          </div>

          <div class="info-sections">
            <div class="info-section">
              <h3>收货信息</h3>
              <div class="info-content">
                <div class="info-row">
                  <span class="label">收货人：</span>
                  <span class="value">{{ orderInfo.receiverName }}</span>
                </div>
                <div class="info-row">
                  <span class="label">联系电话：</span>
                  <span class="value">{{ orderInfo.receiverPhone }}</span>
                </div>
                <div class="info-row">
                  <span class="label">收货地址：</span>
                  <span class="value">{{ orderInfo.receiverAddress }}</span>
                </div>
              </div>
            </div>

            <div class="info-section">
              <h3>订单信息</h3>
              <div class="info-content">
                <div class="info-row">
                  <span class="label">订单编号：</span>
                  <span class="value">{{ orderInfo.orderNo }}</span>
                </div>
                <div class="info-row">
                  <span class="label">下单时间：</span>
                  <span class="value">{{ orderInfo.createTime }}</span>
                </div>
                <div class="info-row" v-if="orderInfo.payTime">
                  <span class="label">付款时间：</span>
                  <span class="value">{{ orderInfo.payTime }}</span>
                </div>
                <div class="info-row" v-if="orderInfo.payMethod">
                  <span class="label">支付方式：</span>
                  <span class="value">{{ orderInfo.payMethod === 'alipay' ? '支付宝' : '微信支付' }}</span>
                </div>
              </div>
            </div>

            <div class="info-section">
              <h3>商品清单</h3>
              <div class="product-list">
                <div class="product-item" v-for="item in orderInfo.items" :key="item.id">
                  <img :src="item.productImage || defaultImage" :alt="item.productName" />
                  <div class="product-info">
                    <div class="product-name">{{ item.productName }}</div>
                    <div class="product-spec" v-if="item.spec">{{ item.spec }}</div>
                  </div>
                  <div class="product-price">¥{{ item.price }}</div>
                  <div class="product-quantity">x{{ item.quantity }}</div>
                  <div class="product-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                </div>
              </div>
            </div>

            <div class="info-section amount-section">
              <div class="amount-row">
                <span class="label">商品总额：</span>
                <span class="value">¥{{ orderInfo.totalAmount }}</span>
              </div>
              <div class="amount-row">
                <span class="label">运费：</span>
                <span class="value">¥{{ orderInfo.freight || '0.00' }}</span>
              </div>
              <div class="amount-row total">
                <span class="label">实付金额：</span>
                <span class="value">¥{{ orderInfo.payAmount }}</span>
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Edit, List, Location, Clock, CircleCheck, Van, CircleClose } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { orderApi } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%23f5f5f5" width="100" height="100"/%3E%3Ctext x="50" y="50" text-anchor="middle" dy=".3em" fill="%23ccc" font-size="14"%3E暂无图片%3C/text%3E%3C/svg%3E'

const activeMenu = computed(() => '/user/orders')
const userInfo = computed(() => userStore.userInfo)

const loading = ref(false)
const orderInfo = ref({
  orderNo: '',
  status: 0,
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  totalAmount: '0.00',
  freight: '0.00',
  payAmount: '0.00',
  createTime: '',
  payTime: '',
  deliveryTime: '',
  receiveTime: '',
  payMethod: '',
  items: []
})

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

const getStatusDesc = (status) => {
  const descMap = {
    0: '请在30分钟内完成付款',
    1: '商家正在准备发货',
    2: '商品正在配送中',
    3: '订单已完成',
    4: '订单已取消'
  }
  return descMap[status] || ''
}

const getStatusIcon = (status) => {
  const iconMap = {
    0: Clock,
    1: CircleCheck,
    2: Van,
    3: CircleCheck,
    4: CircleClose
  }
  return iconMap[status] || Clock
}

const fetchOrderDetail = async () => {
  const orderNo = route.params.orderNo
  if (!orderNo) {
    ElMessage.error('订单号不存在')
    router.push('/user/orders')
    return
  }

  loading.value = true
  try {
    const res = await orderApi.getOrderByNo(orderNo)
    if (res.code === 200) {
      const order = res.data
      const shippingInfo = parseShippingAddress(order.shippingAddress)
      orderInfo.value = {
        orderNo: order.orderSn,
        status: order.status,
        receiverName: shippingInfo.receiverName || '',
        receiverPhone: shippingInfo.receiverPhone || '',
        receiverAddress: shippingInfo.address || '',
        totalAmount: order.totalAmount,
        freight: '0.00',
        payAmount: order.payAmount,
        createTime: order.createTime,
        payTime: order.payTime || '',
        deliveryTime: order.deliveryTime || '',
        receiveTime: order.receiveTime || '',
        payMethod: order.paymentType === 1 ? 'alipay' : 'wechat',
        items: (order.orderItems || []).map(item => ({
          id: item.id,
          productName: item.productName,
          productImage: item.productImage,
          price: item.productPrice,
          quantity: item.quantity,
          spec: ''
        }))
      }
    }
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

const parseShippingAddress = (addressStr) => {
  if (!addressStr) return {}
  try {
    const addr = JSON.parse(addressStr)
    const fullAddress = [addr.province, addr.city, addr.district, addr.detailAddress].filter(Boolean).join('')
    return {
      receiverName: addr.name || '',
      receiverPhone: addr.phone || '',
      address: fullAddress || ''
    }
  } catch {
    return {}
  }
}

const goPay = () => {
  router.push(`/order/pay/${orderInfo.value.orderNo}`)
}

const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await orderApi.cancelByNo(orderInfo.value.orderNo)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrderDetail()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

const confirmReceive = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await orderApi.receiveByNo(orderInfo.value.orderNo)
    if (res.code === 200) {
      ElMessage.success('已确认收货')
      fetchOrderDetail()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认收货失败')
    }
  }
}

onMounted(() => {
  fetchOrderDetail()
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

.order-status-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 24px;

  .status-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20px;

    &.status-0 { background: rgba(196, 30, 58, 0.1); color: #C41E3A; }
    &.status-1 { background: rgba(230, 162, 60, 0.1); color: #E6A23C; }
    &.status-2 { background: rgba(64, 158, 255, 0.1); color: #409EFF; }
    &.status-3 { background: rgba(103, 194, 58, 0.1); color: #67C23A; }
    &.status-4 { background: rgba(140, 140, 140, 0.1); color: #8c8c8c; }
  }

  .status-info {
    flex: 1;

    .status-text {
      font-size: 20px;
      font-weight: 600;
      color: #262626;
      margin-bottom: 4px;
    }

    .status-desc {
      font-size: 14px;
      color: #8c8c8c;
    }
  }

  .status-actions {
    display: flex;
    gap: 12px;
  }
}

.order-progress {
  margin-bottom: 24px;
  padding: 20px 0;
}

.info-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-section {
  h3 {
    font-size: 16px;
    color: #262626;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;
  }

  .info-content {
    .info-row {
      display: flex;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        width: 80px;
        color: #8c8c8c;
        font-size: 14px;
      }

      .value {
        flex: 1;
        color: #262626;
        font-size: 14px;
      }
    }
  }
}

.product-list {
  .product-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
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
        margin-bottom: 4px;
      }

      .product-spec {
        font-size: 12px;
        color: #8c8c8c;
      }
    }

    .product-price,
    .product-quantity,
    .product-subtotal {
      width: 100px;
      text-align: center;
      font-size: 14px;
      color: #595959;
    }

    .product-subtotal {
      color: #C41E3A;
      font-weight: 500;
    }
  }
}

.amount-section {
  background: #fafafa;
  border-radius: 8px;
  padding: 20px;

  .amount-row {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 12px;
    font-size: 14px;

    &:last-child {
      margin-bottom: 0;
    }

    .label {
      color: #8c8c8c;
      margin-right: 8px;
    }

    .value {
      min-width: 80px;
      text-align: right;
    }

    &.total {
      padding-top: 12px;
      border-top: 1px solid #e8e8e8;

      .label {
        font-weight: 500;
        color: #262626;
      }

      .value {
        font-size: 20px;
        font-weight: 600;
        color: #C41E3A;
      }
    }
  }
}
</style>
