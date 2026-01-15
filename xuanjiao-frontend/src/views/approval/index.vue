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
            <el-button link type="primary" @click="handleOpenDetail(row)">审批</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审批详情对话框 -->
    <el-dialog v-model="showApproveDialog" title="审批详情" width="800px" @closed="resetForm">
      <div v-loading="loadingDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="素材名称">{{ taskDetail.businessName }}</el-descriptions-item>
          <el-descriptions-item label="审批流程">{{ taskDetail.workflowName }}</el-descriptions-item>
          <el-descriptions-item label="当前阶段">{{ taskDetail.stageName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ taskDetail.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="审批类型">
            <el-tag v-if="taskDetail.approveType === 'OR'" type="warning">或签（任一通过即可）</el-tag>
            <el-tag v-else type="primary">会签（全部通过）</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 同阶段其他审批人 -->
        <div v-if="taskDetail.otherApprovers && taskDetail.otherApprovers.length > 0" style="margin-top: 15px;">
          <div style="font-weight: bold; margin-bottom: 10px;">同阶段其他审批人：</div>
          <el-tag v-for="approver in taskDetail.otherApprovers" :key="approver.id" style="margin-right: 5px;">
            {{ approver.name }}
            <el-tag v-if="approver.status === 'APPROVED'" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="approver.status === 'REJECTED'" type="danger" size="small">已驳回</el-tag>
            <el-tag v-else type="info" size="small">待审批</el-tag>
          </el-tag>
        </div>

        <!-- 审批进度 -->
        <div style="margin-top: 20px;">
          <div style="font-weight: bold; margin-bottom: 10px;">审批进度：</div>
          <div class="progress-list">
            <div
              v-for="progress in taskDetail.approvalProgress"
              :key="progress.id"
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
                  <el-tag v-if="progress.isSubWorkflow === 1" type="info" size="small">子流程</el-tag>
                </div>
                <div v-if="progress.approvers && progress.approvers.length > 0" class="progress-approvers">
                  <span v-for="approver in progress.approvers" :key="approver.id" class="approver-item">
                    {{ approver.name }}
                    <span v-if="approver.status === 'APPROVED'" style="color: #67C23A;">✓</span>
                    <span v-else-if="approver.status === 'REJECTED'" style="color: #F56C6C;">✗</span>
                    <span v-else style="color: #909399;">待审批</span>
                  </span>
                </div>
                <div class="progress-status">
                  <el-tag v-if="progress.status === 'PENDING'" type="warning" size="small">待审批</el-tag>
                  <el-tag v-else-if="progress.status === 'APPROVED'" type="success" size="small">已通过</el-tag>
                  <el-tag v-else type="danger" size="small">已驳回</el-tag>
                  <span v-if="progress.approveTime" style="margin-left: 10px; color: #909399; font-size: 12px;">
                    {{ progress.approveTime }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 选择下一层审批人 -->
        <div v-if="taskDetail.canSelectNextApprovers && taskDetail.nextStageId" style="margin-top: 20px;">
          <div style="border: 1px solid #409EFF; border-radius: 6px; padding: 16px; background-color: #FAFAFA">
            <div style="display: flex; align-items: center; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #DCDFE6">
              <el-icon style="color: #409EFF; margin-right: 8px;"><Document /></el-icon>
              <span style="font-weight: bold; color: #409EFF; font-size: 14px">主流程下一层审批人（{{ taskDetail.nextStageName }}）</span>
              <el-tag v-if="taskDetail.nextStageApproveType === 'OR'" type="warning" size="small" style="margin-left: auto">或签</el-tag>
              <el-tag v-else type="success" size="small" style="margin-left: auto">会签</el-tag>
            </div>

            <el-alert
              title="提示"
              type="info"
              :closable="false"
              style="margin-bottom: 15px"
            >
              <template v-if="taskDetail.nextStageApproveType === 'OR'">
                或签：请为每个子流程选择审批人，并从其他配置中选择 1 个审批人，共需要选择
                {{ (taskDetail.subWorkflows?.length || 0) + 1 }} 个审批人。
              </template>
              <template v-else>
                会签：请按照配置顺序为每个配置项选择一个审批人，共需要选择
                {{ (taskDetail.nextStageApproverConfigs?.length || 0) + (taskDetail.subWorkflows?.length || 0) }} 个审批人。
              </template>
            </el-alert>

            <div v-if="taskDetail.nextStageApproverConfigs && taskDetail.nextStageApproverConfigs.length > 0">
              <div v-for="(config, index) in taskDetail.nextStageApproverConfigs" :key="config.configId" style="margin-bottom: 15px;">
                <div style="display: flex; align-items: center; margin-bottom: 5px;">
                  <span style="display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; background-color: #409EFF; color: white; font-size: 12px; margin-right: 8px;">{{ index + 1 }}</span>
                  <span style="font-weight: bold; color: #606266;">{{ config.approverTypeName }}：{{ config.approverName }}</span>
                </div>
                <el-select
                  v-model="selectedNextApprovers[config.configId]"
                  filterable
                  placeholder="请选择审批人"
                  style="width: 100%;"
                  :clearable="true"
                  @change="handleNormalApproverChange"
                >
                  <el-option
                    v-for="user in config.availableUsers"
                    :key="user.id"
                    :label="user.realName || user.username"
                    :value="user.id"
                  >
                    <span>{{ user.realName || user.username }}</span>
                    <span style="color: #909399; font-size: 12px; margin-left: 10px;">
                      {{ user.deptName }} / {{ user.roleName }}
                    </span>
                  </el-option>
                </el-select>
              </div>
              <!-- 已选择提示 -->
              <div style="margin-top: 8px; color: #67C23A; font-size: 12px">
                已选择 {{ Object.values(selectedNextApprovers).filter(v => v !== null && v !== undefined).length }} / {{ taskDetail.nextStageApproveType === 'OR' ? 1 : (taskDetail.nextStageApproverConfigs?.length || 0) }} 位审批人
              </div>
            </div>
            <div v-else style="color: #F56C6C; font-size: 12px;">
              下一层没有配置审批人
            </div>
          </div>
        </div>

        <!-- 子流程审批人选择 -->
        <div v-if="taskDetail.hasSubWorkflows && taskDetail.subWorkflows && taskDetail.subWorkflows.length > 0 && taskDetail.canSelectNextApprovers" style="margin-top: 20px;">
          <el-alert
            title="提示"
            type="warning"
            :closable="false"
            style="margin-bottom: 15px"
          >
            当前阶段包含子流程，您需要为每个子流程选择第一层审批人。子流程将独立运行，不影响主流程。
          </el-alert>
          <div v-for="subWorkflow in taskDetail.subWorkflows" :key="subWorkflow.id" style="margin-bottom: 20px; padding: 15px; border: 1px solid #E6A23C; border-radius: 6px; background-color: #FFFBF0">
            <div style="display: flex; align-items: center; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #DCDFE6">
              <el-icon style="color: #E6A23C; margin-right: 8px;"><Folder /></el-icon>
              <span style="font-weight: bold; color: #E6A23C; font-size: 14px">子流程：{{ subWorkflow.name || '未命名' }} (ID: {{ subWorkflow.id }})</span>
              <span v-if="subWorkflow.workflowType" style="color: #909399; font-size: 12px; margin-left: 10px;">
                ({{ subWorkflow.workflowType === 'ASSET_UPLOAD' ? '素材录入' : '素材使用' }})
              </span>
              <el-tag v-if="subWorkflow.approveType === 'OR'" type="warning" size="small" style="margin-left: auto">或签</el-tag>
              <el-tag v-else type="success" size="small" style="margin-left: auto">会签</el-tag>
            </div>
            <!-- 子流程未配置阶段或审批人 -->
            <div v-if="!subWorkflow.approverConfigs || subWorkflow.approverConfigs.length === 0" style="color: #F56C6C; font-size: 13px;">
              <el-icon style="vertical-align: middle;"><WarningFilled /></el-icon>
              该子流程未配置阶段或审批人，请在流程设计器中配置。
            </div>
            <!-- 子流程有配置 -->
            <template v-else>
              <div style="margin-bottom: 10px; color: #606266; font-size: 13px">
                <template v-if="subWorkflow.approveType === 'OR'">
                  或签：请从以下配置中选择 1 个审批人
                </template>
                <template v-else>
                  会签：请按照配置顺序为每个配置项选择一个审批人，共需要选择 {{ subWorkflow.approverCount }} 个审批人。
                </template>
              </div>
              <div v-for="(config, index) in subWorkflow.approverConfigs" :key="config.configId" style="margin-bottom: 10px;">
                <div style="display: flex; align-items: center; margin-bottom: 5px;">
                  <span style="display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; background-color: #E6A23C; color: white; font-size: 12px; margin-right: 8px;">{{ index + 1 }}</span>
                  <span style="font-weight: bold; color: #606266;">{{ config.approverTypeName || '未知类型' }}：{{ config.approverName || '未命名' }}</span>
                  <span v-if="!config.availableUsers || config.availableUsers.length === 0" style="color: #F56C6C; font-size: 12px; margin-left: 10px;">
                    （无可选用户，请检查用户状态）
                  </span>
                </div>
                <el-select
                  v-model="selectedSubWorkflowApprovers[subWorkflow.id][config.configId]"
                  filterable
                  placeholder="请选择审批人"
                  style="width: 100%;"
                  clearable
                  @change="handleSubWorkflowApproverChange(subWorkflow.id)"
                  :disabled="!config.availableUsers || config.availableUsers.length === 0"
                >
                  <el-option
                    v-for="user in (config.availableUsers || [])"
                    :key="user.id"
                    :label="user.realName || user.username"
                    :value="user.id"
                  >
                    <span>{{ user.realName || user.username }}</span>
                    <span style="color: #909399; font-size: 12px; margin-left: 10px;">
                      {{ user.deptName }} / {{ user.roleName }}
                    </span>
                  </el-option>
                </el-select>
              </div>
              <!-- 已选择提示 -->
              <div style="margin-top: 8px; color: #67C23A; font-size: 12px">
                已选择 {{ Object.values(selectedSubWorkflowApprovers[subWorkflow.id] || {}).filter(v => v !== null && v !== undefined).length }} / {{ subWorkflow.approveType === 'OR' ? 1 : subWorkflow.approverCount }} 位审批人
              </div>
            </template>
          </div>
        </div>

        <!-- 已选择的下一层审批人（只读） -->
        <div v-else-if="taskDetail.selectedNextApprovers && taskDetail.selectedNextApprovers.length > 0" style="margin-top: 20px;">
          <div style="font-weight: bold; margin-bottom: 10px;">已选择的下一层审批人：</div>
          <el-tag v-for="approver in taskDetail.selectedNextApprovers" :key="approver.id" type="info">
            {{ approver.name }}
          </el-tag>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            由 {{ taskDetail.selectedByUserId === currentUserId ? '您' : '其他审批人' }} 选择
          </div>
        </div>

        <!-- 已选择的子流程审批人（只读） -->
        <div v-if="taskDetail.selectedSubWorkflowApprovers && Object.keys(taskDetail.selectedSubWorkflowApprovers).length > 0" style="margin-top: 20px;">
          <div style="font-weight: bold; margin-bottom: 10px;">已选择的子流程审批人：</div>
          <div v-for="(approvers, subWorkflowId) in taskDetail.selectedSubWorkflowApprovers" :key="subWorkflowId" style="margin-bottom: 10px;">
            <div style="color: #606266; font-size: 13px; margin-bottom: 5px;">
              <span v-if="taskDetail.subWorkflows && taskDetail.subWorkflows.find((sw: any) => sw.id === subWorkflowId)">
                子流程：{{ taskDetail.subWorkflows.find((sw: any) => sw.id === subWorkflowId).name }}
              </span>
              <span v-else>子流程 #{{ subWorkflowId }}</span>
            </div>
            <el-tag v-for="approver in approvers" :key="approver.id" type="info" size="small">
              {{ approver.name }}
            </el-tag>
          </div>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            由 {{ taskDetail.selectedByUserId === currentUserId ? '您' : '其他审批人' }} 选择
          </div>
        </div>

        <!-- 审批意见 -->
        <div style="margin-top: 20px;">
          <div style="font-weight: bold; margin-bottom: 10px;">审批意见：</div>
          <el-input
            v-model="approveForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="showApproveDialog = false" :disabled="submitting">取消</el-button>
        <el-button type="danger" @click="submitApprove(false)" :loading="submitting">驳回</el-button>
        <el-button type="success" @click="submitApprove(true)" :loading="submitting">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, SuccessFilled, CircleCloseFilled, WarningFilled, Document, Folder } from '@element-plus/icons-vue'
import { getMyTasks, getMyApplied, getTaskDetail, approve } from '@/api/approval'
import { selectNextStageApprovers, selectNextStageApproversWithSubWorkflows } from '@/api/workflow'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const currentUserId = ref(userStore.userInfo?.id)

const loading = ref(false)
const activeTab = ref('pending')
const list = ref([])
const showApproveDialog = ref(false)
const loadingDetail = ref(false)
const submitting = ref(false)
const currentTask = ref<any>(null)
const approveForm = reactive({ comment: '' })

// 任务详情
const taskDetail = ref<any>({
  businessName: '',
  workflowName: '',
  stageName: '',
  applicantName: '',
  approveType: '',
  canSelectNextApprovers: false,
  nextStageId: null,
  nextStageName: '',
  nextStageApproveType: '', // 下一阶段的审批类型（OR=或签，其他=会签）
  otherApprovers: [],
  selectedNextApprovers: [],
  approvalProgress: [],
  hasSubWorkflows: false,
  subWorkflows: []
})

// 下一层审批人选择
const selectedNextApprovers = ref<Record<number, number>>({})  // 配置ID -> 选中的用户ID
const loadingApprovers = ref(false)

// 子流程审批人选择
const selectedSubWorkflowApprovers = ref<Record<number, Record<number, number>>>({})  // 子流程ID -> 配置ID -> 选中的用户ID
const loadingSubWorkflowApprovers = ref<Record<number, boolean>>({})

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

async function handleOpenDetail(row: any) {
  console.log('handleOpenDetail start, row:', row)
  currentTask.value = row
  showApproveDialog.value = true
  loadingDetail.value = true
  console.log('loadingDetail set to true')

  // 重置表单状态（但不重置taskDetail，避免在渲染时出错）
  approveForm.comment = ''
  selectedNextApprovers.value = {}
  selectedSubWorkflowApprovers.value = {}

  try {
    const res = await getTaskDetail(row.id)
    console.log('getTaskDetail response:', res)
    taskDetail.value = res.data
    console.log('taskDetail.value set:', taskDetail.value)

    // 如果可以选择下一层审批人，初始化选择状态
    if (res.data.canSelectNextApprovers && res.data.nextStageId && res.data.nextStageApproverConfigs) {
      // 清空之前的选择
      selectedNextApprovers.value = {}
      console.log('cleared selectedNextApprovers')
    }

    // 如果有子流程且可以选择审批人，初始化子流程选择状态
    if (res.data.hasSubWorkflows && res.data.subWorkflows && res.data.subWorkflows.length > 0 && res.data.canSelectNextApprovers) {
      // 为每个子流程初始化选择对象
      for (const subWorkflow of res.data.subWorkflows) {
        selectedSubWorkflowApprovers.value[subWorkflow.id] = {}
        console.log('initialized selectedSubWorkflowApprovers for subWorkflow:', subWorkflow.id)
      }
    }
  } catch (e: any) {
    console.error('handleOpenDetail error:', e)
    ElMessage.error(e.message || '加载详情失败')
  } finally {
    loadingDetail.value = false
    console.log('loadingDetail set to false')
  }
  console.log('handleOpenDetail end')
}

// 处理普通审批人选择变化（或签时只允许选1个）
function handleNormalApproverChange() {
  if (taskDetail.value.nextStageApproveType === 'OR') {
    const selectedKeys = Object.keys(selectedNextApprovers.value).filter(
      key => selectedNextApprovers.value[key] !== null && selectedNextApprovers.value[key] !== undefined
    )
    // 如果选择了多个，只保留最后一个
    if (selectedKeys.length > 1) {
      const lastKey = selectedKeys[selectedKeys.length - 1]
      const lastValue = selectedNextApprovers.value[lastKey]
      selectedNextApprovers.value = {}
      selectedNextApprovers.value[lastKey] = lastValue
    }
  }
}

// 处理子流程审批人选择变化（或签时只允许选1个）
function handleSubWorkflowApproverChange(subWorkflowId: number) {
  const subWorkflow = taskDetail.value.subWorkflows?.find((sw: any) => sw.id === subWorkflowId)
  if (subWorkflow && subWorkflow.approveType === 'OR') {
    const subSelected = selectedSubWorkflowApprovers.value[subWorkflowId]
    if (subSelected) {
      const selectedKeys = Object.keys(subSelected).filter(
        key => subSelected[key] !== null && subSelected[key] !== undefined
      )
      // 如果选择了多个，只保留最后一个
      if (selectedKeys.length > 1) {
        const lastKey = selectedKeys[selectedKeys.length - 1]
        const lastValue = subSelected[lastKey]
        selectedSubWorkflowApprovers.value[subWorkflowId] = {}
        selectedSubWorkflowApprovers.value[subWorkflowId][lastKey] = lastValue
      }
    }
  }
}

async function submitApprove(passed: boolean) {
  // 如果可以选择下一层审批人且通过，检查是否已选择
  if (passed && taskDetail.value.canSelectNextApprovers) {
    const configs = taskDetail.value.nextStageApproverConfigs || []
    const isOrSign = taskDetail.value.nextStageApproveType === 'OR'

    if (isOrSign) {
      // 或签：至少选1个普通审批人 + 所有子流程
      const selectedCount = Object.values(selectedNextApprovers.value).filter(v => v !== null && v !== undefined).length

      if (selectedCount === 0) {
        ElMessage.warning('请从普通审批人配置中选择至少 1 个审批人')
        return
      }

      // 检查所有子流程是否都选择了（根据子流程自己的或签/会签类型）
      if (taskDetail.value.hasSubWorkflows && taskDetail.value.subWorkflows) {
        for (const subWorkflow of taskDetail.value.subWorkflows) {
          const subSelected = selectedSubWorkflowApprovers.value[subWorkflow.id]
          const subConfigs = subWorkflow.approverConfigs || []
          const subSelectedCount = subSelected ? Object.values(subSelected).filter(v => v !== null && v !== undefined).length : 0
          const subRequiredCount = subConfigs.length

          if (subWorkflow.approveType === 'OR') {
            // 子流程或签：至少选1个
            if (subSelectedCount === 0) {
              ElMessage.warning(`请为子流程"${subWorkflow.name}"选择至少 1 位审批人（或签）`)
              return
            }
          } else {
            // 子流程会签：所有配置都要选
            if (subSelectedCount < subRequiredCount) {
              ElMessage.warning(`请为子流程"${subWorkflow.name}"选择所有配置项的审批人（已选择 ${subSelectedCount}/${subRequiredCount}）`)
              return
            }
          }
        }
      }
    } else {
      // 会签：所有配置都要选（普通审批人 + 子流程）
      const selectedCount = Object.keys(selectedNextApprovers.value).length
      const requiredCount = configs.length

      if (selectedCount === 0) {
        ElMessage.warning('请选择下一层审批人')
        return
      }

      if (selectedCount < requiredCount) {
        ElMessage.warning(`请为所有配置项选择审批人（已选择 ${selectedCount}/${requiredCount}）`)
        return
      }

      // 检查是否所有子流程都选择了审批人
      if (taskDetail.value.hasSubWorkflows && taskDetail.value.subWorkflows) {
        for (const subWorkflow of taskDetail.value.subWorkflows) {
          const subSelected = selectedSubWorkflowApprovers.value[subWorkflow.id]
          const subConfigs = subWorkflow.approverConfigs || []
          const subSelectedCount = subSelected ? Object.keys(subSelected).length : 0
          const subRequiredCount = subConfigs.length

          if (subRequiredCount > 0 && subSelectedCount < subRequiredCount) {
            ElMessage.warning(`请为子流程"${subWorkflow.name}"选择所有配置项的审批人（已选择 ${subSelectedCount}/${subRequiredCount}）`)
            return
          }
        }
      }
    }
  }

  submitting.value = true
  try {
    // 如果通过且可以选择下一层审批人，先选择下一层审批人
    if (passed && taskDetail.value.canSelectNextApprovers) {
      // 将选择的用户按照配置顺序组成数组
      const configs = taskDetail.value.nextStageApproverConfigs || []
      const approverIds: number[] = []
      for (const config of configs) {
        const selectedUserId = selectedNextApprovers.value[config.configId]
        if (selectedUserId) {
          approverIds.push(selectedUserId)
        }
      }

      // 构建子流程审批人选择（按配置顺序）
      const subWorkflowApproverIds: Record<number, number[]> = {}
      if (taskDetail.value.hasSubWorkflows && taskDetail.value.subWorkflows) {
        for (const subWorkflow of taskDetail.value.subWorkflows) {
          const subConfigs = subWorkflow.approverConfigs || []
          const subApproverIds: number[] = []
          for (const subConfig of subConfigs) {
            const subSelected = selectedSubWorkflowApprovers.value[subWorkflow.id]
            if (subSelected && subSelected[subConfig.configId]) {
              subApproverIds.push(subSelected[subConfig.configId])
            }
          }
          if (subApproverIds.length > 0) {
            subWorkflowApproverIds[subWorkflow.id] = subApproverIds
          }
        }
      }

      if (approverIds.length > 0 || Object.keys(subWorkflowApproverIds).length > 0) {
        // 使用包含子流程的新API
        await selectNextStageApproversWithSubWorkflows({
          taskId: currentTask.value.id,
          approverIds: approverIds,
          subWorkflowApproverIds: subWorkflowApproverIds
        })
      }
    }

    // 然后审批
    await approve(currentTask.value.id, approveForm.comment, passed)

    ElMessage.success(passed ? '审批通过' : '已驳回')
    showApproveDialog.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  approveForm.comment = ''
  selectedNextApprovers.value = {}
  selectedSubWorkflowApprovers.value = {}
  loadingSubWorkflowApprovers.value = {}
  taskDetail.value = {
    businessName: '',
    workflowName: '',
    stageName: '',
    applicantName: '',
    approveType: '',
    canSelectNextApprovers: false,
    nextStageId: null,
    nextStageName: '',
    otherApprovers: [],
    selectedNextApprovers: [],
    approvalProgress: [],
    hasSubWorkflows: false,
    subWorkflows: []
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

<style scoped>
.progress-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.progress-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #dcdfe6;
  transition: all 0.3s;
}

.progress-item.active {
  border-left-color: #e6a23c;
  background: #fdf6ec;
}

.progress-item.approved {
  border-left-color: #67c23a;
  background: #f0f9ff;
}

.progress-item.rejected {
  border-left-color: #f56c6c;
  background: #fef0f0;
}

.progress-icon {
  margin-right: 12px;
  font-size: 20px;
}

.progress-item.active .progress-icon {
  color: #e6a23c;
}

.progress-item.approved .progress-icon {
  color: #67c23a;
}

.progress-item.rejected .progress-icon {
  color: #f56c6c;
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
</style>
