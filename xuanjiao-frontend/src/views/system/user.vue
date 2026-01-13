<template>
  <div class="user-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      <el-table :data="list" v-loading="loading">
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
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'
import { getDeptTree } from '@/api/dept'
import { getRoleList } from '@/api/role'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const deptTree = ref([])
const roleList = ref([])
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
    const res = await getUserList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadDeptTree() {
  const res = await getDeptTree()
  deptTree.value = res.data || []
}

async function loadRoleList() {
  const res = await getRoleList()
  roleList.value = res.data || []
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

onMounted(() => {
  loadData()
  loadDeptTree()
  loadRoleList()
})
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
