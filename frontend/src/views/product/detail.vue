<template>
  <div class="product-detail-page">
    <div class="container" v-loading="loading">
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/product' }">商品列表</el-breadcrumb-item>
          <el-breadcrumb-item>商品详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <div class="product-content" v-if="product">
        <div class="product-gallery">
          <div class="main-image">
            <img :src="currentImage" :alt="product.name" />
          </div>
          <div class="thumbnail-list" v-if="images.length > 1">
            <div 
              v-for="(img, index) in images" 
              :key="index"
              :class="['thumbnail-item', { active: currentImageIndex === index }]"
              @click="currentImageIndex = index"
            >
              <img :src="img" :alt="`${product.name} - ${index + 1}`" />
            </div>
          </div>
        </div>
        
        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <p class="product-subtitle" v-if="product.subtitle">{{ product.subtitle }}</p>
          
          <div class="price-section">
            <div class="current-price">
              <span class="label">价格</span>
              <span class="price">
                <span class="symbol">¥</span>
                <span class="value">{{ product.price }}</span>
              </span>
            </div>
          </div>
          
          <div class="info-row">
            <span class="label">库存</span>
            <span class="value">{{ product.stock }} 件</span>
          </div>
          
          <div class="info-row">
            <span class="label">销量</span>
            <span class="value">{{ product.sales || 0 }} 件</span>
          </div>
          
          <div class="quantity-section">
            <span class="label">数量</span>
            <el-input-number 
              v-model="quantity" 
              :min="1" 
              :max="product.stock"
              :disabled="product.stock === 0"
            />
          </div>
          
          <div class="action-buttons">
            <el-button 
              type="primary" 
              size="large"
              :disabled="product.stock === 0"
              @click="handleAddToCart"
            >
              <el-icon><ShoppingCart /></el-icon>
              加入购物车
            </el-button>
            <el-button 
              size="large"
              :disabled="product.stock === 0"
              @click="handleBuyNow"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>
      
      <div class="product-detail" v-if="product">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="detail-content" v-html="product.description || '暂无详情'"></div>
          </el-tab-pane>
          <el-tab-pane label="商品规格" name="specs">
            <div class="specs-content">
              <div v-if="product.specs && Object.keys(product.specs).length > 0">
                <div 
                  v-for="(value, key) in product.specs" 
                  :key="key" 
                  class="spec-item"
                >
                  <span class="spec-label">{{ key }}</span>
                  <span class="spec-value">{{ value }}</span>
                </div>
              </div>
              <el-empty v-else description="暂无规格信息" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import { productApi } from '@/api'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const currentImageIndex = ref(0)
const activeTab = ref('detail')

const images = computed(() => {
  if (product.value?.images && Array.isArray(product.value.images) && product.value.images.length > 0) {
    return product.value.images
  }
  if (product.value?.mainImage) {
    return [product.value.mainImage]
  }
  if (typeof product.value?.images === 'string') {
    try {
      const parsed = JSON.parse(product.value.images)
      return Array.isArray(parsed) && parsed.length > 0 ? parsed : [defaultImage]
    } catch {
      return [product.value.images]
    }
  }
  return [defaultImage]
})

const currentImage = computed(() => images.value[currentImageIndex.value] || defaultImage)
const defaultImage = 'https://via.placeholder.com/400x400?text=No+Image'

const getProduct = async () => {
  loading.value = true
  try {
    const res = await productApi.getDetail(route.params.id)
    product.value = res.data
  } catch (e) {
    console.error('get product error:', e)
    ElMessage.error('获取商品信息失败')
  } finally {
    loading.value = false
  }
}

const handleAddToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  
  await cartStore.addToCart(product.value.id, quantity.value)
}

const handleBuyNow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  
  await cartStore.addToCart(product.value.id, quantity.value)
  router.push({ name: 'OrderConfirm', query: { cartIds: cartStore.cartList[cartStore.cartList.length - 1].id } })
}

onMounted(() => {
  getProduct()
})
</script>

<style lang="scss" scoped>
.product-detail-page {
  padding: 20px 0;
  
  .page-header {
    margin-bottom: 20px;
  }
  
  .product-content {
    display: flex;
    gap: 40px;
    background: #fff;
    border-radius: 8px;
    padding: 30px;
    margin-bottom: 20px;
    
    .product-gallery {
      width: 400px;
      flex-shrink: 0;
      
      .main-image {
        width: 100%;
        height: 400px;
        border-radius: 8px;
        overflow: hidden;
        margin-bottom: 16px;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .thumbnail-list {
        display: flex;
        gap: 10px;
        
        .thumbnail-item {
          width: 60px;
          height: 60px;
          border-radius: 4px;
          overflow: hidden;
          cursor: pointer;
          border: 2px solid transparent;
          transition: all 0.3s;
          
          &.active {
            border-color: #C41E3A;
          }
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
      }
    }
    
    .product-info {
      flex: 1;
      
      .product-name {
        font-size: 24px;
        color: #262626;
        line-height: 1.4;
        margin-bottom: 12px;
      }
      
      .product-subtitle {
        font-size: 14px;
        color: #8c8c8c;
        margin-bottom: 20px;
      }
      
      .price-section {
        background: #fafafa;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 20px;
        
        .current-price {
          display: flex;
          align-items: baseline;
          gap: 12px;
          
          .label {
            font-size: 14px;
            color: #8c8c8c;
          }
          
          .price {
            color: #C41E3A;
            
            .symbol {
              font-size: 16px;
            }
            
            .value {
              font-size: 32px;
              font-weight: bold;
            }
          }
        }
      }
      
      .info-row {
        display: flex;
        align-items: center;
        margin-bottom: 16px;
        
        .label {
          width: 60px;
          color: #8c8c8c;
          font-size: 14px;
        }
        
        .value {
          color: #262626;
        }
      }
      
      .quantity-section {
        display: flex;
        align-items: center;
        margin-bottom: 24px;
        
        .label {
          width: 60px;
          color: #8c8c8c;
          font-size: 14px;
        }
      }
      
      .action-buttons {
        display: flex;
        gap: 16px;
        
        .el-button {
          min-width: 140px;
        }
      }
    }
  }
  
  .product-detail {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    
    .detail-content {
      padding: 20px 0;
      line-height: 1.8;
      
      img {
        max-width: 100%;
      }
    }
    
    .specs-content {
      padding: 20px 0;
      
      .spec-item {
        display: flex;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        
        .spec-label {
          width: 120px;
          color: #8c8c8c;
        }
        
        .spec-value {
          flex: 1;
          color: #262626;
        }
      }
    }
  }
}
</style>
