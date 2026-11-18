import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'product',
        name: 'ProductList',
        component: () => import('@/views/product/list.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/product/detail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/cart/index.vue'),
        meta: { title: '购物车', requiresAuth: true }
      },
      {
        path: 'order/confirm',
        name: 'OrderConfirm',
        component: () => import('@/views/order/confirm.vue'),
        meta: { title: '确认订单', requiresAuth: true }
      },
      {
        path: 'order/pay/:orderNo',
        name: 'OrderPay',
        component: () => import('@/views/order/pay.vue'),
        meta: { title: '订单支付', requiresAuth: true }
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'user/info',
        name: 'UserInfo',
        component: () => import('@/views/user/info.vue'),
        meta: { title: '个人信息', requiresAuth: true }
      },
      {
        path: 'user/orders',
        name: 'UserOrders',
        component: () => import('@/views/user/orders.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'user/order/:orderNo',
        name: 'OrderDetail',
        component: () => import('@/views/user/order-detail.vue'),
        meta: { title: '订单详情', requiresAuth: true }
      },
      {
        path: 'user/address',
        name: 'UserAddress',
        component: () => import('@/views/user/address.vue'),
        meta: { title: '收货地址', requiresAuth: true }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/search/index.vue'),
        meta: { title: '搜索结果' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard.vue'),
        meta: { title: '后台首页' }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/products.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/categories.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/orders.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'banners',
        name: 'AdminBanners',
        component: () => import('@/views/admin/banners.vue'),
        meta: { title: 'Banner管理' }
      },
      {
        path: 'captcha',
        name: 'AdminCaptcha',
        component: () => import('@/views/admin/captcha.vue'),
        meta: { title: '验证码配置' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - ShopLab` : 'ShopLab - 电商平台'
  
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next({ name: 'Home' })
    return
  }
  
  if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
    next({ name: 'Home' })
    return
  }
  
  next()
})

export default router
