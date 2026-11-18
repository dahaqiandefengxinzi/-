<template>
  <div class="search-page">
    <div class="container">
      <div class="search-header" v-if="searchedKeyword">
        <div class="search-info">
          搜索 "<span class="keyword">{{ searchedKeyword }}</span>" 共找到
          <span class="count">{{ total }}</span> 件商品
        </div>
      </div>

      <div class="search-content" v-loading="loading">
        <div class="filter-bar">
          <div class="filter-item">
            <span class="label">排序：</span>
            <el-radio-group v-model="sortBy" @change="handleSearch">
              <el-radio-button value="default">综合</el-radio-button>
              <el-radio-button value="sales">销量</el-radio-button>
              <el-radio-button value="price_asc">价格升序</el-radio-button>
              <el-radio-button value="price_desc">价格降序</el-radio-button>
            </el-radio-group>
          </div>
          <div class="filter-item">
            <span class="label">价格区间：</span>
            <el-input-number
              v-model="priceMin"
              :min="0"
              :precision="0"
              placeholder="最低价"
              style="width: 100px"
            />
            <span class="separator">-</span>
            <el-input-number
              v-model="priceMax"
              :min="0"
              :precision="0"
              placeholder="最高价"
              style="width: 100px"
            />
            <el-button type="primary" size="small" @click="handleSearch">确定</el-button>
          </div>
        </div>

        <div class="empty-result" v-if="!loading && productList.length === 0 && searchedKeyword">
          <el-empty description="没有找到相关商品">
            <el-button type="primary" @click="clearSearch">清空搜索</el-button>
          </el-empty>
        </div>

        <div class="product-grid" v-else>
          <div class="product-item" v-for="product in productList" :key="product.id">
            <ProductCard :product="product" />
          </div>
        </div>

        <div class="pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handleSearch"
          />
        </div>
      </div>

      <div class="hot-search" v-if="!searchedKeyword">
        <h3>热门搜索</h3>
        <div class="hot-tags">
          <el-tag
            v-for="(tag, index) in hotKeywords"
            :key="index"
            class="hot-tag"
            @click="searchByKeyword(tag)"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const keyword = ref('')
const searchedKeyword = ref('')
const sortBy = ref('default')
const priceMin = ref(null)
const priceMax = ref(null)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const productList = ref([])

const hotKeywords = ref([
  '手机', '电脑', '耳机', '键盘', '鼠标', '显示器', '平板', '相机'
])

const handleSearch = async () => {
  if (!keyword.value.trim()) return

  searchedKeyword.value = keyword.value.trim()
  loading.value = true

  try {
    const params = {
      keyword: searchedKeyword.value,
      page: currentPage.value,
      size: pageSize.value,
      sort: sortBy.value === 'default' ? null : sortBy.value
    }
    if (priceMin.value !== null) {
      params.minPrice = priceMin.value
    }
    if (priceMax.value !== null) {
      params.maxPrice = priceMax.value
    }

    const res = await searchApi.search(params)
    if (res.code === 200) {
      productList.value = res.data.records || res.data.list || []
      total.value = res.data.total || 0
    }

    router.replace({
      path: '/search',
      query: { q: searchedKeyword.value }
    })
  } catch (error) {
    console.error('搜索失败', error)
  } finally {
    loading.value = false
  }
}

const searchByKeyword = (kw) => {
  keyword.value = kw
  currentPage.value = 1
  handleSearch()
}

const clearSearch = () => {
  keyword.value = ''
  searchedKeyword.value = ''
  productList.value = []
  total.value = 0
  router.replace({ path: '/search' })
}

watch(() => route.query.q, (newKeyword) => {
  if (newKeyword && newKeyword !== searchedKeyword.value) {
    keyword.value = newKeyword
    handleSearch()
  }
}, { immediate: true })

onMounted(() => {
  if (route.query.q) {
    keyword.value = route.query.q
    handleSearch()
  }
})
</script>

<style lang="scss" scoped>
.search-page {
  padding: 20px 0 40px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-header {
  background: #fff;
  border-radius: 8px;
  padding: 16px 24px;
  margin-bottom: 20px;

  .search-info {
    text-align: center;
    font-size: 14px;
    color: #595959;

    .keyword {
      color: #C41E3A;
      font-weight: 500;
    }

    .count {
      color: #C41E3A;
      font-weight: 600;
    }
  }
}

.search-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;

  .filter-item {
    display: flex;
    align-items: center;
    gap: 8px;

    .label {
      font-size: 14px;
      color: #595959;
    }

    .separator {
      color: #8c8c8c;
    }
  }
}

.empty-result {
  padding: 60px 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;

  .product-item {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }
  }
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.hot-search {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-top: 20px;

  h3 {
    font-size: 16px;
    color: #262626;
    margin-bottom: 16px;
  }

  .hot-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .hot-tag {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: #C41E3A;
        color: #fff;
        border-color: #C41E3A;
      }
    }
  }
}

@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-bar {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
