<template>
  <div class="orders-page">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchOrderNo"
        placeholder="搜索订单号"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
      <el-select v-model="filterStatus" placeholder="订单状态" clearable @change="handleSearch">
        <el-option label="待付款" :value="0" />
        <el-option label="待发货" :value="1" />
        <el-option label="待收货" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已取消" :value="4" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        @change="handleSearch"
      />
    </div>

    <el-table :data="orderList" v-loading="loading" style="width: 100%">
      <el-table-column prop="orderSn" label="订单号" width="180" />
      <el-table-column label="收货人" width="100">
        <template #default="{ row }">
          {{ getReceiverName(row) }}
        </template>
      </el-table-column>
      <el-table-column label="电话" width="120">
        <template #default="{ row }">
          {{ getReceiverPhone(row) }}
        </template>
      </el-table-column>
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div class="product-preview">
            <span>{{ row.orderItems?.length || 0 }}件商品</span>
            <el-popover placement="right" :width="400" trigger="hover">
              <template #reference>
                <el-button link type="primary">查看</el-button>
              </template>
              <div class="product-list-popover">
                <div class="product-item" v-for="item in row.orderItems" :key="item.id">
                  <img :src="item.productImage || defaultImage" />
                  <div class="info">
                    <div class="name">{{ item.productName }}</div>
                    <div class="meta">¥{{ item.productPrice }} x {{ item.quantity }}</div>
                  </div>
                </div>
              </div>
            </el-popover>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="payAmount" label="金额" width="100">
        <template #default="{ row }">
          <span class="price">¥{{ row.payAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showOrderDetail(row)">详情</el-button>
          <el-button
            v-if="row.status === 1"
            link
            type="primary"
            @click="showShipDialog(row)"
          >
            发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchOrders"
      />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <div class="order-detail" v-if="currentOrder">
        <div class="detail-section">
          <h4>订单信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">订单号：</span>
              <span>{{ currentOrder.orderSn }}</span>
            </div>
            <div class="info-item">
              <span class="label">下单时间：</span>
              <span>{{ currentOrder.createTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单状态：</span>
              <el-tag :type="getStatusType(currentOrder.status)" size="small">
                {{ getStatusText(currentOrder.status) }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">支付方式：</span>
              <span>{{ currentOrder.paymentType === 1 ? '支付宝' : '微信支付' }}</span>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <h4>收货信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">收货人：</span>
              <span>{{ getReceiverName(currentOrder) }}</span>
            </div>
            <div class="info-item">
              <span class="label">电话：</span>
              <span>{{ getReceiverPhone(currentOrder) }}</span>
            </div>
            <div class="info-item full">
              <span class="label">地址：</span>
              <span>{{ getReceiverAddress(currentOrder) }}</span>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <h4>商品清单</h4>
          <el-table :data="currentOrder.orderItems" size="small">
            <el-table-column label="商品" min-width="200">
              <template #default="{ row }">
                <div class="product-cell">
                  <img :src="row.productImage || defaultImage" />
                  <span>{{ row.productName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="productPrice" label="单价" width="100">
              <template #default="{ row }">¥{{ row.productPrice }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column label="小计" width="100">
              <template #default="{ row }">
                <span class="price">¥{{ (row.productPrice * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="detail-section">
          <div class="amount-summary">
            <span>订单金额：</span>
            <span class="price">¥{{ currentOrder.payAmount }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="shipVisible" title="订单发货" width="400px">
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="物流公司">
          <el-select v-model="shipForm.expressCompany" placeholder="请选择" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="shipForm.expressNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" @click="handleShip" :loading="shipping">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%23f5f5f5" width="100" height="100"/%3E%3Ctext x="50" y="50" text-anchor="middle" dy=".3em" fill="%23ccc" font-size="14"%3E暂无图片%3C/text%3E%3C/svg%3E'

const loading = ref(false)
const shipping = ref(false)
const detailVisible = ref(false)
const shipVisible = ref(false)

const searchOrderNo = ref('')
const filterStatus = ref('')
const dateRange = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orderList = ref([])
const currentOrder = ref(null)

const shipForm = reactive({
  orderId: null,
  expressCompany: '',
  expressNo: ''
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

const getReceiverPhone = (order) => {
  if (order.shippingAddress) {
    try {
      const addr = JSON.parse(order.shippingAddress)
      return addr.phone || '-'
    } catch (e) {
      return '-'
    }
  }
  return '-'
}

const getReceiverAddress = (order) => {
  if (order.shippingAddress) {
    try {
      const addr = JSON.parse(order.shippingAddress)
      return `${addr.province || ''}${addr.city || ''}${addr.district || ''}${addr.detailAddress || ''}`
    } catch (e) {
      return '-'
    }
  }
  return '-'
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      orderNo: searchOrderNo.value
    }
    if (filterStatus.value !== '') {
      params.status = filterStatus.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await adminApi.getOrderList(params)
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

const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

const showOrderDetail = async (order) => {
  try {
    const res = await adminApi.getOrderDetail(order.orderSn)
    if (res.code === 200) {
      currentOrder.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  }
}

const showShipDialog = (order) => {
  shipForm.orderId = order.id
  shipForm.expressCompany = ''
  shipForm.expressNo = ''
  shipVisible.value = true
}

const handleShip = async () => {
  if (!shipForm.expressCompany || !shipForm.expressNo) {
    ElMessage.warning('请填写完整的物流信息')
    return
  }

  shipping.value = true
  try {
    const res = await adminApi.shipOrder(shipForm.orderId, {
      shippingCompany: shipForm.expressCompany,
      shippingNumber: shipForm.expressNo
    })
    if (res.code === 200) {
      ElMessage.success('发货成功')
      shipVisible.value = false
      fetchOrders()
    }
  } catch (error) {
    ElMessage.error('发货失败')
  } finally {
    shipping.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style lang="scss" scoped>
.orders-page {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;

  h2 {
    font-size: 18px;
    color: #262626;
  }
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.price {
  color: #C41E3A;
  font-weight: 500;
}

.product-preview {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-list-popover {
  .product-item {
    display: flex;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    img {
      width: 50px;
      height: 50px;
      object-fit: cover;
      border-radius: 4px;
      margin-right: 12px;
    }

    .info {
      .name {
        font-size: 14px;
        color: #262626;
        margin-bottom: 4px;
      }

      .meta {
        font-size: 12px;
        color: #8c8c8c;
      }
    }
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.order-detail {
  .detail-section {
    margin-bottom: 20px;

    h4 {
      font-size: 14px;
      color: #262626;
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 1px solid #f0f0f0;
    }

    .info-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;

      .info-item {
        font-size: 14px;

        &.full {
          grid-column: span 2;
        }

        .label {
          color: #8c8c8c;
        }
      }
    }

    .product-cell {
      display: flex;
      align-items: center;
      gap: 8px;

      img {
        width: 40px;
        height: 40px;
        object-fit: cover;
        border-radius: 4px;
      }
    }

    .amount-summary {
      text-align: right;
      font-size: 16px;

      .price {
        font-size: 20px;
      }
    }
  }
}
</style>
