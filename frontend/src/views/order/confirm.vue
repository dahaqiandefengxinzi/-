<template>
  <div class="order-confirm-page">
    <div class="container">
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/cart' }">购物车</el-breadcrumb-item>
          <el-breadcrumb-item>确认订单</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <div class="order-content" v-loading="loading">
        <div class="address-section">
          <div class="section-header">
            <h3>收货地址</h3>
            <el-button type="primary" text @click="showAddressDialog = true">
              <el-icon><Plus /></el-icon>
              新增地址
            </el-button>
          </div>
          
          <div class="address-list" v-if="addresses.length > 0">
            <div 
              v-for="addr in addresses" 
              :key="addr.id"
              :class="['address-item', { active: selectedAddressId === addr.id }]"
              @click="selectedAddressId = addr.id"
            >
              <div class="address-info">
                <div class="address-name">
                  <span class="name">{{ addr.name }}</span>
                  <span class="phone">{{ addr.phone }}</span>
                  <el-tag v-if="addr.isDefault" type="danger" size="small">默认</el-tag>
                </div>
                <div class="address-detail">
                  {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}
                </div>
              </div>
              <div class="address-actions">
                <el-button type="text" size="small" @click.stop="editAddress(addr)">编辑</el-button>
                <el-button type="text" size="small" @click.stop="deleteAddress(addr.id)">删除</el-button>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="暂无收货地址，请添加" />
        </div>
        
        <div class="product-section">
          <div class="section-header">
            <h3>商品清单</h3>
          </div>
          
          <div class="product-list">
            <div v-for="item in selectedItems" :key="item.id" class="product-item">
              <img :src="item.productImage || defaultImage" :alt="item.productName" />
              <div class="product-info">
                <div class="product-name">{{ item.productName }}</div>
              </div>
              <div class="product-price">¥{{ item.price }}</div>
              <div class="product-quantity">x{{ item.quantity }}</div>
              <div class="product-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </div>
        
        <div class="payment-section">
          <div class="section-header">
            <h3>支付方式</h3>
          </div>
          
          <el-radio-group v-model="paymentType">
            <el-radio :label="1">
              <div class="payment-option">
                <el-icon :size="20"><Wallet /></el-icon>
                <span>支付宝</span>
              </div>
            </el-radio>
            <el-radio :label="2">
              <div class="payment-option">
                <el-icon :size="20"><ChatDotRound /></el-icon>
                <span>微信支付</span>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
        
        <div class="order-summary">
          <div class="summary-row">
            <span>商品总额</span>
            <span>¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <div class="summary-row total">
            <span>应付金额</span>
            <span class="price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
        </div>
        
        <div class="order-actions">
          <el-button @click="$router.back()">返回购物车</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交订单
          </el-button>
        </div>
      </div>
    </div>
    
    <el-dialog 
      v-model="showAddressDialog" 
      :title="editingAddress ? '编辑地址' : '新增地址'"
      width="500px"
    >
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="80px">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-input v-model="addressForm.district" placeholder="请输入区县" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input 
            v-model="addressForm.detailAddress" 
            type="textarea" 
            placeholder="请输入详细地址"
            :rows="2"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" :loading="addressSubmitting" @click="handleSaveAddress">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Wallet, ChatDotRound } from '@element-plus/icons-vue'
import { addressApi, orderApi } from '@/api'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

const loading = ref(false)
const submitting = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)
const paymentType = ref(1)
const showAddressDialog = ref(false)
const editingAddress = ref(null)
const addressFormRef = ref(null)
const addressSubmitting = ref(false)
const defaultImage = 'https://via.placeholder.com/80x80?text=No+Image'

const selectedItems = computed(() => cartStore.selectedItems)
const totalPrice = computed(() => cartStore.selectedTotalPrice)

const addressForm = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: ''
})

const addressRules = {
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const getAddresses = async () => {
  loading.value = true
  try {
    const res = await addressApi.getList()
    addresses.value = res.data || []
    const defaultAddr = addresses.value.find(a => a.isDefault)
    if (defaultAddr) {
      selectedAddressId.value = defaultAddr.id
    } else if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id
    }
  } catch (e) {
    console.error('get addresses error:', e)
  } finally {
    loading.value = false
  }
}

const editAddress = (addr) => {
  editingAddress.value = addr
  Object.assign(addressForm, addr)
  showAddressDialog.value = true
}

const deleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await addressApi.delete(id)
    ElMessage.success('删除成功')
    await getAddresses()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('delete address error:', e)
    }
  }
}

const handleSaveAddress = async () => {
  const valid = await addressFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  addressSubmitting.value = true
  try {
    if (editingAddress.value) {
      await addressApi.update(editingAddress.value.id, addressForm)
      ElMessage.success('修改成功')
    } else {
      await addressApi.add(addressForm)
      ElMessage.success('添加成功')
    }
    showAddressDialog.value = false
    await getAddresses()
  } catch (e) {
    console.error('save address error:', e)
  } finally {
    addressSubmitting.value = false
  }
}

const handleSubmit = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  submitting.value = true
  try {
    await orderApi.create({
      addressId: selectedAddressId.value,
      paymentType: paymentType.value,
      cartIds: selectedItems.value.map(item => item.id)
    })
    ElMessage.success('订单创建成功')
    cartStore.getCartList()
    router.push({ name: 'UserOrders' })
  } catch (e) {
    console.error('create order error:', e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  getAddresses()
})
</script>

<style lang="scss" scoped>
.order-confirm-page {
  padding: 20px 0;
  
  .page-header {
    margin-bottom: 20px;
  }
  
  .order-content {
    .address-section,
    .product-section,
    .payment-section {
      background: #fff;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 20px;
      
      .section-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;
        
        h3 {
          font-size: 16px;
          color: #262626;
        }
      }
    }
    
    .address-list {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      
      .address-item {
        border: 1px solid #e8e8e8;
        border-radius: 8px;
        padding: 16px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          border-color: #C41E3A;
        }
        
        &.active {
          border-color: #C41E3A;
          background: #fff1f3;
        }
        
        .address-info {
          .address-name {
            margin-bottom: 8px;
            
            .name {
              font-weight: 500;
              margin-right: 12px;
            }
            
            .phone {
              color: #8c8c8c;
              margin-right: 8px;
            }
          }
          
          .address-detail {
            font-size: 14px;
            color: #595959;
          }
        }
        
        .address-actions {
          margin-top: 12px;
          text-align: right;
        }
      }
    }
    
    .product-list {
      .product-item {
        display: flex;
        align-items: center;
        padding: 16px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        img {
          width: 80px;
          height: 80px;
          object-fit: cover;
          border-radius: 4px;
          margin-right: 16px;
        }
        
        .product-info {
          flex: 1;
          
          .product-name {
            color: #262626;
            font-size: 14px;
          }
        }
        
        .product-price,
        .product-quantity,
        .product-total {
          width: 100px;
          text-align: center;
        }
        
        .product-total {
          color: #C41E3A;
          font-weight: 500;
        }
      }
    }
    
    .payment-section {
      .el-radio-group {
        display: flex;
        gap: 20px;
        
        .payment-option {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }
    }
    
    .order-summary {
      background: #fff;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 20px;
      
      .summary-row {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        margin-bottom: 12px;
        
        span:first-child {
          color: #8c8c8c;
          margin-right: 20px;
        }
        
        &.total {
          margin-bottom: 0;
          padding-top: 12px;
          border-top: 1px solid #f0f0f0;
          
          .price {
            color: #C41E3A;
            font-size: 24px;
            font-weight: bold;
          }
        }
      }
    }
    
    .order-actions {
      background: #fff;
      border-radius: 8px;
      padding: 20px;
      display: flex;
      justify-content: flex-end;
      gap: 16px;
    }
  }
}
</style>
