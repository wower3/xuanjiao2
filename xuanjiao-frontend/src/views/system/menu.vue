<template>
  <div class="menu-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>菜单管理</span>
          <el-button type="primary" @click="handleAdd(null)">新增菜单</el-button>
        </div>
      </template>
      <el-table
        :data="menuTree"
        row-key="id"
        :tree-props="{ children: 'children', children: 'children' }"
        :default-expand-all="false"
        v-loading="loading"
      >
        <el-table-column prop="name" label="菜单名称" width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'MENU' ? 'primary' : 'info'" size="small">
              {{ row.type === 'MENU' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" />
        <el-table-column prop="component" label="组件路径" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAdd(row)">新增子菜单</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder="选择上级菜单（不选则为根菜单）"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path" v-if="form.type === 'MENU'">
          <el-input v-model="form.path" placeholder="如：/system/user" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.type === 'MENU'">
          <el-input v-model="form.component" placeholder="如：system/user" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="如：User" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, saveMenu, updateMenu, deleteMenu } from '@/api/menu'

const loading = ref(false)
const menuTree = ref([])
const showDialog = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null as any,
  parentId: null,
  type: 'MENU',
  name: '',
  path: '',
  component: '',
  icon: '',
  sort: 0,
  status: 1
})

const rules = {
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getMenuTree()
    menuTree.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd(row: any) {
  dialogTitle.value = row ? '新增子菜单' : '新增菜单'
  form.id = null
  form.parentId = row ? row.id : 0
  form.type = 'MENU'
  form.name = ''
  form.path = ''
  form.component = ''
  form.icon = ''
  form.sort = 0
  form.status = 1
  showDialog.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑菜单'
  Object.assign(form, row)
  showDialog.value = true
}

async function handleDelete(row: any) {
  // 检查是否有子菜单
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该菜单存在子菜单，无法删除')
    return
  }
  await ElMessageBox.confirm('确定删除该菜单吗？', '提示')
  await deleteMenu(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateMenu(form)
    } else {
      await saveMenu(form)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

onMounted(loadData)
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
