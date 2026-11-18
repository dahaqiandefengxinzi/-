<template>
  <div class="products-page">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="showProductDialog()">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品名称"
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
      <el-select v-model="filterCategory" placeholder="选择分类" clearable @change="handleSearch">
        <el-option
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>
      <el-select v-model="filterStatus" placeholder="商品状态" clearable @change="handleSearch">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
    </div>

    <el-table :data="productList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="商品图片" width="100">
        <template #default="{ row }">
          <img :src="row.mainImage || defaultImage" class="product-image" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="200" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="price" label="价格" width="120">
        <template #default="{ row }">
          <span class="price">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column prop="sales" label="销量" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showProductDialog(row)">编辑</el-button>
          <el-button
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button link type="danger" @click="deleteProduct(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchProducts"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '添加商品'"
      width="600px"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="formData.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品图片" prop="mainImage">
          <el-input v-model="formData.mainImage" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="商品状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
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
import { Plus } from '@element-plus/icons-vue'
import { adminApi, productApi } from '@/api'

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%23f5f5f5" width="100" height="100"/%3E%3Ctext x="50" y="50" text-anchor="middle" dy=".3em" fill="%23ccc" font-size="14"%3E暂无图片%3C/text%3E%3C/svg%3E'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const searchKeyword = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const productList = ref([])
const categories = ref([])

const formData = reactive({
  id: null,
  name: '',
  categoryId: null,
  price: 0,
  stock: 0,
  mainImage: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const fetchCategories = async () => {
  try {
    const res = await productApi.getCategories()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
    }
    if (filterCategory.value) {
      params.categoryId = filterCategory.value
    }
    if (filterStatus.value !== '') {
      params.status = filterStatus.value
    }
    const res = await adminApi.getProductList(params)
    if (res.code === 200) {
      productList.value = res.data.records || res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

const showProductDialog = async (product = null) => {
  isEdit.value = !!product
  if (product) {
    try {
      const res = await productApi.getDetail(product.id)
      if (res.code === 200) {
        const detail = res.data
        formData.id = detail.id
        formData.name = detail.name
        formData.categoryId = detail.categoryId
        formData.price = detail.price
        formData.stock = detail.stock
        formData.mainImage = detail.mainImage || ''
        formData.description = detail.description || ''
        formData.status = detail.status
      }
    } catch (error) {
      ElMessage.error('获取商品详情失败')
      return
    }
  } else {
    formData.id = null
    formData.name = ''
    formData.categoryId = null
    formData.price = 0
    formData.stock = 0
    formData.mainImage = ''
    formData.description = ''
    formData.status = 1
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
      res = await adminApi.updateProduct(formData.id, formData)
    } else {
      res = await adminApi.addProduct(formData)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchProducts()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (product) => {
  const newStatus = product.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${action}该商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await adminApi.updateProductStatus(product.id, { status: newStatus })
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      fetchProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const deleteProduct = async (product) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await adminApi.deleteProduct(product.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<style lang="scss" scoped>
.products-page {
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

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.price {
  color: #C41E3A;
  font-weight: 500;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
