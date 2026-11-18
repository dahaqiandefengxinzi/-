<template>
  <div class="user-center-page">
    <div class="container">
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/user' }">个人中心</el-breadcrumb-item>
          <el-breadcrumb-item>收货地址</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="user-content">
        <aside class="user-sidebar">
          <div class="user-info">
            <el-avatar :size="64" :src="userInfo.avatar">
              {{ userInfo.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="user-name">{{ userInfo.username }}</div>
            <div class="user-level">普通会员</div>
          </div>
          <el-menu :default-active="activeMenu" router>
            <el-menu-item index="/user">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
            <el-menu-item index="/user/info">
              <el-icon><Edit /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="/user/orders">
              <el-icon><List /></el-icon>
              <span>我的订单</span>
            </el-menu-item>
            <el-menu-item index="/user/address">
              <el-icon><Location /></el-icon>
              <span>收货地址</span>
            </el-menu-item>
          </el-menu>
        </aside>

        <main class="user-main">
          <div class="page-title">
            <h2>收货地址</h2>
            <el-button type="primary" @click="showAddressDialog()">
              <el-icon><Plus /></el-icon>
              新增地址
            </el-button>
          </div>

          <div class="address-list" v-loading="loading">
            <div class="empty-tip" v-if="addressList.length === 0">
              <el-empty description="暂无收货地址" />
            </div>
            <div
              class="address-item"
              v-for="address in addressList"
              :key="address.id"
              :class="{ default: address.isDefault }"
            >
              <div class="address-content">
                <div class="address-header">
                  <span class="name">{{ address.receiverName }}</span>
                  <span class="phone">{{ address.receiverPhone }}</span>
                  <el-tag v-if="address.isDefault" type="danger" size="small">默认</el-tag>
                </div>
                <div class="address-detail">
                  {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}
                </div>
              </div>
              <div class="address-actions">
                <el-button link type="primary" @click="showAddressDialog(address)">编辑</el-button>
                <el-button
                  link
                  type="primary"
                  v-if="!address.isDefault"
                  @click="setDefault(address.id)"
                >
                  设为默认
                </el-button>
                <el-button link type="danger" @click="deleteAddress(address.id)">删除</el-button>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑地址' : '新增地址'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="formData.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="formData.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="formData.region"
            :options="regionOptions"
            placeholder="请选择省/市/区"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input
            v-model="formData.detail"
            type="textarea"
            :rows="2"
            placeholder="请输入详细地址"
          />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="formData.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Edit, List, Location, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { addressApi } from '@/api'

const userStore = useUserStore()

const activeMenu = computed(() => '/user/address')
const userInfo = computed(() => userStore.userInfo)

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const addressList = ref([])

const formData = reactive({
  id: null,
  receiverName: '',
  receiverPhone: '',
  region: [],
  detail: '',
  isDefault: false
})

const rules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

const regionOptions = [
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' },
          { value: '朝阳区', label: '朝阳区' },
          { value: '海淀区', label: '海淀区' }
        ]
      }
    ]
  },
  {
    value: '上海市',
    label: '上海市',
    children: [
      {
        value: '上海市',
        label: '上海市',
        children: [
          { value: '黄浦区', label: '黄浦区' },
          { value: '徐汇区', label: '徐汇区' },
          { value: '浦东新区', label: '浦东新区' }
        ]
      }
    ]
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '越秀区', label: '越秀区' },
          { value: '海珠区', label: '海珠区' }
        ]
      },
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '南山区', label: '南山区' },
          { value: '福田区', label: '福田区' },
          { value: '宝安区', label: '宝安区' }
        ]
      }
    ]
  }
]

const fetchAddressList = async () => {
  loading.value = true
  try {
    const res = await addressApi.getAddressList()
    if (res.code === 200) {
      addressList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取地址列表失败')
  } finally {
    loading.value = false
  }
}

const showAddressDialog = (address = null) => {
  isEdit.value = !!address
  if (address) {
    formData.id = address.id
    formData.receiverName = address.receiverName
    formData.receiverPhone = address.receiverPhone
    formData.region = [address.province, address.city, address.district]
    formData.detail = address.detail
    formData.isDefault = address.isDefault
  } else {
    formData.id = null
    formData.receiverName = ''
    formData.receiverPhone = ''
    formData.region = []
    formData.detail = ''
    formData.isDefault = false
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      receiverName: formData.receiverName,
      receiverPhone: formData.receiverPhone,
      province: formData.region[0],
      city: formData.region[1],
      district: formData.region[2],
      detail: formData.detail,
      isDefault: formData.isDefault ? 1 : 0
    }

    let res
    if (isEdit.value) {
      res = await addressApi.updateAddress(formData.id, data)
    } else {
      res = await addressApi.addAddress(data)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchAddressList()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const setDefault = async (id) => {
  try {
    const res = await addressApi.setDefaultAddress(id)
    if (res.code === 200) {
      ElMessage.success('设置成功')
      fetchAddressList()
    }
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

const deleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await addressApi.deleteAddress(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchAddressList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchAddressList()
})
</script>

<style lang="scss" scoped>
.user-center-page {
  padding: 20px 0 40px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  padding: 16px 0;
}

.user-content {
  display: flex;
  gap: 20px;
}

.user-sidebar {
  width: 220px;
  flex-shrink: 0;

  .user-info {
    background: linear-gradient(135deg, #C41E3A 0%, #9B1B30 100%);
    border-radius: 8px;
    padding: 24px;
    text-align: center;
    color: #fff;
    margin-bottom: 12px;

    .el-avatar {
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
      font-size: 24px;
      margin-bottom: 12px;
    }

    .user-name {
      font-size: 18px;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .user-level {
      font-size: 12px;
      opacity: 0.8;
    }
  }

  .el-menu {
    background: #fff;
    border-radius: 8px;
    border: none;

    .el-menu-item {
      height: 48px;
      line-height: 48px;

      &.is-active {
        background: rgba(196, 30, 58, 0.1);
        color: #C41E3A;
        border-right: 3px solid #C41E3A;
      }
    }
  }
}

.user-main {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.page-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    font-size: 18px;
    color: #262626;
  }
}

.empty-tip {
  padding: 40px 0;
}

.address-item {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;

  &:hover {
    border-color: #C41E3A;
  }

  &.default {
    border-color: #C41E3A;
    background: rgba(196, 30, 58, 0.02);
  }

  .address-content {
    .address-header {
      margin-bottom: 8px;

      .name {
        font-size: 16px;
        font-weight: 500;
        color: #262626;
        margin-right: 12px;
      }

      .phone {
        font-size: 14px;
        color: #595959;
        margin-right: 12px;
      }
    }

    .address-detail {
      font-size: 14px;
      color: #8c8c8c;
    }
  }

  .address-actions {
    display: flex;
    gap: 8px;
  }
}
</style>
