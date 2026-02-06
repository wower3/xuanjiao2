<!--
/**
 * 部门管理页面
 * <p>提供组织架构的树形展示和增删改查功能</p>
 * <p>支持多级部门管理，自动生成部门编号</p>
 * <p>支持部门的启用/禁用、排序管理</p>
 *
 * @author system
 * @version 1.0
 */
<template>
  <div class="dept-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>部门管理</span>
          <el-button type="primary" @click="handleAdd(null)">新增部门</el-button>
        </div>
      </template>
      <el-table
        :data="deptTree"
        row-key="id"
        :tree-props="{ children: 'children' }"
        :default-expand-all="false"
        v-loading="loading"
      >
        <el-table-column prop="name" label="部门名称" width="200" />
        <el-table-column prop="code" label="部门编号" width="120" />
        <el-table-column prop="level" label="层级" width="80">
          <template #default="{ row }">
            <el-tag size="small">第{{ row.level }}级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fullCode" label="完整编号" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAdd(row)">新增子部门</el-button>
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
      width="500px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder="选择上级部门（不选则为根部门）"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编号">
          <el-input v-model="form.code" placeholder="留空则自动生成">
            <template #append>
              <el-button @click="handleGenerateCode" :loading="generating">生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="排序">
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
import { getDeptTree, saveDept, updateDept, deleteDept, generateDeptCode } from '@/api/dept'

const loading = ref(false)
const deptTree = ref([])
const showDialog = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const generating = ref(false)
const formRef = ref()
const form = reactive({
  id: null as any,
  parentId: null,
  name: '',
  code: '',
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getDeptTree()
    deptTree.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd(row: any) {
  dialogTitle.value = row ? '新增子部门' : '新增部门'
  form.id = null
  form.parentId = row ? row.id : 0
  form.name = ''
  form.code = ''
  form.sort = 0
  form.status = 1
  showDialog.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑部门'
  Object.assign(form, row)
  showDialog.value = true
}

async function handleDelete(row: any) {
  // 检查是否有子部门
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该部门存在子部门，无法删除')
    return
  }
  await ElMessageBox.confirm('确定删除该部门吗？', '提示')
  await deleteDept(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleGenerateCode() {
  generating.value = true
  try {
    const res = await generateDeptCode()
    form.code = res.data
  } finally {
    generating.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateDept(form)
    } else {
      await saveDept(form)
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
