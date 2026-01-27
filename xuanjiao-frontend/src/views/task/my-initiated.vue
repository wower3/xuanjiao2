<template>
  <div class="my-initiated-page">
    <el-card>
      <template #header>
        <span>我发起的</span>
      </template>

      <!-- 状态筛选 -->
      <div style="margin-bottom: 15px; display: flex; align-items: center; gap: 10px;">
        <span style="color: #606266; font-size: 14px;">筛选状态：</span>
        <el-select
          v-model="statusFilter"
          placeholder="全部状态"
          clearable
          style="width: 150px"
          @change="handleStatusFilterChange"
        >
          <el-option label="审批中" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column label="申请单ID" width="120">
          <template #default="{ row }">
            <span style="color: #409EFF; font-weight: 500;">AP-{{ row.applicationId || row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请标题" min-width="200" prop="businessName" />
        <el-table-column prop="workflowName" label="审批流程" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleOpenInstanceDetail(row)">详情</el-button>
            <el-button v-if="row.businessType === 'MATERIAL_ENTRY'" link type="success" @click="handleCopyApplication(row)" :loading="copying">复制申请单</el-button>
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

    <!-- 我发起的详情对话框 -->
    <el-dialog v-model="showInstanceDetailDialog" title="工单详情" width="1000px">
      <div v-if="instanceDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请单ID" :span="2">
            AP-{{ instanceDetail.applicationId || instanceDetail.id }}
          </el-descriptions-item>
          <el-descriptions-item label="申请标题" :span="2">
            {{ instanceDetail.applicationTitle || instanceDetail.businessName }}
          </el-descriptions-item>
          <el-descriptions-item label="发起人">
            {{ instanceDetail.applicantName }}
          </el-descriptions-item>
          <el-descriptions-item label="审批流程">
            {{ instanceDetail.workflowName }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(instanceDetail.status)" size="small">
              {{ getStatusText(instanceDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前阶段">
            {{ instanceDetail.currentStageName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ instanceDetail.createTime }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 素材信息 -->
        <div v-if="instanceDetail.assetType || instanceDetail.assetCount" style="margin-top: 20px">
          <h4>素材信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="素材类型">
              {{ instanceDetail.assetType || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="素材数量">
              {{ instanceDetail.assetCount || 0 }} 个
            </el-descriptions-item>
          </el-descriptions>

          <!-- 素材列表 -->
          <div v-if="instanceDetail.assetList && instanceDetail.assetList.length > 0" style="margin-top: 15px">
            <div class="asset-list-header">素材清单</div>
            <el-table :data="instanceDetail.assetList" size="small" border>
              <el-table-column prop="id" label="素材ID" width="80" />
              <el-table-column prop="name" label="素材名称" min-width="150" />
              <el-table-column prop="type" label="类型" width="80" />
              <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
              <el-table-column prop="publishChannel" label="发布渠道" width="120" show-overflow-tooltip />
              <el-table-column label="预览" width="100">
                <template #default="{ row }">
                  <el-button
                    v-if="row.type === 'IMAGE' && (row.thumbnailPath || row.filePath)"
                    type="primary"
                    size="small"
                    link
                    @click="previewImage(row)"
                  >
                    预览图片
                  </el-button>
                  <el-button
                    v-else-if="row.type === 'VIDEO' && row.filePath"
                    type="primary"
                    size="small"
                    link
                    @click="previewVideo(row)"
                  >
                    预览视频
                  </el-button>
                  <span v-else style="color: #909399; font-size: 12px;">不支持预览</span>
                </template>
              </el-table-column>
              <el-table-column label="附件" width="100">
                <template #default="{ row }">
                  <el-button
                    v-if="row.copyrightFilePath"
                    type="success"
                    size="small"
                    link
                    @click="downloadAttachment(row.copyrightFilePath, row.name)"
                  >
                    下载附件
                  </el-button>
                  <span v-else style="color: #909399; font-size: 12px;">无附件</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="getAssetStatusType(row.status)" size="small">
                    {{ getAssetStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 审批进度 -->
        <div v-if="instanceDetail.approvalProgress && instanceDetail.approvalProgress.length > 0" style="margin-top: 20px">

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
        <div v-if="instanceDetail.pendingApprovers && instanceDetail.pendingApprovers.length > 0" style="margin-top: 20px">
          <h4>当前待审批人</h4>
          <div>
            <el-tag
              v-for="approver in instanceDetail.pendingApprovers"
              :key="approver.id"
              type="warning"
              style="margin-right: 10px"
            >
              {{ approver.name }}
            </el-tag>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showInstanceDetailDialog = false">关闭</el-button>
        <el-button
          v-if="canWithdraw"
          type="danger"
          @click="handleWithdraw"
          :loading="withdrawing"
        >
          追回工单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, SuccessFilled, CircleCloseFilled, WarningFilled, Document, Folder, MoreFilled } from '@element-plus/icons-vue'
import { getMyInitiated, withdrawInstance } from '@/api/task'
import { copyApplication } from '@/api/materialApplication'

const router = useRouter()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const statusFilter = ref<string>('')
const query = reactive({ pageNum: 1, pageSize: 10 })
const showInstanceDetailDialog = ref(false)
const withdrawing = ref(false)
const copying = ref(false)

// 实例详情（我发起的）
const instanceDetail = ref<any>({
  id: null,
  applicationId: null,
  businessName: '',
  workflowName: '',
  currentStageName: '',
  approveType: '',
  status: '',
  assetType: '',
  assetStatus: '',
  assetCount: null,
  createTime: '',
  applicantName: '',
  pendingApprovers: [],
  approvalProgress: []
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

// 是否可以追回工单：只有审批中(PENDING)的工单才能追回
const canWithdraw = computed(() => {
  return instanceDetail.value && instanceDetail.value.status === 'PENDING'
})

async function loadData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    const res = await getMyInitiated(params)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleStatusFilterChange() {
  loadData()
}

// 打开"我发起的"详情
function handleOpenInstanceDetail(row: any) {
  instanceDetail.value = row
  showInstanceDetailDialog.value = true
}

// 追回工单
async function handleWithdraw() {
  await ElMessageBox.confirm(
    '确定要追回此工单吗？追回后工单将被驳回，您可以重新编辑并提交申请。',
    '确认追回',
    {
      confirmButtonText: '确定追回',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )

  withdrawing.value = true
  try {
    await withdrawInstance(instanceDetail.value.id, '发起人追回工单')
    ElMessage.success('工单已追回')
    showInstanceDetailDialog.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error('追回失败: ' + (e.message || '未知错误'))
  } finally {
    withdrawing.value = false
  }
}

async function handleCopyApplication(row: any) {
  await ElMessageBox.confirm(
    `确定要复制申请单"${row.businessName}"吗？复制后将创建一个新的草稿，您可以在草稿箱中继续编辑。`,
    '确认复制',
    {
      confirmButtonText: '确定复制',
      cancelButtonText: '取消',
      type: 'info'
    }
  )

  copying.value = true
  try {
    const newApplicationId = await copyApplication(row.applicationId || row.id)
    ElMessage.success('复制成功，正在跳转到素材录入页面...')
    setTimeout(() => {
      router.push(`/asset/material-entry?id=${newApplicationId.data}`)
    }, 500)
  } catch (e: any) {
    ElMessage.error('复制失败: ' + (e.message || '未知错误'))
  } finally {
    copying.value = false
  }
}

function getStatusType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
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
    RETURNED: '已退回',
    NOT_STARTED: '未开始'
  }
  return map[status] || status
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

// 预览图片 - 使用后端API
function previewImage(asset: any) {
  if (asset.id) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
    const fullUrl = `${baseUrl}/asset/preview/${asset.id}`
    window.open(fullUrl, '_blank')
  } else {
    ElMessage.warning('素材ID为空，无法预览')
  }
}

// 预览视频 - 使用后端API
function previewVideo(asset: any) {
  if (asset.id) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
    const fullUrl = `${baseUrl}/asset/preview/${asset.id}`
    window.open(fullUrl, '_blank')
  } else {
    ElMessage.warning('素材ID为空，无法预览')
  }
}

// 下载附件 - 使用后端API
async function downloadAttachment(filePath: string, assetName: string) {
  try {
    // 注意：filePath 是版权附件路径，不是素材路径，需要直接访问
    // 检查是否是绝对路径（Windows 盘符开头）
    const isAbsolutePath = /^[A-Za-z]:/.test(filePath)
    const isHttpUrl = filePath.startsWith('http://') || filePath.startsWith('https://')

    let downloadUrl: string
    if (isHttpUrl) {
      downloadUrl = filePath
    } else if (isAbsolutePath) {
      // 绝对路径无法直接访问，提示用户
      ElMessage.warning('版权附件仅支持在线查看，暂不支持下载')
      return
    } else {
      // 相对路径，拼接baseUrl
      const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
      downloadUrl = `${baseUrl}${filePath}`
    }

    // 创建隐藏的a标签触发下载
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `${assetName}-附件.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (e: any) {
    ElMessage.error('下载失败: ' + (e.message || '未知错误'))
  }
}

onMounted(loadData)
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
  margin-right: 15px;
}

.progress-status {
  display: flex;
  align-items: center;
}

/* 素材列表样式 */
.asset-list-header {
  font-weight: bold;
  color: #606266;
  margin-bottom: 10px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
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

/* 审批人意见样式 */
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
</style>
