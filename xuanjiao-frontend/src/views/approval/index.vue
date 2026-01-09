<template>
  <div class="approval-page">
    <el-card>
      <template #header>审批工单</template>
      <el-tabs v-model="activeTab" @tab-change="loadData">
        <el-tab-pane label="待我审批" name="pending" />
        <el-tab-pane label="我发起的" name="mine" />
      </el-tabs>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="businessName" label="素材名称" />
        <el-table-column prop="workflowName" label="审批流程" />
        <el-table-column prop="applicantName" label="申请人" v-if="activeTab === 'pending'" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" v-if="activeTab === 'pending'">
          <template #default="{ row }">
            <el-button link type="success" @click="handleApprove(row, true)">通过</el-button>
            <el-button link type="danger" @click="handleApprove(row, false)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showApproveDialog" :title="approveAction ? '审批通过' : '审批驳回'" width="400px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApproveDialog = false">取消</el-button>
        <el-button :type="approveAction ? 'success' : 'danger'" @click="submitApprove" :loading="submitting">
          {{ approveAction ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyTasks, getMyApplied, approve } from '@/api/approval'

const loading = ref(false)
const activeTab = ref('pending')
const list = ref([])
const showApproveDialog = ref(false)
const approveAction = ref(true)
const submitting = ref(false)
const currentTask = ref<any>(null)
const approveForm = reactive({ comment: '' })

async function loadData() {
  loading.value = true
  try {
    const api = activeTab.value === 'pending' ? getMyTasks : getMyApplied
    const res = await api({ pageNum: 1, pageSize: 20 })
    list.value = res.data?.list || []
  } finally {
    loading.value = false
  }
}

function handleApprove(row: any, passed: boolean) {
  currentTask.value = row
  approveAction.value = passed
  approveForm.comment = ''
  showApproveDialog.value = true
}

async function submitApprove() {
  submitting.value = true
  try {
    await approve(currentTask.value.id, approveForm.comment, approveAction.value)
    ElMessage.success(approveAction.value ? '审批通过' : '已驳回')
    showApproveDialog.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

function getStatusType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

function getStatusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '审批中',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

onMounted(loadData)
</script>
