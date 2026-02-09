<!-- 知会事项页面 - 展示当前用户收到的知会通知列表 -->
<template>
  <div class="notifications-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>知会事项</span>
          <div class="header-actions">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="unread-badge">
              <el-button type="primary" link @click="handleMarkAllAsRead" :loading="markingAll">
                全部已读
              </el-button>
            </el-badge>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-input
          v-model="keywordFilter"
          placeholder="搜索工单ID/申请标题/知会人"
          style="width: 250px"
          clearable
          @keyup.enter="handleFilterChange"
        />
        <el-select
          v-model="isReadFilter"
          placeholder="阅读状态"
          clearable
          style="width: 150px"
          @change="handleFilterChange"
        >
          <el-option label="未读" value="0" />
          <el-option label="已读" value="1" />
        </el-select>
        <el-button type="primary" @click="handleFilterChange">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="list" v-loading="loading" @row-click="handleRowClick">
        <el-table-column width="50">
          <template #default="{ row }">
            <div class="read-indicator" :class="{ unread: row.isRead !== 1 }">
              <el-icon v-if="row.isRead === 1"><SuccessFilled /></el-icon>
              <el-icon v-else><Message /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="工单ID" width="120">
          <template #default="{ row }">
            <span v-if="row.displayWorkOrderId" style="color: #409EFF; font-weight: 500;">
              {{ row.displayWorkOrderId }}
            </span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="申请标题" min-width="200">
          <template #default="{ row }">
            {{ row.displayTitle || row.title || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="workflowName" label="审批流程" width="150">
          <template #default="{ row }">
            {{ row.workflowName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="知会人" width="100">
          <template #default="{ row }">
            {{ row.senderName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="sourceTypeText" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getSourceTypeTag(row.sourceType)" size="small">
              {{ row.sourceTypeText || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.statusText || getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="handleViewDetail(row)">查看</el-button>
            <el-button link type="success" @click.stop="handleNotifyOthers(row)" :disabled="!row.instanceId">
              知会其他人
            </el-button>
            <el-tag :type="row.isRead === 1 ? 'success' : 'info'" size="small" style="margin-left: 8px">
              {{ row.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
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

    <!-- 工单详情对话框 -->
    <WorkOrderDetailDialog v-model="showDetailDialog" :instance-id="currentInstanceId" :current-user-id="userStore.userInfo?.id">
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button
          v-if="currentNotification && currentNotification.instanceId"
          type="primary"
          @click="handleNotifyOthersFromDialog"
        >
          知会其他人
        </el-button>
      </template>
    </WorkOrderDetailDialog>

    <!-- 知会其他人对话框 -->
    <el-dialog v-model="showNotifyDialog" title="知会其他人" width="600px" @closed="resetNotifyForm">
      <div v-loading="notifying">
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          style="margin-bottom: 15px"
        >
          选择要知会的用户，他们将收到关于此工单的通知。
        </el-alert>

        <div style="margin-bottom: 15px">
          <div style="font-weight: bold; margin-bottom: 10px; color: #606266">
            工单信息：
          </div>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="工单ID">
              {{ notifyForm.instanceId ? 'AP-' + notifyForm.instanceId : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="申请标题">
              {{ notifyForm.businessTitle || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div style="margin-bottom: 15px">
          <div style="font-weight: bold; margin-bottom: 10px; color: #606266">
            选择知会人：
          </div>
          <UserSelector
            v-model="notifyForm.recipientIds"
            :show-filters="true"
            placeholder="选择要知会的用户"
          />
        </div>

        <div>
          <div style="font-weight: bold; margin-bottom: 10px; color: #606266">
            附加消息（可选）：
          </div>
          <el-input
            v-model="notifyForm.message"
            type="textarea"
            :rows="3"
            placeholder="请输入附加消息"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="showNotifyDialog = false" :disabled="notifying">取消</el-button>
        <el-button type="primary" @click="submitNotify" :loading="notifying" :disabled="!notifyForm.recipientIds || notifyForm.recipientIds.length === 0">
          确认知会
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { SuccessFilled, Message, Clock, CircleCloseFilled, WarningFilled, Document, Folder, MoreFilled } from '@element-plus/icons-vue'
import {
  getNotificationListWithWorkOrder,
  getUnreadCount,
  markAsRead,
  batchMarkAsRead,
  markAllAsRead,
  notifyUsers
} from '@/api/notification'
import { getInstanceDetail } from '@/api/task'
import { useUserStore } from '@/stores/user'
import UserSelector from '@/components/UserSelector.vue'
import WorkOrderDetailDialog from '@/components/WorkOrderDetailDialog.vue'

const userStore = useUserStore()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const unreadCount = ref(0)
const keywordFilter = ref('')
const isReadFilter = ref('')
const query = reactive({ pageNum: 1, pageSize: 10 })
const showDetailDialog = ref(false)
const showNotifyDialog = ref(false)
const currentNotification = ref<any>(null)
const instanceDetail = ref<any>(null)
const loadingDetail = ref(false)
const markingIds = ref<number[]>([])
const markingAll = ref(false)
const notifying = ref(false)
const currentInstanceId = ref<number | null>(null)

// 知会表单
const notifyForm = reactive({
  instanceId: null as number | null,
  businessTitle: '',
  recipientIds: [] as number[],
  message: ''
})

// 分离主流程和子流程进度
const mainWorkflowProgress = computed(() => {
  if (!instanceDetail.value || !instanceDetail.value.approvalProgress) return []
  return instanceDetail.value.approvalProgress.filter((p: any) => p.isSubWorkflow !== 1)
})

const subWorkflowProgress = computed(() => {
  if (!instanceDetail.value || !instanceDetail.value.approvalProgress) return []
  return instanceDetail.value.approvalProgress.filter((p: any) => p.isSubWorkflow === 1)
})

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
    if (keywordFilter.value) {
      params.keyword = keywordFilter.value
    }
    if (isReadFilter.value !== '') {
      params.isRead = parseInt(isReadFilter.value)
    }
    const res = await getNotificationListWithWorkOrder(params)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch (e) {
    console.error('获取未读数量失败', e)
  }
}

function handleFilterChange() {
  query.pageNum = 1
  loadData()
}

function handleReset() {
  keywordFilter.value = ''
  isReadFilter.value = ''
  query.pageNum = 1
  loadData()
}

function handleRowClick(row: any) {
  handleViewDetail(row)
}

async function handleViewDetail(row: any) {
  currentNotification.value = row
  currentInstanceId.value = row.instanceId

  // 如果是未读状态，标记为已读
  if (row.isRead !== 1) {
    try {
      await markAsRead(row.id)
      row.isRead = 1
      await loadUnreadCount()
    } catch (e: any) {
      console.error('标记已读失败', e)
    }
  }

  showDetailDialog.value = true
}

// 打开知会对话框
function handleNotifyOthers(row: any) {
  if (!row.instanceId) {
    ElMessage.warning('该通知没有关联工单，无法知会其他人')
    return
  }
  notifyForm.instanceId = row.instanceId
  notifyForm.businessTitle = row.displayTitle || row.title || ''
  notifyForm.recipientIds = []
  notifyForm.message = ''
  showNotifyDialog.value = true
}

// 从详情对话框打开知会
function handleNotifyOthersFromDialog() {
  if (currentNotification.value && currentNotification.value.instanceId) {
    handleNotifyOthers(currentNotification.value)
  }
}

// 提交知会
async function submitNotify() {
  if (!notifyForm.recipientIds || notifyForm.recipientIds.length === 0) {
    ElMessage.warning('请选择要知会的用户')
    return
  }

  notifying.value = true
  try {
    await notifyUsers({
      instanceId: notifyForm.instanceId!,
      recipientIds: notifyForm.recipientIds,
      message: notifyForm.message || undefined
    })
    ElMessage.success('知会成功')
    showNotifyDialog.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '知会失败')
  } finally {
    notifying.value = false
  }
}

// 重置知会表单
function resetNotifyForm() {
  notifyForm.instanceId = null
  notifyForm.businessTitle = ''
  notifyForm.recipientIds = []
  notifyForm.message = ''
}

async function handleMarkAsRead(row: any) {
  markingIds.value.push(row.id)
  try {
    await markAsRead(row.id)
    row.isRead = 1
    await loadUnreadCount()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    markingIds.value = markingIds.value.filter(id => id !== row.id)
  }
}

async function handleMarkAllAsRead() {
  markingAll.value = true
  try {
    await markAllAsRead()
    await loadData()
    await loadUnreadCount()
    ElMessage.success('已将所有通知标记为已读')
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    markingAll.value = false
  }
}

function getSourceTypeTag(type: string) {
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
    CANCELLED: 'info',
    RETURNED: 'warning',
    NOT_STARTED: 'info'
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

onMounted(() => {
  loadData()
  loadUnreadCount()
})
</script>

<style scoped>
.notifications-page {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.unread-badge {
  margin-right: 15px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.read-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #f0f0f0;
  color: #909399;
}

.read-indicator.unread {
  background: #409EFF;
  color: #fff;
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
  transition: all 0.3s;
}

/* 子流程列表样式 */
.sub-workflow-list {
  padding-left: 0;
}

.sub-workflow-item {
  background: #fffbf0;
  border-left-color: #E6A23C;
}

.progress-item.active {
  border-left-color: #1890ff;
  background: #bae7ff;
}

.sub-workflow-item.active {
  background: #fff3e0;
  border-left-color: #FF9800;
}

.progress-item.approved {
  border-left-color: #52c41a;
  background: #d9f7be;
}

.sub-workflow-item.approved {
  background: #e8f5e9;
}

.progress-item.rejected {
  border-left-color: #ff4d4f;
  background: #ffccc7;
}

.sub-workflow-item.rejected {
  background: #ffebee;
}

.progress-item.returned {
  border-left-color: #FA8C16;
  background: #fff7e6;
}

.sub-workflow-item.returned {
  background: #fff7e6;
}

.progress-item.not-started {
  border-left-color: #d9d9d9;
  background: #f5f5f5;
  opacity: 0.7;
}

.sub-workflow-item.not-started {
  background: #ffebee;
}

.progress-icon {
  margin-right: 12px;
  font-size: 20px;
}

.progress-item.active .progress-icon {
  color: #1890ff;
}

.progress-item.approved .progress-icon {
  color: #52c41a;
}

.progress-item.rejected .progress-icon {
  color: #ff4d4f;
}

.progress-item.returned .progress-icon {
  color: #FA8C16;
}

.progress-content {
  flex: 1;
}

.progress-stage {
  font-weight: bold;
  margin-bottom: 5px;
}

.progress-approvers {
  margin: 5px 0;
  color: #606266;
}

.approver-item {
  margin-bottom: 8px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 4px;
}

.approver-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.approver-comment {
  margin-top: 4px;
  padding: 6px 10px;
  background: #fff;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  border-left: 2px solid #E6A23C;
}

.comment-label {
  font-weight: bold;
  color: #909399;
  margin-right: 4px;
}

.progress-status {
  display: flex;
  align-items: center;
}

/* 流程区域标题 */
.workflow-section-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(90deg, #e6f7ff 0%, #ffffff 100%);
  border-left: 4px solid #409EFF;
  border-radius: 4px;
  margin-bottom: 15px;
}

.workflow-section-header.sub-workflow {
  background: linear-gradient(90deg, #fffbf0 0%, #ffffff 100%);
  border-left-color: #E6A23C;
}

.workflow-section-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
}

h4 {
  margin: 15px 0 10px;
  color: #303133;
}
</style>
