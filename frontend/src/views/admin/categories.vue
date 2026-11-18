<template>
  <div class="category-page">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加分类
      </el-button>
    </div>

    <el-table
      :data="categoryList"
      v-loading="loading"
      row-key="id"
      border
      default-expand-all
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="name" label="分类名称" min-width="200" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="图标" width="80">
        <template #default="{ row }">
          <el-image
            v-if="row.icon"
            :src="row.icon"
            style="width: 40px; height: 40px"
            fit="contain"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="level" label="层级" width="80">
        <template #default="{ row }">
          <el-tag size="small">{{ row.level }}级</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
          <el-button link type="primary" @click="showAddChildDialog(row)" v-if="row.level < 3">
            添加子分类
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类" prop="parentId">
          <el-cascader
            v-model="form.parentIds"
            :options="categoryOptions"
            :props="{
              checkStrictly: true,
              value: 'id',
              label: 'name',
              children: 'children',
              emitPath: false
            }"
            clearable
            placeholder="请选择上级分类（不选则为一级分类）"
            style="width: 100%"
            @change="handleParentChange"
          />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="请输入图标URL" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const categoryList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)

const form = reactive({
  name: '',
  parentId: 0,
  parentIds: [],
  icon: '',
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑分类' : '添加分类'))

const categoryOptions = computed(() => {
  const buildOptions = (list) => {
    return list.map(item => ({
      id: item.id,
      name: item.name,
      children: item.children && item.children.length > 0 ? buildOptions(item.children) : undefined
    }))
  }
  return buildOptions(categoryList.value)
})

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await adminApi.getCategoryList()
    if (res.code === 200) {
      const buildTree = (list) => {
        const map = {}
        const roots = []
        
        list.forEach(item => {
          map[item.id] = { ...item, children: [] }
        })
        
        list.forEach(item => {
          if (item.parentId === 0 || !map[item.parentId]) {
            roots.push(map[item.id])
          } else {
            map[item.parentId].children.push(map[item.id])
          }
        })
        
        const removeEmptyChildren = (nodes) => {
          nodes.forEach(node => {
            if (node.children && node.children.length === 0) {
              delete node.children
            } else if (node.children) {
              removeEmptyChildren(node.children)
            }
          })
        }
        removeEmptyChildren(roots)
        
        return roots
      }
      
      categoryList.value = buildTree(res.data)
    }
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.name = ''
  form.parentId = 0
  form.parentIds = []
  form.icon = ''
  form.sort = 0
  form.status = 1
}

const showAddDialog = () => {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

const showAddChildDialog = (row) => {
  isEdit.value = false
  editId.value = null
  resetForm()
  form.parentId = row.id
  form.parentIds = [row.id]
  form.level = row.level + 1
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.name = row.name
  form.parentId = row.parentId || 0
  form.parentIds = row.parentId ? [row.parentId] : []
  form.icon = row.icon || ''
  form.sort = row.sort || 0
  form.status = row.status
  dialogVisible.value = true
}

const handleParentChange = (val) => {
  form.parentId = val || 0
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      name: form.name,
      parentId: form.parentId,
      icon: form.icon,
      sort: form.sort,
      status: form.status
    }

    if (isEdit.value) {
      await adminApi.updateCategory(editId.value, data)
      ElMessage.success('更新成功')
    } else {
      await adminApi.addCategory(data)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    fetchCategories()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该分类下存在子分类，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await adminApi.deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}
</style>
