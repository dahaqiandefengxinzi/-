<template>
  <div class="pay-page">
    <div class="container">
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/user/orders' }">我的订单</el-breadcrumb-item>
          <el-breadcrumb-item>订单支付</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="pay-content" v-loading="loading">
        <div class="order-info-card">
          <div class="card-header">
            <span class="title">订单信息</span>
            <span class="order-no">订单号：{{ orderInfo.orderNo }}</span>
          </div>
          <div class="card-body">
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

        <div class="product-list-card">
          <div class="card-header">
            <span class="title">商品清单</span>
          </div>
          <div class="card-body">
            <div class="product-item" v-for="item in orderInfo.items" :key="item.id">
              <img :src="item.productImage || defaultImage" :alt="item.productName" class="product-image" />
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

        <div class="pay-amount-card">
          <div class="amount-row">
            <span class="label">商品总额：</span>
            <span class="value">¥{{ orderInfo.totalAmount }}</span>
          </div>
          <div class="amount-row">
            <span class="label">运费：</span>
            <span class="value">¥{{ orderInfo.freight || '0.00' }}</span>
          </div>
          <div class="amount-row total">
            <span class="label">应付金额：</span>
            <span class="value price">¥{{ orderInfo.payAmount }}</span>
          </div>
        </div>

        <div class="pay-method-card">
          <div class="card-header">
            <span class="title">选择支付方式</span>
          </div>
          <div class="card-body">
            <div class="pay-methods">
              <div
                class="pay-method-item"
                :class="{ active: payMethod === 'alipay' }"
                @click="payMethod = 'alipay'"
              >
                <div class="method-icon alipay">
                  <svg viewBox="0 0 24 24" width="32" height="32">
                    <path fill="#1677FF" d="M21.422 15.358c-3.83-1.153-6.055-1.84-7.373-2.313.532-.983.968-2.062 1.29-3.213h-3.84V8.4h4.736V7.36H10.5V5.28H9.12c-.24 0-.384.192-.384.384V7.36H4.8v1.04h3.936v1.432H4.8v1.04h6.624c-.264.816-.6 1.584-1.008 2.28-1.824-.384-3.6-.48-5.136-.24-2.88.448-4.032 2.016-3.792 3.648.24 1.632 1.824 2.64 4.08 2.4 2.256-.24 4.128-1.44 5.52-3.312 2.112.72 5.28 1.776 9.12 3.024.96.288 1.824-.528 1.824-1.488v-.384c0-.72-.48-1.344-1.152-1.536l-.168-.048zM6.24 18.8c-1.44.144-2.448-.384-2.592-1.296-.144-.912.576-1.68 1.968-1.92 1.152-.192 2.496-.096 4.032.336-.96 1.44-2.16 2.64-3.408 2.88z"/>
                  </svg>
                </div>
                <span class="method-name">支付宝</span>
                <div class="check-icon" v-if="payMethod === 'alipay'">
                  <el-icon><Check /></el-icon>
                </div>
              </div>
              <div
                class="pay-method-item"
                :class="{ active: payMethod === 'wechat' }"
                @click="payMethod = 'wechat'"
              >
                <div class="method-icon wechat">
                  <svg viewBox="0 0 24 24" width="32" height="32">
                    <path fill="#07C160" d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.406-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982z"/>
                  </svg>
                </div>
                <span class="method-name">微信支付</span>
                <div class="check-icon" v-if="payMethod === 'wechat'">
                  <el-icon><Check /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="pay-action">
          <el-button type="primary" size="large" :loading="paying" @click="handlePay">
            立即支付 ¥{{ orderInfo.payAmount }}
          </el-button>
        </div>
      </div>

      <el-dialog v-model="qrcodeVisible" title="扫码支付" width="400px" :close-on-click-modal="false">
        <div class="qrcode-container">
          <div class="qrcode-placeholder">
            <el-icon :size="120"><Promotion /></el-icon>
            <p>模拟支付二维码</p>
          </div>
          <p class="pay-tip">请使用{{ payMethod === 'alipay' ? '支付宝' : '微信' }}扫码支付</p>
        </div>
        <template #footer>
          <el-button @click="qrcodeVisible = false">取消支付</el-button>
          <el-button type="primary" @click="handlePaySuccess">支付成功</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, Promotion } from '@element-plus/icons-vue'
import { orderApi } from '@/api'

const route = useRoute()
const router = useRouter()

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%23f5f5f5" width="100" height="100"/%3E%3Ctext x="50" y="50" text-anchor="middle" dy=".3em" fill="%23ccc" font-size="14"%3E暂无图片%3C/text%3E%3C/svg%3E'

const loading = ref(false)
const paying = ref(false)
const qrcodeVisible = ref(false)
const payMethod = ref('alipay')
const orderInfo = ref({
  orderNo: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  totalAmount: '0.00',
  freight: '0.00',
  payAmount: '0.00',
  items: []
})

const getOrderDetail = async () => {
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
        receiverName: shippingInfo.receiverName || '',
        receiverPhone: shippingInfo.receiverPhone || '',
        receiverAddress: shippingInfo.address || '',
        totalAmount: order.totalAmount,
        freight: '0.00',
        payAmount: order.payAmount,
        items: (order.orderItems || []).map(item => ({
          id: item.id,
          productName: item.productName,
          productImage: item.productImage,
          price: item.productPrice,
          quantity: item.quantity,
          spec: ''
        })),
        status: order.status
      }
      if (orderInfo.value.status !== 0) {
        ElMessage.warning('该订单已支付或已取消')
        router.push('/user/orders')
      }
    }
  } catch (error) {
    ElMessage.error('获取订单信息失败')
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

const handlePay = () => {
  qrcodeVisible.value = true
}

const handlePaySuccess = async () => {
  paying.value = true
  try {
    const res = await orderApi.payOrderByNo(orderInfo.value.orderNo, {
      payMethod: payMethod.value
    })
    if (res.code === 200) {
      ElMessage.success('支付成功')
      qrcodeVisible.value = false
      router.push('/user/orders')
    }
  } catch (error) {
    ElMessage.error('支付失败，请重试')
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  getOrderDetail()
})
</script>

<style lang="scss" scoped>
.pay-page {
  padding: 20px 0 40px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  padding: 16px 0;
}

.pay-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-info-card,
.product-list-card,
.pay-method-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;

  .card-header {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #262626;
    }

    .order-no {
      font-size: 14px;
      color: #8c8c8c;
    }
  }

  .card-body {
    padding: 16px 20px;
  }
}

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

.product-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .product-image {
    width: 60px;
    height: 60px;
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

.pay-amount-card {
  background: #fff;
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
      color: #262626;
      min-width: 80px;
      text-align: right;
    }

    &.total {
      padding-top: 12px;
      border-top: 1px solid #f0f0f0;

      .label {
        font-weight: 500;
        color: #262626;
      }

      .value.price {
        font-size: 20px;
        font-weight: 600;
        color: #C41E3A;
      }
    }
  }
}

.pay-methods {
  display: flex;
  gap: 16px;
}

.pay-method-item {
  flex: 1;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;

  &:hover {
    border-color: #C41E3A;
  }

  &.active {
    border-color: #C41E3A;
    background: rgba(196, 30, 58, 0.02);
  }

  .method-icon {
    margin-bottom: 8px;
  }

  .method-name {
    font-size: 14px;
    color: #262626;
  }

  .check-icon {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 20px;
    height: 20px;
    background: #C41E3A;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 12px;
  }
}

.pay-action {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  justify-content: center;

  .el-button {
    min-width: 200px;
    height: 48px;
    font-size: 16px;
  }
}

.qrcode-container {
  text-align: center;
  padding: 20px 0;

  .qrcode-placeholder {
    width: 200px;
    height: 200px;
    background: #f5f5f5;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    color: #8c8c8c;
  }

  .pay-tip {
    font-size: 14px;
    color: #595959;
  }
}
</style>
