<template>
  <div class="workflow-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>流程管理</span>
          <el-button type="primary" @click="$router.push('/workflow/design')">新建流程</el-button>
        </div>
      </template>
      <el-form :inline="true">
        <el-form-item label="流程类型">
          <el-select v-model="typeFilter" placeholder="全部" clearable @change="loadData">
            <el-option label="素材录入审批" value="ASSET_UPLOAD" />
            <el-option label="素材使用审批" value="ASSET_USAGE" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="name" label="流程名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <span v-if="row.workflowType">
              <el-tag v-if="row.workflowType === 'ASSET_UPLOAD'" type="primary">素材录入</el-tag>
              <el-tag v-else type="success">素材使用</el-tag>
            </span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="boundRoleId" label="绑定角色" width="150">
          <template #default="{ row }">
            <span v-if="row.boundRoleId">
              <el-tag type="info">{{ row.roleName || '-' }}</el-tag>
              <el-tag v-if="row.workflowType" size="small" style="margin-left: 5px">
                {{ row.workflowType === 'ASSET_UPLOAD' ? '素材录入' : '素材使用' }}
              </el-tag>
            </span>
            <span v-else style="color: #909399;">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="330">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/workflow/design/${row.id}`)">编辑</el-button>
            <el-button link type="success" @click="handleCopy(row)">复制</el-button>
            <el-button link type="primary" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="warning" @click="showBindDialog(row)">
              {{ row.boundRoleId ? '改绑角色' : '绑定角色' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 绑定角色对话框 -->
    <el-dialog v-model="showBind" title="绑定角色" width="400px">
      <el-form label-width="100px">
        <el-form-item label="流程名称">
          <span>{{ currentWorkflow?.name }}</span>
        </el-form-item>
        <el-form-item label="绑定角色">
          <el-select v-model="bindForm.roleId" placeholder="请选择角色" style="width: 100%;">
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="流程类型">
          <el-select v-model="bindForm.workflowType" placeholder="请选择流程类型" style="width: 100%;">
            <el-option label="素材录入审批" value="ASSET_UPLOAD" />
            <el-option label="素材使用审批" value="ASSET_USAGE" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="currentWorkflow?.boundRoleId" label="当前绑定">
          <el-tag type="info">{{ currentWorkflow.roleName }}</el-tag>
          <el-tag size="small" style="margin-left: 5px">
            {{ currentWorkflow.workflowType === 'ASSET_UPLOAD' ? '素材录入' : '素材使用' }}
          </el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBind = false">取消</el-button>
        <el-button v-if="currentWorkflow?.boundRoleId" type="danger" @click="handleUnbind" :loading="binding">解除绑定</el-button>
        <el-button type="primary" @click="handleBind" :loading="binding">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getWorkflowList,
  updateWorkflowStatus,
  deleteWorkflow,
  bindRole as bindRoleApi,
  unbindRole,
  copyWorkflow
} from '@/api/workflow'
import { getRoleList } from '@/api/role'

const loading = ref(false)
const list = ref([])
const typeFilter = ref('')
const showBind = ref(false)
const binding = ref(false)
const roleList = ref([])
const currentWorkflow = ref<any>(null)
const bindForm = ref({
  roleId: null as number | null,
  workflowType: ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await getWorkflowList()
    let workflows = res.data || []
    if (typeFilter.value) {
      workflows = workflows.filter((w: any) => w.workflowType === typeFilter.value)
    }
    list.value = workflows
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  try {
    const res = await getRoleList()
    roleList.value = res.data || []
  } catch (e) {
    console.error('加载角色列表失败', e)
  }
}

async function toggleStatus(row: any) {
  const newStatus = row.status === 1 ? 0 : 1
  await updateWorkflowStatus(row.id, newStatus)
  ElMessage.success('操作成功')
  loadData()
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定要删除流程"${row.name}"吗？删除后不可恢复！`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    await deleteWorkflow(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

async function handleCopy(row: any) {
  try {
    const res = await copyWorkflow(row.id)
    ElMessage.success('复制成功')
    loadData()
    // 可选：自动跳转到新流程的编辑页面
    // router.push(`/workflow/design/${res.data.id}`)
  } catch (e: any) {
    ElMessage.error(e.message || '复制失败')
  }
}

function showBindDialog(row: any) {
  currentWorkflow.value = row
  bindForm.value = {
    roleId: row.boundRoleId || null,
    workflowType: row.workflowType || ''
  }
  showBind.value = true
}

async function handleBind() {
  if (!bindForm.value.roleId) {
    ElMessage.warning('请选择角色')
    return
  }
  if (!bindForm.value.workflowType) {
    ElMessage.warning('请选择流程类型')
    return
  }

  binding.value = true
  try {
    await bindRoleApi({
      id: currentWorkflow.value.id,
      roleId: bindForm.value.roleId,
      workflowType: bindForm.value.workflowType
    })
    ElMessage.success('绑定成功')
    showBind.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '绑定失败')
  } finally {
    binding.value = false
  }
}

async function handleUnbind() {
  binding.value = true
  try {
    await unbindRole(currentWorkflow.value.id)
    ElMessage.success('解除绑定成功')
    showBind.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '解除绑定失败')
  } finally {
    binding.value = false
  }
}

onMounted(() => {
  loadData()
  loadRoles()
})
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
