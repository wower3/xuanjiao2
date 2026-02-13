<!-- 流经事项页面 - 展示当前用户参与过的所有审批流程 -->
<template>
  <div class="flow-items-page">
    <el-card>
      <template #header>
        <span>流经事项</span>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-select
          v-model="businessTypeFilter"
          placeholder="全部类型"
          clearable
          style="width: 150px"
          @change="handleFilterChange"
        >
          <el-option label="素材录入" value="MATERIAL_ENTRY" />
          <el-option label="素材使用" value="ASSET_USAGE" />
          <el-option label="素材删除" value="ASSET_DELETION" />
        </el-select>
        <el-select
          v-model="statusFilter"
          placeholder="全部状态"
          clearable
          style="width: 150px"
          @change="handleFilterChange"
        >
          <el-option label="审批中" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-input
          v-model="keywordFilter"
          placeholder="搜索标题"
          style="width: 200px"
          clearable
          @keyup.enter="handleFilterChange"
        />
        <el-button type="primary" @click="handleFilterChange">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column label="工单ID" width="120">
          <template #default="{ row }">
            <span style="color: #409EFF; font-weight: 500;">AP-{{ row.applicationId || row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请标题" min-width="200">
          <template #default="{ row }">
            {{ row.applicationTitle || row.businessName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="workflowName" label="审批流程" width="150" />
        <el-table-column label="我的角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.myRole === 'initiator' ? 'primary' : 'success'" size="small">
              {{ row.myRole === 'initiator' ? '发起人' : '审批人' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getBusinessTypeTag(row.businessType)" size="small">
              {{ getBusinessTypeText(row.businessType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleOpenInstanceDetail(row)">详情</el-button>
            <el-button link type="success" @click="handleOpenNotifyDialog(row)">知会</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        @change="loadData"
      />
    </el-card>

    <!-- 详情对话框 -->
    <WorkOrderDetailDialog v-model="showInstanceDetailDialog" :instance-id="currentInstanceId" />

    <!-- 知会对话框 -->
    <NotifyDialog
      v-model="showNotifyDialog"
      :instance-id="currentInstance.id"
      :instance-title="currentInstance.title"
      :instance-status="currentInstance.status"
      @success="handleNotifySuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyFlowItems } from '@/api/flowItems'
import { getInstanceDetail } from '@/api/approval'
import NotifyDialog from '@/components/NotifyDialog.vue'
import WorkOrderDetailDialog from '@/components/WorkOrderDetailDialog.vue'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const businessTypeFilter = ref('')
const statusFilter = ref('')
const keywordFilter = ref('')
const query = reactive({ pageNum: 1, pageSize: 10 })
const showInstanceDetailDialog = ref(false)
const instanceDetail = ref<any>(null)
const showNotifyDialog = ref(false)
const currentInstance = ref<any>({ id: 0, title: '', status: '' })
const loadingDetail = ref(false)
const currentInstanceId = ref<number | null>(null)

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
    if (businessTypeFilter.value) {
      params.businessType = businessTypeFilter.value
    }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    if (keywordFilter.value) {
      params.keyword = keywordFilter.value
    }
    const res = await getMyFlowItems(params)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  query.pageNum = 1
  loadData()
}

function handleReset() {
  businessTypeFilter.value = ''
  statusFilter.value = ''
  keywordFilter.value = ''
  query.pageNum = 1
  loadData()
}

async function handleOpenInstanceDetail(row: any) {
  currentInstanceId.value = row.id || row.instanceId
  instanceDetail.value = row
  showInstanceDetailDialog.value = true
}

function handleOpenNotifyDialog(row: any) {
  currentInstance.value = {
    id: row.id,
    title: row.applicationTitle || row.businessName || '未知标题',
    status: row.status
  }
  showNotifyDialog.value = true
}

function handleNotifySuccess() {
  // 知会成功后刷新列表
  loadData()
}

function getBusinessTypeText(type: string) {
  const map: Record<string, string> = {
    MATERIAL_ENTRY: '素材录入',
    ASSET_USAGE: '素材使用',
    ASSET_DELETION: '素材删除'
  }
  return map[type] || type || '-'
}

function getBusinessTypeTag(type: string) {
  const map: Record<string, string> = {
    MATERIAL_ENTRY: 'primary',
    ASSET_USAGE: 'success',
    ASSET_DELETION: 'warning'
  }
  return map[type] || 'info'
}

function getStatusType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

function getStatusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '审批中',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    CANCELLED: '已取消',
    NOT_STARTED: '未开始',
    RETURNED: '已退回'
  }
  return map[status] || status || '-'
}

function getAssetStatusType(status: string) {
  const typeMap: Record<string, string> = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'AVAILABLE': 'success',
    'USED': 'info'
  }
  return typeMap[status] || 'info'
}

function getAssetStatusText(status: string) {
  const textMap: Record<string, string> = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已驳回',
    'AVAILABLE': '可用',
    'USED': '已使用'
  }
  return textMap[status] || status
}

onMounted(loadData)
</script>

<style scoped>
.flow-items-page {
  padding: 10px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.progress-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  background: #e6f7ff;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
}

.progress-item.active {
  border-left-color: #409EFF;
  background: #bae7ff;
}

.progress-item.approved {
  border-left-color: #67c23a;
  background: #d9f7be;
}

.progress-item.rejected {
  border-left-color: #f56c6c;
  background: #ffccc7;
}

.progress-item.returned {
  border-left-color: #e6a23c;
  background: #fff7e6;
}

.progress-item.not-started {
  border-left-color: #d9d9d9;
  background: #f5f5f5;
  opacity: 0.7;
}

.progress-stage-name {
  font-weight: 500;
  flex: 1;
}

.progress-status {
  margin-right: 15px;
}

.progress-approvers {
  flex: 1;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.approver-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 6px 10px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 4px;
}

.approver-name {
  font-weight: 500;
  font-size: 13px;
}

.approver-comment {
  padding: 4px 8px;
  background: #fff;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  border-left: 2px solid #E6A23C;
}

.comment-label {
  font-weight: bold;
  color: #909399;
  margin-right: 4px;
}

.approver-tag {
  font-size: 12px;
  padding: 2px 8px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.asset-list-header {
  font-weight: bold;
  color: #606266;
  margin-bottom: 10px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
}

h4 {
  margin: 15px 0 10px;
  color: #303133;
}
</style>
