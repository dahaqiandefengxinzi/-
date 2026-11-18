<template>
  <div class="product-card" @click="handleClick">
    <div class="product-image">
      <img :src="product.mainImage || defaultImage" :alt="product.name" />
      <div v-if="product.stock === 0" class="sold-out">已售罄</div>
    </div>
    <div class="product-info">
      <h3 class="product-name ellipsis-2">{{ product.name }}</h3>
      <p v-if="product.subtitle" class="product-subtitle ellipsis">{{ product.subtitle }}</p>
      <div class="product-price">
        <span class="price-symbol">¥</span>
        <span class="price-value">{{ product.price }}</span>
      </div>
      <div class="product-meta">
        <span>已售 {{ product.sales || 0 }}</span>
        <span>库存 {{ product.stock }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const defaultImage = 'https://via.placeholder.com/200x200?text=No+Image'

const handleClick = () => {
  router.push(`/product/${props.product.id}`)
}
</script>

<style lang="scss" scoped>
.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  }
  
  .product-image {
    position: relative;
    width: 100%;
    padding-top: 100%;
    
    img {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .sold-out {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 18px;
    }
  }
  
  .product-info {
    padding: 12px;
    
    .product-name {
      font-size: 14px;
      color: #262626;
      line-height: 1.4;
      height: 40px;
      margin-bottom: 8px;
    }
    
    .product-subtitle {
      font-size: 12px;
      color: #8c8c8c;
      margin-bottom: 8px;
    }
    
    .product-price {
      color: #C41E3A;
      font-weight: bold;
      margin-bottom: 8px;
      
      .price-symbol {
        font-size: 12px;
      }
      
      .price-value {
        font-size: 18px;
      }
    }
    
    .product-meta {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #8c8c8c;
    }
  }
}
</style>
