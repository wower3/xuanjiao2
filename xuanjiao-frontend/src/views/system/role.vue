<!--
/**
 * 角色管理页面
 * <p>提供系统角色的增删改查和权限配置功能</p>
 * <p>支持新增/编辑角色，配置角色类型（大写字母、数字、下划线）</p>
 * <p>支持为角色配置菜单权限，支持启用/禁用和删除角色</p>
 *
 * @author system
 * @version 1.0
 */
<template>
  <div class="role-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="roleType" label="角色类型" width="200">
          <template #default="{ row }">
            <el-tag>{{ row.roleType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleConfigMenu(row)">配置权限</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑角色' : '新增角色'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色类型" prop="roleType">
          <el-input
            v-model="form.roleType"
            placeholder="请输入角色类型（大写字母、数字、下划线）"
            :disabled="isEdit"
          >
            <template #append>
              <el-tooltip content="角色类型只能包含大写字母、数字和下划线，如：FINANCE_ADMIN" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
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

    <!-- 配置菜单权限对话框 -->
    <el-dialog v-model="showMenuDialog" title="配置菜单权限" width="500px">
      <div class="menu-tree-container">
        <el-tree
          ref="menuTreeRef"
          :data="menuTree"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          show-checkbox
          default-expand-all
        />
      </div>
      <template #footer>
        <el-button @click="showMenuDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenus" :loading="savingMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getRoleList, getRoleById, createRole, updateRole, deleteRole, assignRoleMenus, getRoleMenus } from '@/api/role'
import { getMenuTree } from '@/api/menu'
import { ElMessage, ElMessageBox } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const list = ref([])
const showDialog = ref(false)
const showMenuDialog = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const savingMenus = ref(false)
const formRef = ref()
const menuTreeRef = ref()
const menuTree = ref([])
const currentRoleId = ref<number | null>(null)

// roleType 格式校验：大写字母、数字、下划线
const roleTypePattern = /^[A-Z0-9_]+$/

const form = reactive({
  id: null as number | null,
  name: '',
  roleType: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleType: [
    { required: true, message: '请输入角色类型', trigger: 'blur' },
    {
      pattern: roleTypePattern,
      message: '角色类型只能包含大写字母、数字和下划线',
      trigger: 'blur'
    }
  ]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getRoleList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadMenuTree() {
  const res = await getMenuTree()
  menuTree.value = res.data || []
}

function handleAdd() {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.roleType = ''
  form.description = ''
  form.status = 1
  showDialog.value = true
}

async function handleEdit(row: any) {
  isEdit.value = true
  const res = await getRoleById(row.id)
  Object.assign(form, res.data)
  showDialog.value = true
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm('确定删除该角色?', '提示')
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateRole(form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
      ElMessage.success('新增成功')
    }
    showDialog.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleConfigMenu(row: any) {
  currentRoleId.value = row.id
  showMenuDialog.value = true
  // 加载角色已有的菜单权限
  try {
    const res = await getRoleMenus(row.id)
    const menuIds = res.data || []
    // 设置选中的菜单
    setTimeout(() => {
      menuTreeRef.value?.setCheckedKeys(menuIds)
    }, 100)
  } catch (e) {
    console.error('加载菜单权限失败', e)
  }
}

async function handleSaveMenus() {
  savingMenus.value = true
  try {
    const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
    await assignRoleMenus(currentRoleId.value!, checkedKeys)
    ElMessage.success('保存成功')
    showMenuDialog.value = false
  } finally {
    savingMenus.value = false
  }
}

onMounted(() => {
  loadData()
  loadMenuTree()
})
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.menu-tree-container { max-height: 400px; overflow-y: auto; border: 1px solid #dcdfe6; padding: 10px; border-radius: 4px; }
</style>
