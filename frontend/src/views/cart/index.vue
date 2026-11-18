<template>
  <div class="cart-page">
    <div class="container">
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>购物车</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <div class="cart-content" v-loading="cartStore.loading">
        <template v-if="cartStore.cartList.length > 0">
          <div class="cart-list">
            <div class="cart-header">
              <el-checkbox 
                v-model="isSelectAll" 
                @change="handleSelectAll"
              >
                全选
              </el-checkbox>
              <span class="col-product">商品信息</span>
              <span class="col-price">单价</span>
              <span class="col-quantity">数量</span>
              <span class="col-total">小计</span>
              <span class="col-action">操作</span>
            </div>
            
            <div 
              v-for="item in cartStore.cartList" 
              :key="item.id" 
              class="cart-item"
            >
              <el-checkbox 
                v-model="item.selected" 
                :true-label="1"
                :false-label="0"
                @change="handleSelectItem(item)"
              />
              
              <div class="col-product">
                <img :src="item.productImage || defaultImage" :alt="item.productName" />
                <div class="product-info">
                  <router-link :to="`/product/${item.productId}`" class="product-name">
                    {{ item.productName }}
                  </router-link>
                </div>
              </div>
              
              <div class="col-price">¥{{ item.price }}</div>
              
              <div class="col-quantity">
                <el-input-number 
                  v-model="item.quantity" 
                  :min="1" 
                  :max="item.stock"
                  size="small"
                  @change="handleQuantityChange(item)"
                />
              </div>
              
              <div class="col-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              
              <div class="col-action">
                <el-button type="text" @click="handleDelete(item.id)">删除</el-button>
              </div>
            </div>
          </div>
          
          <div class="cart-footer">
            <div class="footer-left">
              <el-button type="text" @click="handleClearCart">清空购物车</el-button>
            </div>
            
            <div class="footer-right">
              <div class="summary">
                <span>已选择 <strong>{{ cartStore.selectedCount }}</strong> 件商品</span>
                <span class="total-price">
                  合计：<strong>¥{{ cartStore.selectedTotalPrice.toFixed(2) }}</strong>
                </span>
              </div>
              <el-button 
                type="primary" 
                size="large"
                :disabled="cartStore.selectedCount === 0"
                @click="handleCheckout"
              >
                去结算
              </el-button>
            </div>
          </div>
        </template>
        
        <el-empty v-else description="购物车是空的">
          <el-button type="primary" @click="$router.push('/product')">去购物</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const defaultImage = 'https://via.placeholder.com/80x80?text=No+Image'

const isSelectAll = computed({
  get: () => cartStore.cartList.length > 0 && cartStore.cartList.every(item => item.selected),
  set: () => {}
})

const handleSelectAll = (val) => {
  cartStore.selectAll(val)
}

const handleSelectItem = (item) => {
  cartStore.updateCartItem(item.id, { selected: item.selected })
}

const handleQuantityChange = (item) => {
  cartStore.updateCartItem(item.id, { quantity: item.quantity })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cartStore.deleteCartItem(id)
  }).catch(() => {})
}

const handleClearCart = () => {
  ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cartStore.clearCart()
  }).catch(() => {})
}

const handleCheckout = () => {
  router.push('/order/confirm')
}

onMounted(() => {
  cartStore.getCartList()
})
</script>

<style lang="scss" scoped>
.cart-page {
  padding: 20px 0;
  
  .page-header {
    margin-bottom: 20px;
  }
  
  .cart-content {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    
    .cart-list {
      .cart-header {
        display: flex;
        align-items: center;
        padding: 16px 0;
        border-bottom: 1px solid #f0f0f0;
        font-weight: 500;
        color: #262626;
        
        .el-checkbox {
          margin-right: 20px;
        }
        
        .col-product {
          flex: 1;
        }
        
        .col-price,
        .col-quantity,
        .col-total,
        .col-action {
          width: 120px;
          text-align: center;
        }
      }
      
      .cart-item {
        display: flex;
        align-items: center;
        padding: 20px 0;
        border-bottom: 1px solid #f0f0f0;
        
        .el-checkbox {
          margin-right: 20px;
        }
        
        .col-product {
          flex: 1;
          display: flex;
          gap: 16px;
          
          img {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 4px;
          }
          
          .product-info {
            flex: 1;
            
            .product-name {
              color: #262626;
              font-size: 14px;
              line-height: 1.4;
              
              &:hover {
                color: #C41E3A;
              }
            }
          }
        }
        
        .col-price,
        .col-quantity,
        .col-total,
        .col-action {
          width: 120px;
          text-align: center;
        }
        
        .col-price {
          color: #8c8c8c;
        }
        
        .col-total {
          color: #C41E3A;
          font-weight: 500;
        }
      }
    }
    
    .cart-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20px 0;
      
      .footer-right {
        display: flex;
        align-items: center;
        gap: 24px;
        
        .summary {
          font-size: 14px;
          color: #8c8c8c;
          
          strong {
            color: #262626;
          }
          
          .total-price {
            margin-left: 16px;
            
            strong {
              color: #C41E3A;
              font-size: 20px;
            }
          }
        }
      }
    }
  }
}
</style>
