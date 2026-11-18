<template>
  <div class="home-page">
    <div class="banner-section" v-loading="bannerLoading">
      <el-carousel 
        v-if="banners.length > 0" 
        height="400px" 
        :interval="bannerInterval"
        indicator-position="outside"
      >
        <el-carousel-item v-for="item in banners" :key="item.id">
          <div 
            class="banner-item" 
            :style="{ backgroundImage: `url(${item.imageUrl})` }"
            @click="handleBannerClick(item)"
          >
            <div class="banner-overlay">
              <div class="banner-content">
                <h2>{{ item.title }}</h2>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <div v-else class="banner-placeholder">
        <el-empty description="暂无Banner" />
      </div>
    </div>
    
    <div class="category-section">
      <div class="container">
        <div class="section-header">
          <h2>商品分类</h2>
        </div>
        <div class="category-list" v-loading="categoryLoading">
          <div 
            v-for="category in categories" 
            :key="category.id" 
            class="category-item"
            @click="handleCategoryClick(category.id)"
          >
            <div class="category-icon">
              <el-icon :size="32"><Goods /></el-icon>
            </div>
            <span class="category-name">{{ category.name }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="product-section">
      <div class="container">
        <div class="section-header">
          <h2>热门商品</h2>
          <router-link to="/product" class="view-more">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
        <div class="product-list" v-loading="productLoading">
          <ProductCard 
            v-for="product in products" 
            :key="product.id" 
            :product="product"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productApi, bannerApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'
import { Goods, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

const banners = ref([])
const categories = ref([])
const products = ref([])
const bannerLoading = ref(false)
const categoryLoading = ref(false)
const productLoading = ref(false)

const bannerInterval = computed(() => {
  if (banners.value.length > 0) {
    return banners.value[0].intervalTime || 5000
  }
  return 5000
})

const getBanners = async () => {
  bannerLoading.value = true
  try {
    const res = await bannerApi.getActive()
    banners.value = res.data || []
  } catch (e) {
    console.error('get banners error:', e)
  } finally {
    bannerLoading.value = false
  }
}

const getCategories = async () => {
  categoryLoading.value = true
  try {
    const res = await productApi.getCategory()
    categories.value = res.data || []
  } catch (e) {
    console.error('get categories error:', e)
  } finally {
    categoryLoading.value = false
  }
}

const getProducts = async () => {
  productLoading.value = true
  try {
    const res = await productApi.getList({ page: 1, size: 8 })
    products.value = res.data?.records || []
  } catch (e) {
    console.error('get products error:', e)
  } finally {
    productLoading.value = false
  }
}

const handleBannerClick = (banner) => {
  if (banner.linkUrl) {
    if (banner.linkUrl.startsWith('http')) {
      window.open(banner.linkUrl, '_blank')
    } else {
      router.push(banner.linkUrl)
    }
  }
}

const handleCategoryClick = (categoryId) => {
  router.push({ name: 'ProductList', query: { categoryId } })
}

onMounted(() => {
  getBanners()
  getCategories()
  getProducts()
})
</script>

<style lang="scss" scoped>
.home-page {
  .banner-section {
    .banner-item {
      height: 100%;
      background-size: cover;
      background-position: center;
      cursor: pointer;
      position: relative;
      
      .banner-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(to right, rgba(0, 0, 0, 0.5) 0%, rgba(0, 0, 0, 0.1) 100%);
        display: flex;
        align-items: center;
        justify-content: flex-start;
        padding-left: 10%;
        
        .banner-content {
          color: #fff;
          max-width: 500px;
          
          h2 {
            font-size: 48px;
            margin-bottom: 16px;
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
          }
          
          p {
            font-size: 18px;
            opacity: 0.9;
            text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
          }
        }
      }
    }
    
    .banner-placeholder {
      height: 400px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
    }
    
    :deep(.el-carousel__indicators) {
      .el-carousel__indicator {
        .el-carousel__button {
          width: 30px;
          height: 4px;
          border-radius: 2px;
          background: rgba(255, 255, 255, 0.5);
        }
        
        &.is-active .el-carousel__button {
          background: #C41E3A;
        }
      }
    }
  }
  
  .category-section {
    padding: 40px 0;
    background: #fff;
    
    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 24px;
      
      h2 {
        font-size: 24px;
        color: #262626;
      }
    }
    
    .category-list {
      display: flex;
      gap: 20px;
      overflow-x: auto;
      padding-bottom: 10px;
      
      .category-item {
        flex-shrink: 0;
        width: 120px;
        text-align: center;
        padding: 20px;
        background: #fafafa;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #fff1f3;
          transform: translateY(-4px);
        }
        
        .category-icon {
          width: 64px;
          height: 64px;
          margin: 0 auto 12px;
          background: #fff;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #C41E3A;
        }
        
        .category-name {
          font-size: 14px;
          color: #262626;
        }
      }
    }
  }
  
  .product-section {
    padding: 40px 0;
    
    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 24px;
      
      h2 {
        font-size: 24px;
        color: #262626;
      }
      
      .view-more {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #C41E3A;
        font-size: 14px;
        
        &:hover {
          color: #E8505B;
        }
      }
    }
    
    .product-list {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20px;
      
      @media (max-width: 1024px) {
        grid-template-columns: repeat(3, 1fr);
      }
      
      @media (max-width: 768px) {
        grid-template-columns: repeat(2, 1fr);
      }
    }
  }
}
</style>
