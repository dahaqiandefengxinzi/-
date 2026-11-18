<template>
  <div class="product-list-page">
    <div class="container">
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>商品列表</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <div class="page-content">
        <aside class="filter-sidebar">
          <div class="filter-section">
            <h3>商品分类</h3>
            <div class="category-list" v-loading="categoryLoading">
              <div 
                v-for="category in categories" 
                :key="category.id"
                :class="['category-item', { active: currentCategoryId === category.id }]"
                @click="handleCategoryChange(category.id)"
              >
                {{ category.name }}
              </div>
            </div>
          </div>
          
          <div class="filter-section">
            <h3>价格区间</h3>
            <div class="price-filter">
              <el-input v-model="priceMin" placeholder="最低价" type="number" />
              <span>-</span>
              <el-input v-model="priceMax" placeholder="最高价" type="number" />
            </div>
          </div>
        </aside>
        
        <main class="product-main">
          <div class="toolbar">
            <div class="sort-options">
              <span>排序：</span>
              <el-radio-group v-model="sortType" @change="handleSortChange">
                <el-radio-button label="">默认</el-radio-button>
                <el-radio-button label="price_asc">价格升序</el-radio-button>
                <el-radio-button label="price_desc">价格降序</el-radio-button>
                <el-radio-button label="sales">销量</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          
          <div class="product-list" v-loading="loading">
            <template v-if="products.length > 0">
              <ProductCard 
                v-for="product in products" 
                :key="product.id" 
                :product="product"
              />
            </template>
            <el-empty v-else description="暂无商品" />
          </div>
          
          <div class="pagination" v-if="total > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[12, 24, 36, 48]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="getProducts"
              @current-change="getProducts"
            />
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()
const router = useRouter()

const categories = ref([])
const products = ref([])
const categoryLoading = ref(false)
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const sortType = ref('')
const currentCategoryId = ref(null)
const priceMin = ref('')
const priceMax = ref('')

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
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (currentCategoryId.value) {
      params.categoryId = currentCategoryId.value
    }
    
    if (sortType.value) {
      params.sort = sortType.value
    }
    
    if (route.query.keyword) {
      params.keyword = route.query.keyword
    }
    
    const res = await productApi.getList(params)
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('get products error:', e)
  } finally {
    loading.value = false
  }
}

const handleCategoryChange = (categoryId) => {
  currentCategoryId.value = currentCategoryId.value === categoryId ? null : categoryId
  currentPage.value = 1
  getProducts()
}

const handleSortChange = () => {
  currentPage.value = 1
  getProducts()
}

watch(() => route.query.keyword, () => {
  currentPage.value = 1
  getProducts()
})

onMounted(() => {
  getCategories()
  if (route.query.categoryId) {
    currentCategoryId.value = Number(route.query.categoryId)
  }
  getProducts()
})
</script>

<style lang="scss" scoped>
.product-list-page {
  padding: 20px 0;
  
  .page-header {
    margin-bottom: 20px;
  }
  
  .page-content {
    display: flex;
    gap: 20px;
    
    .filter-sidebar {
      width: 240px;
      flex-shrink: 0;
      
      .filter-section {
        background: #fff;
        border-radius: 8px;
        padding: 20px;
        margin-bottom: 20px;
        
        h3 {
          font-size: 16px;
          color: #262626;
          margin-bottom: 16px;
          padding-bottom: 12px;
          border-bottom: 1px solid #f0f0f0;
        }
        
        .category-list {
          .category-item {
            padding: 10px 12px;
            margin-bottom: 4px;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
              background: #fafafa;
            }
            
            &.active {
              background: #fff1f3;
              color: #C41E3A;
            }
          }
        }
        
        .price-filter {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .el-input {
            width: 80px;
          }
        }
      }
    }
    
    .product-main {
      flex: 1;
      
      .toolbar {
        background: #fff;
        border-radius: 8px;
        padding: 16px 20px;
        margin-bottom: 20px;
        
        .sort-options {
          display: flex;
          align-items: center;
          gap: 12px;
          
          span {
            color: #595959;
          }
        }
      }
      
      .product-list {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 20px;
        min-height: 400px;
        
        @media (max-width: 1024px) {
          grid-template-columns: repeat(3, 1fr);
        }
        
        @media (max-width: 768px) {
          grid-template-columns: repeat(2, 1fr);
        }
      }
      
      .pagination {
        background: #fff;
        border-radius: 8px;
        padding: 20px;
        margin-top: 20px;
        display: flex;
        justify-content: center;
      }
    }
  }
}
</style>
