<template>
  <el-dialog v-model="visible" title="工单详情" width="1000px" @close="handleClose">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 工单详情标签页 -->
      <el-tab-pane label="工单详情" name="detail">
        <div v-loading="loading">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="申请单ID" :span="2">
              {{ workOrderDetail.displayId || ('AP-' + workOrderDetail.instanceId) }}
            </el-descriptions-item>
            <el-descriptions-item label="申请标题" :span="2">
              {{ workOrderDetail.displayTitle || workOrderDetail.title }}
            </el-descriptions-item>
            <el-descriptions-item label="申请人">
              {{ workOrderDetail.applicantName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="审批流程">
              {{ workOrderDetail.workflowName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusType(workOrderDetail.status)" size="small">
                {{ getStatusText(workOrderDetail.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请时间">
              {{ workOrderDetail.createTime }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 素材文件列表 -->
          <div v-if="workOrderDetail.assets && workOrderDetail.assets.length > 0" style="margin-top: 20px">
            <h4>素材文件 ({{ workOrderDetail.assets.length }})</h4>
            <el-table :data="workOrderDetail.assets" size="small" style="margin-top: 10px">
              <el-table-column label="预览" width="80">
                <template #default="{ row }">
                  <el-image
                    v-if="row.type === 'IMAGE'"
                    :src="row.filePath ? `/api/asset/preview/${row.id}` : ''"
                    fit="cover"
                    style="width: 60px; height: 40px"
                  />
                  <el-icon v-else-if="row.type === 'VIDEO'" :size="24"><VideoCamera /></el-icon>
                  <el-icon v-else :size="24"><Document /></el-icon>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="文件名称" />
              <el-table-column prop="type" label="类型" width="80" />
              <el-table-column label="标签" width="150">
                <template #default="{ row }">
                  <el-tag
                    v-for="tag in (row.tags || [])"
                    :key="tag.id"
                    size="small"
                    style="margin-right: 5px"
                  >
                    {{ tag.name }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明" show-overflow-tooltip />
              <el-table-column prop="publishChannel" label="发布渠道" width="120" />
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="getAssetStatusType(row.status)" size="small">
                    {{ getAssetStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 审批进度 -->
          <div v-if="workOrderDetail.approvalProgress && workOrderDetail.approvalProgress.length > 0" style="margin-top: 20px">
            <!-- 主流程进度 -->
            <div v-if="mainWorkflowProgress.length > 0" style="margin-bottom: 25px">
              <div class="workflow-section-header">
                <el-icon style="color: #409EFF; margin-right: 8px;"><Document /></el-icon>
                <span class="workflow-section-title">主流程审批进度</span>
              </div>
              <div class="progress-list">
                <div
                  v-for="(progress, index) in mainWorkflowProgress"
                  :key="'main-' + (progress.id || progress.stageId)"
                  class="progress-item"
                  :class="{
                    'active': progress.status === 'PENDING',
                    'approved': progress.status === 'APPROVED',
                    'rejected': progress.status === 'REJECTED',
                    'returned': progress.status === 'RETURNED',
                    'not-started': progress.status === 'NOT_STARTED'
                  }"
                >
                  <div class="progress-icon">
                    <el-icon v-if="progress.status === 'PENDING'"><Clock /></el-icon>
                    <el-icon v-else-if="progress.status === 'APPROVED'"><SuccessFilled /></el-icon>
                    <el-icon v-else-if="progress.status === 'REJECTED'"><CircleCloseFilled /></el-icon>
                    <el-icon v-else-if="progress.status === 'RETURNED'"><WarningFilled /></el-icon>
                    <el-icon v-else><MoreFilled /></el-icon>
                  </div>
                  <div class="progress-content">
                    <div class="progress-stage">
                      {{ progress.stageName }}
                      <el-tag
                        v-if="progress.status"
                        :type="getStatusType(progress.status)"
                        size="small"
                        style="margin-left: 8px"
                      >
                        {{ getStatusText(progress.status) }}
                      </el-tag>
                    </div>
                    <div v-if="progress.approvers && progress.approvers.length > 0" class="progress-approvers">
                      <div v-for="approver in progress.approvers" :key="approver.id" class="approver-item">
                        <div class="approver-name">
                          {{ approver.name }}
                          <span v-if="approver.status === 'APPROVED'" style="color: #67C23A;">✓</span>
                          <span v-else-if="approver.status === 'REJECTED'" style="color: #F56C6C;">✗</span>
                          <span v-else style="color: #909399;">待审批</span>
                        </div>
                        <div v-if="approver.comment" class="approver-comment">
                          <span class="comment-label">意见:</span> {{ approver.comment }}
                        </div>
                      </div>
                    </div>
                    <div v-else-if="progress.status === 'NOT_STARTED'" class="progress-approvers" style="color: #909399; font-style: italic;">
                      尚未到达此阶段
                    </div>
                    <div class="progress-status">
                      <span v-if="progress.approveTime" style="color: #909399; font-size: 12px">
                        {{ progress.approveTime }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 子流程进度 -->
            <div v-if="subWorkflowProgress.length > 0">
              <div class="workflow-section-header sub-workflow">
                <el-icon style="color: #E6A23C; margin-right: 8px;"><Folder /></el-icon>
                <span class="workflow-section-title">子流程审批进度</span>
              </div>
              <div class="progress-list sub-workflow-list">
                <div
                  v-for="(progress, index) in subWorkflowProgress"
                  :key="'sub-' + (progress.id || progress.stageId)"
                  class="progress-item sub-workflow-item"
                  :class="{
                    'active': progress.status === 'PENDING',
                    'approved': progress.status === 'APPROVED',
                    'rejected': progress.status === 'REJECTED',
                    'returned': progress.status === 'RETURNED',
                    'not-started': progress.status === 'NOT_STARTED'
                  }"
                >
                  <div class="progress-icon">
                    <el-icon v-if="progress.status === 'PENDING'"><Clock /></el-icon>
                    <el-icon v-else-if="progress.status === 'APPROVED'"><SuccessFilled /></el-icon>
                    <el-icon v-else-if="progress.status === 'REJECTED'"><CircleCloseFilled /></el-icon>
                    <el-icon v-else-if="progress.status === 'RETURNED'"><WarningFilled /></el-icon>
                    <el-icon v-else><MoreFilled /></el-icon>
                  </div>
                  <div class="progress-content">
                    <div class="progress-stage">
                      {{ progress.stageName }}
                      <el-tag
                        v-if="progress.status"
                        :type="getStatusType(progress.status)"
                        size="small"
                        style="margin-left: 8px"
                      >
                        {{ getStatusText(progress.status) }}
                      </el-tag>
                    </div>
                    <div v-if="progress.approvers && progress.approvers.length > 0" class="progress-approvers">
                      <div v-for="approver in progress.approvers" :key="approver.id" class="approver-item">
                        <div class="approver-name">
                          {{ approver.name }}
                          <span v-if="approver.status === 'APPROVED'" style="color: #67C23A;">✓</span>
                          <span v-else-if="approver.status === 'REJECTED'" style="color: #F56C6C;">✗</span>
                          <span v-else style="color: #909399;">待审批</span>
                        </div>
                        <div v-if="approver.comment" class="approver-comment">
                          <span class="comment-label">意见:</span> {{ approver.comment }}
                        </div>
                      </div>
                    </div>
                    <div v-else-if="progress.status === 'NOT_STARTED'" class="progress-approvers" style="color: #909399; font-style: italic;">
                      尚未到达此阶段
                    </div>
                    <div class="progress-status">
                      <span v-if="progress.approveTime" style="color: #909399; font-size: 12px">
                        {{ progress.approveTime }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 当前待审批人 -->
          <div v-if="workOrderDetail.pendingApprovers && workOrderDetail.pendingApprovers.length > 0" style="margin-top: 20px">
            <h4>当前待审批人</h4>
            <div>
              <el-tag
                v-for="approver in workOrderDetail.pendingApprovers"
                :key="approver.id"
                type="warning"
                style="margin-right: 10px"
              >
                {{ approver.name }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 知会记录标签页 -->
      <el-tab-pane label="知会记录" name="notification-records">
        <div v-loading="loadingRecords">
          <el-table :data="notificationRecords" size="small" v-if="notificationRecords.length > 0">
            <el-table-column label="知会发起人" width="150">
              <template #default="{ row }">
                <div>{{ row.senderName }}</div>
                <div style="color: #909399; font-size: 12px;">{{ row.senderDeptName || '-' }}</div>
              </template>
            </el-table-column>
            <el-table-column label="知会收件人" width="150">
              <template #default="{ row }">
                <div>{{ row.recipientName }}</div>
                <div style="color: #909399; font-size: 12px;">{{ row.recipientDeptName || '-' }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="通知标题" min-width="200" show-overflow-tooltip />
            <el-table-column label="阅读状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isRead === 1 ? 'success' : 'info'" size="small">
                  {{ row.readStatusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发起时间" width="180" />
          </el-table>
          <el-empty v-else description="暂无知会记录" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <slot name="footer">
        <el-button @click="handleClose">关闭</el-button>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { Clock, SuccessFilled, CircleCloseFilled, WarningFilled, Document, Folder, MoreFilled, VideoCamera } from '@element-plus/icons-vue'
import { getInstanceDetail } from '@/api/task'
import { getNotificationRecords } from '@/api/notification'

interface Props {
  modelValue: boolean
  instanceId: number | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(false)
const activeTab = ref('detail')
const loading = ref(false)
const loadingRecords = ref(false)
const workOrderDetail = ref<any>({})
const notificationRecords = ref<any[]>([])

// 分离主流程和子流程进度
const mainWorkflowProgress = computed(() => {
  if (!workOrderDetail.value || !workOrderDetail.value.approvalProgress) return []
  return workOrderDetail.value.approvalProgress.filter((p: any) => p.isSubWorkflow !== 1)
})

const subWorkflowProgress = computed(() => {
  if (!workOrderDetail.value || !workOrderDetail.value.approvalProgress) return []
  return workOrderDetail.value.approvalProgress.filter((p: any) => p.isSubWorkflow === 1)
})

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
  if (newVal && props.instanceId) {
    loadWorkOrderDetail()
    loadNotificationRecords()
  }
})

// 监听 visible 变化，同步到父组件
watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

async function loadWorkOrderDetail() {
  if (!props.instanceId) return

  loading.value = true
  try {
    const res = await getInstanceDetail(props.instanceId)
    workOrderDetail.value = res.data || {}
  } catch (e: any) {
    console.error('加载工单详情失败', e)
  } finally {
    loading.value = false
  }
}

async function loadNotificationRecords() {
  if (!props.instanceId) return

  loadingRecords.value = true
  try {
    const res = await getNotificationRecords(props.instanceId)
    notificationRecords.value = res.data || []
  } catch (e: any) {
    console.error('加载知会记录失败', e)
    notificationRecords.value = []
  } finally {
    loadingRecords.value = false
  }
}

function handleClose() {
  visible.value = false
  activeTab.value = 'detail'
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

function getAssetStatusType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

function getAssetStatusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return map[status] || status || '-'
}
</script>

<style scoped>
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
