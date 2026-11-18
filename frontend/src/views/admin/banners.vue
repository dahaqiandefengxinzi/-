<template>
  <div class="banners-page">
    <div class="page-header">
      <h2>Banner管理</h2>
      <el-button type="primary" @click="showBannerDialog()">
        <el-icon><Plus /></el-icon>
        添加Banner
      </el-button>
    </div>

    <el-table :data="bannerList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="预览" width="200">
        <template #default="{ row }">
          <img :src="row.imageUrl" class="banner-preview" @click="previewImage(row.imageUrl)" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="linkUrl" label="跳转链接" min-width="150" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="intervalTime" label="轮播间隔(ms)" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showBannerDialog(row)">编辑</el-button>
          <el-button
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="danger" @click="deleteBanner(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchBanners"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑Banner' : '添加Banner'"
      width="600px"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入Banner标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入Banner描述" />
        </el-form-item>
        <el-form-item label="图片URL" prop="imageUrl">
          <el-input v-model="formData.imageUrl" placeholder="请输入图片URL地址" />
          <div class="image-tip">建议尺寸：1200 x 400 像素</div>
        </el-form-item>
        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input v-model="formData.linkUrl" placeholder="请输入点击跳转链接（如：/product）" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" style="width: 100%" />
          <div class="image-tip">数字越小越靠前</div>
        </el-form-item>
        <el-form-item label="轮播间隔" prop="intervalTime">
          <el-input-number v-model="formData.intervalTime" :min="1000" :step="500" style="width: 100%" />
          <div class="image-tip">单位：毫秒，建议3000-8000</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="图片预览" width="800px">
      <img :src="previewUrl" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const bannerList = ref([])

const formData = reactive({
  id: null,
  title: '',
  description: '',
  imageUrl: '',
  linkUrl: '',
  sort: 0,
  status: 1,
  intervalTime: 5000
})

const rules = {
  title: [{ required: true, message: '请输入Banner标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请输入图片URL', trigger: 'blur' }]
}

const fetchBanners = async () => {
  loading.value = true
  try {
    const res = await adminApi.getBannerList({
      page: currentPage.value,
      size: pageSize.value
    })
    if (res.code === 200) {
      bannerList.value = res.data.records || res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取Banner列表失败')
  } finally {
    loading.value = false
  }
}

const showBannerDialog = (banner = null) => {
  isEdit.value = !!banner
  if (banner) {
    formData.id = banner.id
    formData.title = banner.title
    formData.description = banner.description || ''
    formData.imageUrl = banner.imageUrl
    formData.linkUrl = banner.linkUrl || ''
    formData.sort = banner.sort || 0
    formData.status = banner.status
    formData.intervalTime = banner.intervalTime || 5000
  } else {
    formData.id = null
    formData.title = ''
    formData.description = ''
    formData.imageUrl = ''
    formData.linkUrl = ''
    formData.sort = 0
    formData.status = 1
    formData.intervalTime = 5000
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      res = await adminApi.updateBanner(formData.id, formData)
    } else {
      res = await adminApi.addBanner(formData)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchBanners()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (banner) => {
  const newStatus = banner.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}该Banner吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await adminApi.updateBannerStatus(banner.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      fetchBanners()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const deleteBanner = async (banner) => {
  try {
    await ElMessageBox.confirm('确定要删除该Banner吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await adminApi.deleteBanner(banner.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchBanners()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

onMounted(() => {
  fetchBanners()
})
</script>

<style lang="scss" scoped>
.banners-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    font-size: 18px;
    color: #262626;
  }
}

.banner-preview {
  width: 160px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s;
  
  &:hover {
    transform: scale(1.05);
  }
}

.image-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
