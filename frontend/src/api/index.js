import request from '@/utils/request'

export const authApi = {
  registrationCode(data) {
    return request.post('/auth/registration-code', data)
  },
  register(data) {
    return request.post('/auth/register', data)
  },
  
  login(data) {
    return request.post('/auth/login', data)
  },
  
  logout() {
    // 静默登出，不显示错误提示
    return request.post('/auth/logout', {}, { silent: true })
  }
}

export const userApi = {
  getInfo() {
    return request.get('/user/info')
  },
  
  updateInfo(data) {
    return request.put('/user/info', data)
  },
  
  updatePassword(data) {
    return request.put('/user/password', data)
  }
}

export const productApi = {
  getCategory() {
    return request.get('/product/category')
  },
  
  getCategories() {
    return request.get('/product/category')
  },
  
  getList(params) {
    return request.get('/product/list', { params })
  },
  
  getDetail(id) {
    return request.get(`/product/detail/${id}`)
  }
}

export const bannerApi = {
  getActive() {
    return request.get('/banner/active')
  }
}

export const cartApi = {
  getList() {
    return request.get('/cart')
  },
  
  add(data) {
    return request.post('/cart', data)
  },
  
  update(id, data) {
    return request.put(`/cart/${id}`, data)
  },
  
  delete(id) {
    return request.delete(`/cart/${id}`)
  },
  
  clear() {
    return request.delete('/cart/clear')
  }
}

export const addressApi = {
  getList() {
    return request.get('/address')
  },
  
  add(data) {
    return request.post('/address', data)
  },
  
  update(id, data) {
    return request.put(`/address/${id}`, data)
  },
  
  delete(id) {
    return request.delete(`/address/${id}`)
  },
  
  setDefault(id) {
    return request.put(`/address/${id}/default`)
  }
}

export const orderApi = {
  create(data) {
    return request.post('/order', data)
  },
  
  getList(params) {
    return request.get('/order', { params })
  },
  
  getOrderList(params) {
    return request.get('/order', { params })
  },
  
  getDetail(id) {
    return request.get(`/order/${id}`)
  },
  
  getOrderByNo(orderNo) {
    return request.get(`/order/no/${orderNo}`)
  },
  
  pay(id, data) {
    return request.post(`/order/${id}/pay`, data)
  },
  
  payOrderByNo(orderNo, data) {
    return request.post(`/order/no/${orderNo}/pay`, data)
  },
  
  receive(id) {
    return request.post(`/order/${id}/receive`)
  },
  
  receiveByNo(orderNo) {
    return request.post(`/order/no/${orderNo}/receive`)
  },
  
  cancel(id) {
    return request.post(`/order/${id}/cancel`)
  },
  
  cancelByNo(orderNo) {
    return request.post(`/order/no/${orderNo}/cancel`)
  }
}

export const searchApi = {
  search(params) {
    return request.get('/search', { params })
  }
}

export const adminApi = {
  getDashboardStats() {
    return request.get('/admin/dashboard/stats')
  },
  
  getCategoryList() {
    return request.get('/admin/category/list')
  },
  
  getCategoryTree() {
    return request.get('/admin/category/tree')
  },
  
  addCategory(data) {
    return request.post('/admin/category', data)
  },
  
  updateCategory(id, data) {
    return request.put(`/admin/category/${id}`, data)
  },
  
  deleteCategory(id) {
    return request.delete(`/admin/category/${id}`)
  },
  
  getProductList(params) {
    return request.get('/admin/product/list', { params })
  },
  
  addProduct(data) {
    return request.post('/admin/product', data)
  },
  
  updateProduct(id, data) {
    return request.put(`/admin/product/${id}`, data)
  },
  
  updateProductStatus(id, data) {
    return request.put(`/admin/product/${id}/status`, data)
  },
  
  deleteProduct(id) {
    return request.delete(`/admin/product/${id}`)
  },
  
  getOrderList(params) {
    return request.get('/admin/order/list', { params })
  },
  
  getOrderDetail(orderNo) {
    return request.get(`/admin/order/detail/${orderNo}`)
  },
  
  shipOrder(id, data) {
    return request.put(`/admin/order/${id}/ship`, data)
  },
  
  getUserList(params) {
    return request.get('/admin/user/list', { params })
  },
  
  updateUser(id, data) {
    return request.put(`/admin/user/${id}`, data)
  },
  
  getBannerList(params) {
    return request.get('/admin/banner/list', { params })
  },
  
  addBanner(data) {
    return request.post('/admin/banner', data)
  },
  
  updateBanner(id, data) {
    return request.put(`/admin/banner/${id}`, data)
  },
  
  updateBannerStatus(id, status) {
    return request.put(`/admin/banner/${id}/status`, null, { params: { status } })
  },
  
  deleteBanner(id) {
    return request.delete(`/admin/banner/${id}`)
  }
}
