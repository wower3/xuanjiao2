<template>
  <div class="material-approval-page">
    <el-card>
      <template #header>
        <div class="header-content">
          <span>审批工单</span>
          <div class="filter-section">
            <el-select
              v-model="filter.applicantId"
              placeholder="筛选发起人"
              clearable
              filterable
              style="width: 200px; margin-right: 10px"
              @change="handleFilterChange"
            >
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="user.realName || user.username"
                :value="user.id"
              />
            </el-select>

            <el-select
              v-model="filter.deptId"
              placeholder="筛选发起部门"
              clearable
              filterable
              style="width: 200px; margin-right: 10px"
              @change="handleFilterChange"
            >
              <el-option
                v-for="dept in deptList"
                :key="dept.id"
                :label="dept.name"
                :value="dept.id"
              />
            </el-select>

            <el-select
              v-model="filter.roleType"
              placeholder="筛选发起人角色类型"
              clearable
              style="width: 220px; margin-right: 10px"
              @change="handleFilterChange"
            >
              <el-option
                v-for="roleType in roleTypeOptions"
                :key="roleType.value"
                :label="roleType.label"
                :value="roleType.value"
              />
            </el-select>

            <el-select
              v-model="filter.status"
              placeholder="筛选审批状态"
              clearable
              style="width: 150px"
              @change="handleFilterChange"
            >
              <el-option
                v-for="status in statusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
              />
            </el-select>

            <el-button
              v-if="hasFilter"
              type="primary"
              link
              @click="clearFilter"
              style="margin-left: 10px"
            >
              清除筛选
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="approvalList" v-loading="loading" stripe>
        <el-table-column label="申请单ID" width="120">
          <template #default="{ row }">
            <span style="color: #409EFF; font-weight: 500;">AP-{{ row.applicationId || row.id }}</span>
          </template>
        </el-table-column>

        <el-table-column label="申请标题" min-width="200" prop="businessName" />

        <el-table-column prop="applicantName" label="发起人" width="120" />

        <el-table-column label="素材信息" width="150">
          <template #default="{ row }">
            <div v-if="row.assetType || row.assetCount">
              <div>类型: {{ row.assetType || '-' }}</div>
              <div>数量: {{ row.assetCount || 0 }} 个</div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="workflowName" label="审批流程" width="150" />

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="当前阶段" width="150">
          <template #default="{ row }">
            {{ row.currentStageName || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="待审批人" width="180">
          <template #default="{ row }">
            <div v-if="row.pendingApprovers && row.pendingApprovers.length > 0">
              <el-tag
                v-for="approver in row.pendingApprovers"
                :key="approver.id"
                size="small"
                type="info"
                style="margin-right: 5px; margin-bottom: 5px"
              >
                {{ approver.name }}
              </el-tag>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="180" />

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @change="loadApprovals"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetail" title="审批工单详情" width="1000px">
      <div v-if="currentDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请单ID" :span="2">
            AP-{{ currentDetail.applicationId || currentDetail.id }}
          </el-descriptions-item>
          <el-descriptions-item label="申请标题" :span="2">
            {{ currentDetail.applicationTitle || currentDetail.businessName }}
          </el-descriptions-item>
          <el-descriptions-item label="发起人">
            {{ currentDetail.applicantName }}
          </el-descriptions-item>
          <el-descriptions-item label="审批流程">
            {{ currentDetail.workflowName }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(currentDetail.status)" size="small">
              {{ getStatusText(currentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前阶段">
            {{ currentDetail.currentStageName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ currentDetail.createTime }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 素材信息 -->
        <div v-if="currentDetail.assetType || currentDetail.assetCount" style="margin-top: 20px">
          <h4>素材信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="素材类型">
              {{ currentDetail.assetType || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="素材数量">
              {{ currentDetail.assetCount || 0 }} 个
            </el-descriptions-item>
          </el-descriptions>

          <!-- 素材列表 -->
          <div v-if="currentDetail.assetList && currentDetail.assetList.length > 0" style="margin-top: 15px">
            <div class="asset-list-header">素材清单</div>
            <el-table :data="currentDetail.assetList" size="small" border>
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
        <div v-if="currentDetail.approvalProgress && currentDetail.approvalProgress.length > 0" style="margin-top: 20px">

          <!-- 主流程进度 -->
          <div v-if="mainWorkflowProgress.length > 0" style="margin-bottom: 25px">
            <div class="workflow-section-header">
              <el-icon style="color: #409EFF; margin-right: 8px;"><Document /></el-icon>
              <span class="workflow-section-title">主流程审批进度</span>
            </div>
            <div class="progress-list">
              <div
                v-for="(progress, index) in mainWorkflowProgress"
                :key="'main-' + index"
                class="progress-item"
                :class="{
                  'active': progress.status === 'PENDING',
                  'approved': progress.status === 'APPROVED',
                  'rejected': progress.status === 'REJECTED'
                }"
              >
                <div class="progress-icon">
                  <el-icon v-if="progress.status === 'PENDING'"><Clock /></el-icon>
                  <el-icon v-else-if="progress.status === 'APPROVED'"><SuccessFilled /></el-icon>
                  <el-icon v-else><CircleCloseFilled /></el-icon>
                </div>
                <div class="progress-content">
                  <div class="progress-stage">
                    {{ progress.stageName }}
                    <el-tag
                      v-if="progress.status"
                      :type="getProgressType(progress.status)"
                      size="small"
                      style="margin-left: 8px"
                    >
                      {{ getProgressStatusText(progress.status) }}
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
                :key="'sub-' + index"
                class="progress-item sub-workflow-item"
                :class="{
                  'active': progress.status === 'PENDING',
                  'approved': progress.status === 'APPROVED',
                  'rejected': progress.status === 'REJECTED'
                }"
              >
                <div class="progress-icon">
                  <el-icon v-if="progress.status === 'PENDING'"><Clock /></el-icon>
                  <el-icon v-else-if="progress.status === 'APPROVED'"><SuccessFilled /></el-icon>
                  <el-icon v-else><CircleCloseFilled /></el-icon>
                </div>
                <div class="progress-content">
                  <div class="progress-stage">
                    {{ progress.stageName }}
                    <el-tag
                      v-if="progress.status"
                      :type="getProgressType(progress.status)"
                      size="small"
                      style="margin-left: 8px"
                    >
                      {{ getProgressStatusText(progress.status) }}
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
        <div v-if="currentDetail.pendingApprovers && currentDetail.pendingApprovers.length > 0" style="margin-top: 20px">
          <h4>当前待审批人</h4>
          <div>
            <el-tag
              v-for="approver in currentDetail.pendingApprovers"
              :key="approver.id"
              type="warning"
              style="margin-right: 10px"
            >
              {{ approver.name }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getMyApplied } from '@/api/approval'
import { getUserList } from '@/api/user'
import { getDeptList } from '@/api/dept'
import { ElMessage } from 'element-plus'
import { Clock, SuccessFilled, CircleCloseFilled, Document, Folder } from '@element-plus/icons-vue'

const loading = ref(false)
const approvalList = ref<any[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const filter = reactive({
  applicantId: null as number | null,
  deptId: null as number | null,
  roleType: null as string | null,
  status: null as string | null
})

const userList = ref<any[]>([])
const deptList = ref<any[]>([])

const showDetail = ref(false)
const currentDetail = ref<any>(null)

const roleTypeOptions = [
  { label: '系统管理员', value: 'SYSTEM_ADMIN' },
  { label: '总部管理', value: 'GENERAL_MGMT' },
  { label: '分支机构管理', value: 'BRANCH_MGMT' },
  { label: '总部普通用户', value: 'GENERAL_USER' },
  { label: '分支机构普通用户', value: 'BRANCH_USER' },
  { label: '自定义角色', value: 'CUSTOM' }
]

const statusOptions = [
  { label: '审批中', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已取消', value: 'CANCELLED' }
]

const hasFilter = computed(() => {
  return filter.applicantId !== null || filter.deptId !== null || filter.roleType !== null || filter.status !== null
})

// 分离主流程和子流程进度
const mainWorkflowProgress = computed(() => {
  if (!currentDetail.value || !currentDetail.value.approvalProgress) return []
  return currentDetail.value.approvalProgress.filter((p: any) => p.isSubWorkflow !== 1)
})

const subWorkflowProgress = computed(() => {
  if (!currentDetail.value || !currentDetail.value.approvalProgress) return []
  return currentDetail.value.approvalProgress.filter((p: any) => p.isSubWorkflow === 1)
})

async function loadApprovals() {
  loading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      businessType: 'MATERIAL_ENTRY',  // 只查询素材录入类型
      forAllUsers: true,                // 查询所有用户的工单
      applicantId: filter.applicantId,
      deptId: filter.deptId,
      roleType: filter.roleType,
      status: filter.status
    }
    const res = await getMyApplied(params)
    approvalList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  query.pageNum = 1
  loadApprovals()
}

function clearFilter() {
  filter.applicantId = null
  filter.deptId = null
  filter.roleType = null
  filter.status = null
  handleFilterChange()
}

async function viewDetail(row: any) {
  currentDetail.value = row
  showDetail.value = true
}

function getStatusType(status: string) {
  const typeMap: Record<string, string> = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return typeMap[status] || 'info'
}

function getStatusText(status: string) {
  const textMap: Record<string, string> = {
    'PENDING': '审批中',
    'APPROVED': '已通过',
    'REJECTED': '已驳回',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

function getProgressType(status: string) {
  const typeMap: Record<string, string> = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return typeMap[status] || 'info'
}

function getProgressStatusText(status: string) {
  const textMap: Record<string, string> = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已驳回',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
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

async function loadUsers() {
  try {
    const res = await getUserList()
    userList.value = res.data
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

async function loadDepts() {
  try {
    const res = await getDeptList()
    deptList.value = res.data
  } catch (error) {
    console.error('加载部门列表失败:', error)
  }
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

onMounted(() => {
  loadUsers()
  loadDepts()
  loadApprovals()
})
</script>

<style scoped>
.material-approval-page {
  padding: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  display: flex;
  align-items: center;
}

h4 {
  margin: 15px 0 10px;
  color: #303133;
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

/* 审批进度样式 */
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
</style>
