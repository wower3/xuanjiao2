<template>
  <div class="user-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>

      <!-- 筛选条件区域 -->
      <div class="filter-section">
        <el-row :gutter="16">
          <el-col :span="6">
            <div class="filter-item">
              <label>用户类型</label>
              <el-select
                v-model="filter.roleIds"
                multiple
                collapse-tags
                collapse-tags-tooltip
                placeholder="全部角色"
                clearable
                @change="handleFilterChange"
                style="width: 100%"
              >
                <el-option
                  v-for="role in roleList"
                  :key="role.id"
                  :label="role.name"
                  :value="role.id"
                />
              </el-select>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="filter-item">
              <label>部门</label>
              <el-tree-select
                v-model="filter.deptId"
                :data="deptTree"
                :props="{ label: 'name', value: 'id' }"
                placeholder="全部部门"
                clearable
                check-strictly
                @change="handleFilterChange"
                style="width: 100%"
              />
            </div>
          </el-col>
          <el-col :span="6">
            <div class="filter-item">
              <label>&nbsp;</label>
              <div class="checkbox-wrapper">
                <el-checkbox
                  v-model="filter.includeSubDept"
                  :disabled="!filter.deptId"
                  @change="handleFilterChange"
                >
                  包含子部门
                </el-checkbox>
              </div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="filter-item">
              <label>&nbsp;</label>
              <el-button @click="handleResetFilter">重置</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-table :data="list" v-loading="loading" style="margin-top: 16px">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="deptName" label="部门" width="150" />
        <el-table-column prop="roleName" label="角色" width="150" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑用户' : '新增用户'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username" v-if="!isEdit">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择部门"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" clearable>
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
import {
  getUserListWithFilter,
  getDefaultFilterDept,
  createUser,
  updateUser,
  deleteUser
} from '@/api/user'
import { getDeptTree } from '@/api/dept'
import { getRoleList } from '@/api/role'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const roleList = ref([])

// 筛选条件
const filter = reactive({
  roleIds: [] as number[],
  deptId: null as number | null,
  includeSubDept: true
})

// 默认筛选配置
const defaultFilter = ref({
  hasFilter: false,
  deptId: null as number | null,
  includeSubDept: true,
  canAssignAllRoles: false,
  allowedDeptIds: null as number[] | null,
  rootDeptId: null as number | null // 分消保管理岗的根部门ID（二级机构）
})

// 原始完整的部门树（用于过滤）
const rawDeptTree = ref([])

// 过滤后的部门树（用于选择器）
const deptTree = ref([])

// 根据权限重构部门树，以指定的根部门为起点
function buildDeptTreeFromRoot(tree: any[], rootId: number): any[] {
  console.log('[buildDeptTreeFromRoot] 输入 - rootId:', rootId, 'rootId类型:', typeof rootId)
  console.log('[buildDeptTreeFromRoot] 原始树结构:', JSON.stringify(tree, null, 2))

  // 查找根部门节点（使用宽松比较以处理类型不匹配）
  function findNode(nodes: any[], id: number): any {
    for (const node of nodes) {
      console.log('[findNode] 检查节点:', node.id, typeof node.id, 'vs', id, typeof id)
      // 使用 == 进行宽松比较，处理字符串/数字类型不匹配
      if (node.id == id) return node
      if (node.children && node.children.length > 0) {
        const found = findNode(node.children, id)
        if (found) return found
      }
    }
    return null
  }

  const rootNode = findNode(tree, rootId)
  console.log('[buildDeptTreeFromRoot] 找到的根节点:', rootNode)

  if (!rootNode) {
    console.warn('[buildDeptTreeFromRoot] 未找到根节点，rootId:', rootId)
    // 返回空数组而不是原始树，这样用户可以看到问题
    return []
  }

  // 克隆根节点及其子树
  const result = [cloneNode(rootNode)]
  console.log('[buildDeptTreeFromRoot] 构建的结果树:', JSON.stringify(result, null, 2))
  return result
}

// 递归克隆节点
function cloneNode(node: any): any {
  const cloned = { ...node }
  if (node.children && node.children.length > 0) {
    cloned.children = node.children.map((child: any) => cloneNode(child))
  }
  return cloned
}

const form = reactive({
  id: null as number | null,
  username: '',
  realName: '',
  deptId: null as number | null,
  roleId: null as number | null,
  phone: '',
  email: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    // 构建查询参数
    const params: {
      roleIds?: number[]
      deptId?: number
      includeSubDept?: boolean
    } = {}

    if (filter.roleIds && filter.roleIds.length > 0) {
      params.roleIds = filter.roleIds
    }
    if (filter.deptId !== null) {
      params.deptId = filter.deptId
      params.includeSubDept = filter.includeSubDept
    }

    const res = await getUserListWithFilter(params)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadDefaultFilter() {
  try {
    console.log('[loadDefaultFilter] 开始获取默认筛选条件')
    const res = await getDefaultFilterDept()
    console.log('[loadDefaultFilter] API响应:', res.data)

    if (res.data && res.data.hasFilter) {
      defaultFilter.value = res.data
      // 设置默认筛选值
      filter.deptId = res.data.deptId
      filter.includeSubDept = res.data.includeSubDept !== false

      console.log('[loadDefaultFilter] hasFilter=true, deptId:', res.data.deptId, 'rootDeptId:', res.data.rootDeptId)

      // 对于分消保管理岗，以其二级机构为根构建部门树
      if (res.data.rootDeptId) {
        console.log('[loadDefaultFilter] 开始构建部门树，rawDeptTree:', rawDeptTree.value)
        deptTree.value = buildDeptTreeFromRoot(rawDeptTree.value, res.data.rootDeptId)
        console.log('[loadDefaultFilter] 构建后的deptTree:', deptTree.value)
      } else {
        console.log('[loadDefaultFilter] rootDeptId为空，使用原始部门树')
      }
    } else {
      console.log('[loadDefaultFilter] hasFilter=false，使用原始部门树')
    }
  } catch (error) {
    // 忽略错误，可能用户未登录
    console.error('[loadDefaultFilter] 获取默认筛选条件失败', error)
  }
}

async function loadDeptTree() {
  console.log('[loadDeptTree] 开始加载部门树')
  const res = await getDeptTree()
  console.log('[loadDeptTree] API响应:', res.data)
  rawDeptTree.value = res.data || []
  // 默认使用原始树，如果有权限限制会在 loadDefaultFilter 中重建
  deptTree.value = rawDeptTree.value
  console.log('[loadDeptTree] 初始化deptTree:', deptTree.value)
}

async function loadRoleList() {
  const res = await getRoleList()
  roleList.value = res.data || []
}

function handleFilterChange() {
  loadData()
}

function handleResetFilter() {
  filter.roleIds = []
  filter.deptId = defaultFilter.value.hasFilter ? defaultFilter.value.deptId : null
  filter.includeSubDept = defaultFilter.value.includeSubDept !== false
  loadData()
}

function handleAdd() {
  isEdit.value = false
  form.id = null
  form.username = ''
  form.realName = ''
  form.deptId = null
  form.roleId = null
  form.phone = ''
  form.email = ''
  form.status = 1
  showDialog.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  form.id = row.id
  form.username = row.username
  form.realName = row.realName
  form.deptId = row.deptId
  form.roleId = row.roleId
  form.phone = row.phone
  form.email = row.email
  form.status = row.status
  showDialog.value = true
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm('确定删除该用户?', '提示')
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUser(form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('新增成功，默认密码123456')
    }
    showDialog.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadDeptTree()
  await loadRoleList()
  await loadDefaultFilter()
  await loadData()
})
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.filter-item {
  display: flex;
  flex-direction: column;
}

.filter-item label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  height: 32px;
}
</style>
