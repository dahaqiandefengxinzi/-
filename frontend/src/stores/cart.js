import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '@/api'
import { ElMessage } from 'element-plus'

export const useCartStore = defineStore('cart', () => {
  const cartList = ref([])
  const loading = ref(false)
  
  const totalCount = computed(() => {
    return cartList.value.reduce((sum, item) => sum + item.quantity, 0)
  })
  
  const selectedItems = computed(() => {
    return cartList.value.filter(item => item.selected)
  })
  
  const selectedCount = computed(() => {
    return selectedItems.value.reduce((sum, item) => sum + item.quantity, 0)
  })
  
  const selectedTotalPrice = computed(() => {
    return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  })
  
  const getCartList = async () => {
    loading.value = true
    try {
      const res = await cartApi.getList()
      cartList.value = res.data || []
    } catch (e) {
      console.error('get cart list error:', e)
    } finally {
      loading.value = false
    }
  }
  
  const addToCart = async (productId, quantity = 1) => {
    try {
      await cartApi.add({ productId, quantity })
      ElMessage.success('添加成功')
      await getCartList()
    } catch (e) {
      console.error('add to cart error:', e)
    }
  }
  
  const updateCartItem = async (id, data) => {
    try {
      await cartApi.update(id, data)
      await getCartList()
    } catch (e) {
      console.error('update cart item error:', e)
    }
  }
  
  const deleteCartItem = async (id) => {
    try {
      await cartApi.delete(id)
      ElMessage.success('删除成功')
      await getCartList()
    } catch (e) {
      console.error('delete cart item error:', e)
    }
  }
  
  const clearCart = async () => {
    try {
      await cartApi.clear()
      ElMessage.success('清空成功')
      cartList.value = []
    } catch (e) {
      console.error('clear cart error:', e)
    }
  }
  
  const selectAll = (selected) => {
    cartList.value.forEach(item => {
      item.selected = selected ? 1 : 0
      updateCartItem(item.id, { selected: item.selected })
    })
  }
  
  return {
    cartList,
    loading,
    totalCount,
    selectedItems,
    selectedCount,
    selectedTotalPrice,
    getCartList,
    addToCart,
    updateCartItem,
    deleteCartItem,
    clearCart,
    selectAll
  }
})
