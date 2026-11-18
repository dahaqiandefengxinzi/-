<template>
  <div class="users-page">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名/手机号/邮箱"
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
      <el-select v-model="filterRole" placeholder="用户角色" clearable @change="handleSearch">
        <el-option label="普通用户" :value="0" />
        <el-option label="管理员" :value="1" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="用户状态" clearable @change="handleSearch">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
    </div>

    <el-table :data="userList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : 'primary'" size="small">
            {{ row.role === 1 ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showUserDialog(row)">编辑</el-button>
          <el-button
            link
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            v-if="row.role !== 1"
            link
            type="warning"
            @click="setAdmin(row)"
          >
            设为管理员
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchUsers"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="编辑用户" width="500px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="formData.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="formData.role">
            <el-radio :value="0">普通用户</el-radio>
            <el-radio :value="1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)

const searchKeyword = ref('')
const filterRole = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const userList = ref([])

const formData = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  phone: '',
  role: 0,
  status: 1
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
    }
    if (filterRole.value !== '') {
      params.role = filterRole.value
    }
    if (filterStatus.value !== '') {
      params.status = filterStatus.value
    }
    const res = await adminApi.getUserList(params)
    if (res.code === 200) {
      userList.value = res.data.records || res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

const showUserDialog = (user) => {
  formData.id = user.id
  formData.username = user.username
  formData.nickname = user.nickname || ''
  formData.email = user.email || ''
  formData.phone = user.phone || ''
  formData.role = user.role
  formData.status = user.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await adminApi.updateUser(formData.id, {
      nickname: formData.nickname,
      email: formData.email,
      phone: formData.phone,
      role: formData.role,
      status: formData.status
    })
    if (res.code === 200) {
      ElMessage.success('修改成功')
      dialogVisible.value = false
      fetchUsers()
    }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await adminApi.updateUser(user.id, { status: newStatus })
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      fetchUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const setAdmin = async (user) => {
  try {
    await ElMessageBox.confirm('确定要将该用户设为管理员吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await adminApi.updateUser(user.id, { role: 1 })
    if (res.code === 200) {
      ElMessage.success('设置成功')
      fetchUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('设置失败')
    }
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style lang="scss" scoped>
.users-page {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;

  h2 {
    font-size: 18px;
    color: #262626;
  }
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
