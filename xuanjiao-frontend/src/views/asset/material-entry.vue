<template>
  <div class="material-entry-page">
    <el-card>
      <template #header>
        <span>{{ isEditMode ? '编辑申请单' : '新建申请单' }}</span>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="事项标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入事项标题" />
        </el-form-item>
        <el-form-item label="维护人" prop="maintainerId">
          <el-input :value="currentUser?.realName" disabled />
        </el-form-item>
        <el-form-item label="归属部门">
          <el-input :value="currentUser?.deptName" disabled />
        </el-form-item>
        <el-form-item label="保证声明">
          <el-checkbox v-model="form.guaranteeDeclaration">我保证所上传的素材符合版权要求</el-checkbox>
        </el-form-item>
      </el-form>

      <!-- 素材文件列表 -->
      <div class="file-section">
        <div class="file-header">
          <span>素材文件 ({{ fileList.length }})</span>
          <el-button type="primary" size="small" @click="showAddFile = true">
            添加文件
          </el-button>
        </div>
        <el-table :data="fileList" size="small">
          <el-table-column prop="name" label="文件名称" />
          <el-table-column prop="type" label="类型" width="80" />
          <el-table-column label="标签" width="150">
            <template #default="{ row }">
              <el-tag
                v-for="tag in row.tags"
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
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="danger" @click="removeFile(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="action-buttons">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="success" @click="handleSubmitDialog" :loading="submitting">提交审批</el-button>
      </div>
    </el-card>

    <!-- 添加文件对话框 -->
    <el-dialog v-model="showAddFile" title="添加素材文件" width="600px">
      <el-form :model="fileForm" :rules="fileRules" ref="fileFormRef" label-width="120px">
        <el-form-item label="文件名称" prop="name">
          <el-input v-model="fileForm.name" placeholder="请输入文件名称" />
        </el-form-item>
        <el-form-item label="文件类型" prop="type">
          <el-select v-model="fileForm.type">
            <el-option label="视频" value="VIDEO" />
            <el-option label="图片" value="IMAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件" prop="file">
          <el-upload
            ref="fileUploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="素材标签">
          <el-select v-model="fileForm.tagIds" multiple placeholder="请选择标签">
            <el-option
              v-for="tag in tagList"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
          <el-button link type="primary" @click="showCreateTag = true" style="margin-left: 10px">
            新建标签
          </el-button>
        </el-form-item>
        <el-form-item label="版权声明">
          <el-radio-group v-model="copyrightType">
            <el-radio label="none">无</el-radio>
            <el-radio label="text">文本</el-radio>
            <el-radio label="file">文件</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="copyrightType === 'text'" label="版权文本">
          <el-input v-model="fileForm.copyrightText" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item v-if="copyrightType === 'file'" label="版权文件">
          <el-upload
            ref="copyrightUploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleCopyrightFileChange"
          >
            <el-button type="primary">选择版权文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="申请说明">
          <el-input v-model="fileForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="发布渠道">
          <el-input v-model="fileForm.publishChannel" placeholder="请输入发布渠道" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddFile = false">取消</el-button>
        <el-button type="primary" @click="handleAddFile" :loading="addingFile">添加</el-button>
      </template>
    </el-dialog>

    <!-- 新建标签对话框 -->
    <el-dialog v-model="showCreateTag" title="新建标签" width="400px">
      <el-form :model="tagForm" label-width="80px">
        <el-form-item label="标签名称">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="tagForm.category" placeholder="请选择分类">
            <el-option label="图片" value="IMAGE" />
            <el-option label="视频" value="VIDEO" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTag = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTag">创建</el-button>
      </template>
    </el-dialog>

    <!-- 提交审批对话框 -->
    <el-dialog v-model="showSubmitDialog" title="提交审批" width="600px">
      <el-form label-width="100px">
        <el-form-item label="审批流程">
          <div v-if="boundWorkflow">
            <el-tag type="success">{{ boundWorkflow.name }}</el-tag>
            <div style="color: #909399; font-size: 12px; margin-top: 5px">
              根据您的角色自动匹配的审批流程
            </div>
          </div>
          <div v-else>
            <span style="color: #F56C6C">您的角色未绑定审批流程，无法提交审批</span>
          </div>
        </el-form-item>

        <!-- 第一层审批人选择 -->
        <el-form-item label="第一层审批人" v-if="firstStageApproverConfigs.length > 0 || (hasLoadedInitialApprovers && approverKeyword)">
          <div style="width: 100%">
            <!-- 主流程审批人选择卡片 -->
            <div style="border: 1px solid #409EFF; border-radius: 6px; padding: 16px; background-color: #FAFAFA">
              <!-- 标题栏 -->
              <div style="display: flex; align-items: center; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #DCDFE6">
                <el-icon style="color: #409EFF; margin-right: 8px;"><Document /></el-icon>
                <span style="font-weight: bold; color: #409EFF; font-size: 14px">主流程审批人</span>
                <el-tag v-if="firstStageApproveType === 'OR'" type="warning" size="small" style="margin-left: auto">或签</el-tag>
                <el-tag v-else type="success" size="small" style="margin-left: auto">会签</el-tag>
              </div>

              <!-- 提示信息 -->
              <div style="margin-bottom: 12px; padding: 10px; background-color: #ECF5FF; border-radius: 4px; border-left: 3px solid #409EFF; font-size: 13px; color: #303133">
                <template v-if="firstStageApproveType === 'OR'">
                  <el-icon style="color: #409EFF; margin-right: 5px"><InfoFilled /></el-icon>
                  或签：请从以下配置中选择 1 个审批人{{ subWorkflows.length > 0 ? '，并完成所有子流程的审批人选择' : '' }}
                </template>
                <template v-else>
                  <el-icon style="color: #409EFF; margin-right: 5px"><InfoFilled /></el-icon>
                  会签：请按照配置顺序为每个配置项选择一个审批人{{ subWorkflows.length > 0 ? '，并完成所有子流程的审批人选择' : '' }}
                </template>
              </div>

              <!-- 审批人配置选择 -->
              <template v-if="firstStageApproverConfigs.length > 0">
                <div v-for="(config, index) in firstStageApproverConfigs" :key="config.configId" style="margin-bottom: 16px">
                  <div style="font-weight: 500; margin-bottom: 8px; color: #303133; font-size: 13px; display: flex; align-items: center">
                    <span style="background-color: #409EFF; color: white; width: 20px; height: 20px; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; font-size: 12px; margin-right: 8px">{{ index + 1 }}</span>
                    {{ config.approverTypeName }}：{{ config.approverName }}
                  </div>
                  <el-select
                    v-model="selectedFirstStageApprovers[config.configId]"
                    filterable
                    placeholder="请选择审批人"
                    style="width: 100%;"
                    clearable
                    @change="handleFirstStageApproverChange"
                    :disabled="!config.availableUsers || config.availableUsers.length === 0"
                  >
                    <el-option
                      v-for="user in (config.availableUsers || [])"
                      :key="user.id"
                      :label="`${user.realName || user.username}${user.username && user.realName ? ` (${user.username})` : ''}`"
                      :value="user.id"
                    >
                      <div style="display: flex; align-items: center; justify-content: space-between">
                        <span>{{ user.realName || user.username }}</span>
                        <span style="color: #909399; font-size: 12px">
                          {{ user.deptName }}
                          <span v-if="user.roleName" style="margin-left: 5px">{{ user.roleName }}</span>
                        </span>
                      </div>
                    </el-option>
                  </el-select>
                </div>
              </template>

              <!-- 无配置提示 -->
              <div v-else style="color: #F56C6C; font-size: 13px; text-align: center; padding: 20px;">
                <el-icon style="vertical-align: middle; margin-right: 5px"><WarningFilled /></el-icon>
                该流程第一层未配置审批人，请在流程设计器中配置。
              </div>

              <!-- 已选择提示 -->
              <div style="margin-top: 12px; padding: 10px; background-color: #F0F9FF; border-radius: 4px; text-align: center; font-size: 13px;">
                <span style="color: #409EFF; font-weight: 500">已选择</span>
                <span style="color: #303133; margin: 0 8px">{{ Object.values(selectedFirstStageApprovers).filter(v => v !== null && v !== undefined).length }} / {{ firstStageApproveType === 'OR' ? 1 : firstStageApproverCount }}</span>
                <span style="color: #606266">位审批人</span>
              </div>
            </div>
          </div>
        </el-form-item>

        <!-- 子流程审批人选择 -->
        <template v-for="subWorkflow in subWorkflows" :key="subWorkflow.id">
          <el-form-item>
            <div style="width: 100%; border: 1px solid #E6A23C; border-radius: 6px; padding: 16px; background-color: #FFFBF0">
              <div v-if="subWorkflow.loading" v-loading="true" style="min-height: 50px"></div>
              <template v-else>
                <!-- 子流程标题栏 -->
                <div style="display: flex; align-items: center; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #DCDFE6">
                  <el-icon style="color: #E6A23C; margin-right: 8px;"><Folder /></el-icon>
                  <span style="font-weight: bold; color: #E6A23C; font-size: 14px">子流程：{{ subWorkflow.name || '未命名' }} (ID: {{ subWorkflow.id }})</span>
                  <el-tag v-if="subWorkflow.approveType === 'OR'" type="warning" size="small" style="margin-left: auto">或签</el-tag>
                  <el-tag v-else type="success" size="small" style="margin-left: auto">会签</el-tag>
                </div>

                <!-- 无配置提示 -->
                <div v-if="!subWorkflow.approverConfigs || subWorkflow.approverConfigs.length === 0" style="color: #F56C6C; font-size: 13px">
                  <el-icon><WarningFilled /></el-icon>
                  该子流程未配置阶段或审批人，请在流程设计器中配置。
                </div>
                <!-- 审批人配置选择 -->
                <template v-else>
                  <div style="margin-bottom: 10px; color: #606266; font-size: 13px">
                    <template v-if="subWorkflow.approveType === 'OR'">
                      或签：请从以下配置中选择 1 个审批人
                    </template>
                    <template v-else>
                      会签：请按照配置顺序为每个配置项选择一个审批人，共需要选择 {{ subWorkflow.approverCount }} 个审批人。
                    </template>
                  </div>
                  <div v-for="(config, index) in subWorkflow.approverConfigs" :key="config.configId" style="margin-bottom: 15px">
                    <div style="display: flex; align-items: center; margin-bottom: 5px">
                      <span style="display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; background-color: #E6A23C; color: white; font-size: 12px; margin-right: 8px;">{{ index + 1 }}</span>
                      <span style="font-weight: 500; color: #303133">{{ config.approverTypeName }}</span>
                      <span style="color: #909399; margin-left: 8px">{{ config.approverName }}</span>
                    </div>
                    <el-select
                      v-model="subWorkflow.selectedApprovers[config.configId]"
                      placeholder="请选择审批人"
                      style="width: 100%"
                      clearable
                      filterable
                      @change="handleSubWorkflowApproverChange(subWorkflow)"
                    >
                      <el-option
                        v-for="user in config.availableUsers"
                        :key="user.id"
                        :label="`${user.realName || user.username}${user.username && user.realName ? ` (${user.username})` : ''}`"
                        :value="user.id"
                      >
                        <div style="display: flex; align-items: center; justify-content: space-between">
                          <span>{{ user.realName || user.username }}</span>
                          <span style="color: #909399; font-size: 12px">
                            {{ user.deptName }}
                            <span v-if="user.roleName" style="margin-left: 5px">{{ user.roleName }}</span>
                          </span>
                        </div>
                      </el-option>
                    </el-select>
                  </div>
                  <!-- 已选择提示 -->
                  <div style="margin-top: 8px; color: #67C23A; font-size: 12px">
                    已选择 {{ Object.values(subWorkflow.selectedApprovers).filter(v => v !== null && v !== undefined).length }} / {{ subWorkflow.approveType === 'OR' ? 1 : subWorkflow.approverCount }} 位审批人
                  </div>
                </template>
              </template>
            </div>
          </el-form-item>
        </template>

        <!-- 无需选择审批人的提示 -->
        <el-form-item v-if="boundWorkflow && hasLoadedInitialApprovers && firstStageApproverConfigs.length === 0 && !approverKeyword && subWorkflows.length === 0">
          <div style="color: #E6A23C; font-size: 13px">
            <el-icon><WarningFilled /></el-icon>
            该流程第一层为子流程阶段，将由子流程自动选择审批人，无需手动选择
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSubmitDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="submitting"
          :disabled="!boundWorkflow"
        >
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import { Search, WarningFilled, Document, Folder } from '@element-plus/icons-vue'
import {
  createMaterialApplication,
  updateMaterialApplication,
  submitMaterialApplication,
  getMaterialApplicationById
} from '@/api/materialApplication'
import { getTagList, createTag } from '@/api/tag'
import { uploadAsset, deleteAsset } from '@/api/asset'
import { getWorkflowList, getFirstStageApprovers, selectFirstStageApproversWithSubWorkflows, getSubWorkflowFirstStageApprovers } from '@/api/workflow'
import { useUserStore } from '@/stores/user'
import { getCurrentUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentUser = ref<any>(null)
const saving = ref(false)
const submitting = ref(false)
const addingFile = ref(false)
const fileList = reactive<any[]>([])

const showAddFile = ref(false)
const showCreateTag = ref(false)
const showSubmitDialog = ref(false)

const workflowList = ref<any[]>([])
const tagList = ref<any[]>([])
const boundWorkflow = ref<any>(null) // 角色绑定的审批流程
const copyrightType = ref('none')

// 第一层审批人相关
const firstStageApproverConfigs = ref<any[]>([]) // 第一层审批人配置列表
const selectedFirstStageApprovers = ref<Record<number, number>>({})  // configId -> userId (会签时多个，或签时1个)
const firstStageApproveType = ref('') // 第一层的审批类型（OR=或签，其他=会签）
const firstStageApproverCount = ref(0) // 第一层配置数量
const approverKeyword = ref('')
const loadingApprovers = ref(false)
const hasLoadedInitialApprovers = ref(false) // 标记是否已加载过初始审批人列表

// 子流程相关
const subWorkflows = ref<any[]>([]) // 第一层包含的子流程列表 { id, name, approverConfigs: [], approverCount: 0, selectedApprovers: {}, loading: false }

// 判断是编辑模式还是新建模式
const isEditMode = computed(() => !!route.query.id)
const applicationId = ref<number | null>(route.query.id ? Number(route.query.id) : null)
const applicationStatus = ref<string>('DRAFT')

// 标记是否有未保存的更改
const hasUnsavedChanges = ref(false)

// 保存初始状态用于比较
const initialForm = ref({
  title: '',
  guaranteeDeclaration: false
})
const initialFileCount = ref(0)

const form = reactive({
  title: '',
  maintainerId: null as number | null,
  deptId: null as number | null,
  guaranteeDeclaration: false
})

const fileForm = reactive({
  name: '',
  type: 'IMAGE',
  tagIds: [] as number[],
  copyrightText: '',
  copyrightFilePath: '',
  description: '',
  publishChannel: ''
})

const tagForm = reactive({
  name: '',
  category: ''
})

const uploadFile = ref<File | null>(null)
const copyrightFile = ref<File | null>(null)

const rules = {
  title: [{ required: true, message: '请输入事项标题', trigger: 'blur' }]
}

const fileRules = {
  name: [{ required: true, message: '请输入文件名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择文件类型', trigger: 'change' }]
}

const formRef = ref()
const fileFormRef = ref()

async function loadWorkflows() {
  try {
    // 检查当前用户角色是否绑定了审批流程
    if (currentUser.value?.roleId) {
      const res = await getWorkflowList()
      if (res.data) {
        // 在客户端过滤：找到绑定到当前角色、类型为ASSET_UPLOAD、状态为启用(1)的流程
        const matched = res.data.find((w: any) =>
          w.boundRoleId === currentUser.value.roleId &&
          w.workflowType === 'ASSET_UPLOAD' &&
          w.status === 1
        )
        if (matched) {
          boundWorkflow.value = matched
        }
      }
    }
  } catch (e: any) {
    console.error('加载审批流程失败', e)
    if (e.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    }
  }
}

async function loadFirstStageApprovers() {
  if (!boundWorkflow.value || !currentUser.value?.id) return

  loadingApprovers.value = true
  try {
    const res = await getFirstStageApprovers({
      workflowId: boundWorkflow.value.id,
      applicantId: currentUser.value.id,
      keyword: approverKeyword.value
    })
    // 处理新的响应结构：{ workflowId, workflowName, stageId, stageName, approveType, approverConfigs, approverCount }
    firstStageApproverConfigs.value = res.data?.approverConfigs || []
    firstStageApproveType.value = res.data?.approveType || ''
    firstStageApproverCount.value = res.data?.approverCount || 0

    // 标记已加载过初始列表（无关键词时的加载）
    if (!approverKeyword.value) {
      hasLoadedInitialApprovers.value = true
      // 加载子流程信息
      await loadSubWorkflows()
    }
  } catch (e: any) {
    console.error('加载第一层审批人配置失败', e)
    ElMessage.error(e.message || '加载第一层审批人配置失败')
  } finally {
    loadingApprovers.value = false
  }
}

async function loadSubWorkflows() {
  if (!boundWorkflow.value?.stages || boundWorkflow.value.stages.length === 0) return

  const firstStage = boundWorkflow.value.stages[0]
  if (!firstStage.approvers) return

  // 查找第一层中包含的子流程
  const subWorkflowApprovers = firstStage.approvers.filter((a: any) => a.subWorkflowId)

  if (subWorkflowApprovers.length === 0) return

  // 为每个子流程初始化数据并加载审批人配置
  subWorkflows.value = []
  for (const approver of subWorkflowApprovers) {
    const subWorkflow: any = {
      id: approver.subWorkflowId,
      name: approver.subWorkflowName,
      approverConfigs: [],
      approverCount: 0,
      selectedApprovers: {} as Record<number, number>,
      loading: false
    }
    await loadSubWorkflowApprovers(subWorkflow)
    subWorkflows.value.push(subWorkflow)
  }
}

async function loadSubWorkflowApprovers(subWorkflow: any) {
  if (!currentUser.value?.id) return

  subWorkflow.loading = true
  try {
    const res = await getSubWorkflowFirstStageApprovers({
      subWorkflowId: subWorkflow.id,
      applicantId: currentUser.value.id,
      keyword: '' // 暂不支持子流程审批人搜索
    })
    // 处理新的响应结构：{ workflowId, workflowName, approveType, approverConfigs, approverCount }
    subWorkflow.approveType = res.data?.approveType || ''
    subWorkflow.approverConfigs = res.data?.approverConfigs || []
    subWorkflow.approverCount = res.data?.approverCount || 0
    // 确保已选择的审批人映射被初始化
    if (!subWorkflow.selectedApprovers) {
      subWorkflow.selectedApprovers = {}
    }
  } catch (e: any) {
    console.error(`加载子流程"${subWorkflow.name}"审批人配置失败`, e)
    ElMessage.error(e.message || `加载子流程"${subWorkflow.name}"审批人配置失败`)
  } finally {
    subWorkflow.loading = false
  }
}

// 处理子流程审批人选择变化（或签时只允许选1个）
function handleSubWorkflowApproverChange(subWorkflow: any) {
  if (subWorkflow.approveType === 'OR') {
    const selectedKeys = Object.keys(subWorkflow.selectedApprovers).filter(
      key => subWorkflow.selectedApprovers[key] !== null && subWorkflow.selectedApprovers[key] !== undefined
    )
    // 如果选择了多个，只保留最后一个
    if (selectedKeys.length > 1) {
      const lastKey = selectedKeys[selectedKeys.length - 1]
      const lastValue = subWorkflow.selectedApprovers[lastKey]
      subWorkflow.selectedApprovers = {}
      subWorkflow.selectedApprovers[lastKey] = lastValue
    }
  }
}

// 处理第一层审批人选择变化（或签时只允许选1个）
function handleFirstStageApproverChange() {
  if (firstStageApproveType.value === 'OR') {
    const selectedKeys = Object.keys(selectedFirstStageApprovers.value).filter(
      key => selectedFirstStageApprovers.value[key] !== null && selectedFirstStageApprovers.value[key] !== undefined
    )
    // 如果选择了多个，只保留最后一个
    if (selectedKeys.length > 1) {
      const lastKey = selectedKeys[selectedKeys.length - 1]
      const lastValue = selectedFirstStageApprovers.value[lastKey]
      selectedFirstStageApprovers.value = {}
      selectedFirstStageApprovers.value[lastKey] = lastValue
    }
  }
}

async function loadTags() {
  try {
    const res = await getTagList()
    tagList.value = res.data || []
  } catch (e) {
    console.error('加载标签失败', e)
  }
}

async function loadApplication() {
  if (!applicationId.value) return

  try {
    const res = await getMaterialApplicationById(applicationId.value)
    const app = res.data
    console.log('loadApplication - 原始响应:', res)
    console.log('loadApplication - app.assets:', app.assets)
    console.log('loadApplication - assets数量:', (app.assets || []).length)

    form.title = app.title
    form.maintainerId = app.maintainerId
    form.deptId = app.deptId
    form.guaranteeDeclaration = app.guaranteeDeclaration === 1
    applicationStatus.value = app.status || 'DRAFT'
    // reactive 数组需要清空后重新填充
    fileList.splice(0, fileList.length)
    console.log('loadApplication - 清空后fileList:', fileList)
    fileList.push(...(app.assets || []))
    console.log('loadApplication - 填充后fileList:', fileList)

    // 记录初始状态
    initialForm.value = {
      title: app.title,
      guaranteeDeclaration: app.guaranteeDeclaration === 1
    }
    initialFileCount.value = (app.assets || []).length
    hasUnsavedChanges.value = false
  } catch (e: any) {
    console.error('loadApplication 错误:', e)
    ElMessage.error(e.message || '加载申请单失败')
  }
}

// 检查是否有未保存的更改
function checkUnsavedChanges(): boolean {
  if (isEditMode.value) {
    // 编辑模式：与初始状态比较
    return form.title !== initialForm.value.title ||
           form.guaranteeDeclaration !== initialForm.value.guaranteeDeclaration ||
           fileList.length !== initialFileCount.value
  } else {
    // 新建模式：只要有输入就认为有更改
    return form.title !== '' || form.guaranteeDeclaration || fileList.length > 0
  }
}

// 监听表单和文件列表变化
watch([() => form.title, () => form.guaranteeDeclaration, () => fileList.length], () => {
  hasUnsavedChanges.value = checkUnsavedChanges()
})

// 路由守卫：离开前检查未保存的更改
onBeforeRouteLeave((to, from, next) => {
  // 如果正在保存中，直接放行，避免重复保存
  if (saving.value || submitting.value) {
    next()
    return
  }

  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm(
      '您有未保存的内容，是否保存为草稿？',
      '提示',
      {
        distinguishCancelAndClose: true,
        confirmButtonText: '保存',
        cancelButtonText: '不保存',
        type: 'warning'
      }
    )
      .then(() => {
        // 用户选择保存
        saveDraftAndNavigate(to)
      })
      .catch((action) => {
        if (action === 'cancel') {
          // 用户选择不保存，直接离开
          next()
        } else {
          // 用户点击关闭按钮，取消导航
          next(false)
        }
      })
  } else {
    next()
  }
})

// 保存草稿后导航
async function saveDraftAndNavigate(to: any) {
  await formRef.value?.validate()

  if (!form.guaranteeDeclaration) {
    ElMessage.warning('请勾选保证声明')
    return
  }

  saving.value = true
  try {
    const submitData = {
      ...form,
      guaranteeDeclaration: form.guaranteeDeclaration ? 1 : 0
    }

    if (isEditMode.value) {
      await updateMaterialApplication(applicationId.value!, submitData)
      ElMessage.success('保存成功')
    } else {
      const res = await createMaterialApplication(submitData)
      ElMessage.success('保存成功')
    }
    hasUnsavedChanges.value = false
    router.push(to)
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleSaveDraft() {
  await formRef.value?.validate()

  if (!form.guaranteeDeclaration) {
    ElMessage.warning('请勾选保证声明')
    return
  }

  saving.value = true
  try {
    const submitData = {
      ...form,
      guaranteeDeclaration: form.guaranteeDeclaration ? 1 : 0
    }

    if (isEditMode.value) {
      await updateMaterialApplication(applicationId.value!, submitData)
      ElMessage.success('保存成功')
    } else {
      const res = await createMaterialApplication(submitData)
      applicationId.value = res.data.id
      ElMessage.success('创建成功')
    }
    // 更新初始状态，标记为已保存
    initialForm.value = {
      title: form.title,
      guaranteeDeclaration: form.guaranteeDeclaration
    }
    initialFileCount.value = fileList.length
    hasUnsavedChanges.value = false
    // 保存草稿后跳转到草稿箱
    goToDraftBox()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function handleSubmitDialog() {
  if (!form.guaranteeDeclaration) {
    ElMessage.warning('请勾选保证声明')
    return
  }

  if (fileList.length === 0) {
    ElMessage.warning('请至少添加一个素材文件')
    return
  }

  // 先尝试加载绑定的流程，然后加载第一层审批人
  loadWorkflows().then(() => {
    showSubmitDialog.value = true
    // 重置状态
    approverKeyword.value = ''
    firstStageApproverConfigs.value = []
    selectedFirstStageApprovers.value = {}
    firstStageApproveType.value = ''
    firstStageApproverCount.value = 0
    subWorkflows.value = []
    hasLoadedInitialApprovers.value = false
    loadFirstStageApprovers()
  })
}

async function handleSubmit() {
  if (!boundWorkflow.value) {
    ElMessage.error('您的角色未绑定审批流程，无法提交审批')
    return
  }

  // 检查是否需要选择第一层审批人
  if (firstStageApproveType.value === 'OR') {
    // 或签：需要选择1个审批人 + 所有子流程
    if (firstStageApproverCount.value > 0) {
      const selectedCount = Object.values(selectedFirstStageApprovers.value).filter(v => v !== null && v !== undefined).length
      if (selectedCount === 0) {
        ElMessage.warning('请选择第一层审批人（或签需要选择1位）')
        return
      }
    }
  } else {
    // 会签：需要选择所有配置的审批人 + 所有子流程
    if (firstStageApproverCount.value > 0) {
      const selectedCount = Object.values(selectedFirstStageApprovers.value).filter(v => v !== null && v !== undefined).length
      if (selectedCount < firstStageApproverCount.value) {
        ElMessage.warning(`请为所有配置项选择审批人（已选择 ${selectedCount}/${firstStageApproverCount.value}）`)
        return
      }
    }
  }

  // 检查是否需要选择子流程审批人（根据子流程自己的或签/会签类型）
  for (const subWorkflow of subWorkflows.value) {
    if (subWorkflow.approverCount > 0) {
      const selectedCount = Object.values(subWorkflow.selectedApprovers).filter(v => v !== null && v !== undefined).length

      if (subWorkflow.approveType === 'OR') {
        // 子流程或签：至少选1个
        if (selectedCount === 0) {
          ElMessage.warning(`请为子流程"${subWorkflow.name}"选择至少 1 位审批人（或签）`)
          return
        }
      } else {
        // 子流程会签：所有配置都要选
        if (selectedCount < subWorkflow.approverCount) {
          ElMessage.warning(`请为子流程"${subWorkflow.name}"选择所有 ${subWorkflow.approverCount} 位审批人（当前已选 ${selectedCount} 位）`)
          return
        }
      }
    }
  }

  submitting.value = true
  try {
    // 先保存基本信息
    if (!applicationId.value) {
      const submitData = {
        ...form,
        guaranteeDeclaration: form.guaranteeDeclaration ? 1 : 0
      }
      const res = await createMaterialApplication(submitData)
      applicationId.value = res.data.id
    }

    // 提交审批（创建审批实例）
    const submitRes = await submitMaterialApplication(applicationId.value!, boundWorkflow.value.id)
    const instanceId = submitRes.data

    // 如果有第一层审批人需要选择，先选择审批人
    if ((firstStageApproverCount.value > 0 || subWorkflows.value.length > 0) && instanceId) {
      // 构建子流程审批人选择映射（从 configId -> userId 转换为数组）
      const subWorkflowApproverIds: Record<number, number[]> = {}
      for (const subWorkflow of subWorkflows.value) {
        const selectedIds = Object.values(subWorkflow.selectedApprovers).filter(v => v !== null && v !== undefined) as number[]
        if (selectedIds.length > 0) {
          subWorkflowApproverIds[subWorkflow.id] = selectedIds
        }
      }

      // 构建主流程审批人ID列表（按配置顺序）
      const mainApproverIds: number[] = []
      // 按配置顺序获取选中的用户ID
      for (const config of firstStageApproverConfigs.value) {
        const selectedUserId = selectedFirstStageApprovers.value[config.configId]
        if (selectedUserId) {
          mainApproverIds.push(selectedUserId)
        }
      }

      // 如果有子流程或主流程审批人，使用新API
      if (Object.keys(subWorkflowApproverIds).length > 0 || mainApproverIds.length > 0) {
        await selectFirstStageApproversWithSubWorkflows({
          instanceId,
          approverIds: mainApproverIds,
          subWorkflowApproverIds
        })
      }
    }

    ElMessage.success('提交成功')
    showSubmitDialog.value = false
    hasUnsavedChanges.value = false
    goToList()
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function handleFileChange(file: any) {
  uploadFile.value = file.raw
  if (!fileForm.name) {
    fileForm.name = file.name
  }
}

function handleCopyrightFileChange(file: any) {
  copyrightFile.value = file.raw
}

async function handleAddFile() {
  await fileFormRef.value?.validate()

  if (!uploadFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  // 确保有申请单ID
  if (!applicationId.value) {
    await formRef.value?.validate()
    if (!form.title) {
      ElMessage.warning('请先输入事项标题')
      return
    }
    if (!form.guaranteeDeclaration) {
      ElMessage.warning('请先勾选保证声明')
      return
    }

    const submitData = {
      ...form,
      guaranteeDeclaration: form.guaranteeDeclaration ? 1 : 0
    }
    const res = await createMaterialApplication(submitData)
    applicationId.value = res.data.id
  }

  addingFile.value = true
  try {
    console.log('handleAddFile - applicationId:', applicationId.value)
    console.log('handleAddFile - 上传数据:', {
      ...fileForm,
      applicationId: applicationId.value
    })
    const uploadRes = await uploadAsset(uploadFile.value, {
      ...fileForm,
      applicationId: applicationId.value
    })
    console.log('handleAddFile - 上传响应:', uploadRes)
    ElMessage.success('添加成功')
    showAddFile.value = false
    resetFileForm()
    // 重新加载文件列表
    console.log('handleAddFile - 准备重新加载...')
    await loadApplication()
    console.log('handleAddFile - 重新加载完成, fileList.length:', fileList.length)
  } catch (e: any) {
    console.error('handleAddFile - 上传失败:', e)
    ElMessage.error(e.message || '添加失败')
  } finally {
    addingFile.value = false
  }
}

function resetFileForm() {
  fileForm.name = ''
  fileForm.type = 'IMAGE'
  fileForm.tagIds = []
  fileForm.copyrightText = ''
  fileForm.copyrightFilePath = ''
  fileForm.description = ''
  fileForm.publishChannel = ''
  uploadFile.value = null
  copyrightFile.value = null
  copyrightType.value = 'none'
}

async function removeFile(row: any) {
  // 检查申请单状态，只有草稿状态可以移除文件
  if (applicationStatus.value !== 'DRAFT') {
    ElMessage.warning('只有草稿状态可以移除文件')
    return
  }

  try {
    await ElMessageBox.confirm('确定要移除该文件吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    // 调用后端 API 删除文件
    await deleteAsset(row.id)
    ElMessage.success('移除成功')

    // 从列表中移除
    const index = fileList.findIndex(f => f.id === row.id)
    if (index > -1) {
      fileList.splice(index, 1)
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '移除失败')
    }
  }
}

async function handleCreateTag() {
  if (!tagForm.name) {
    ElMessage.warning('请输入标签名称')
    return
  }
  try {
    await createTag({ name: tagForm.name, category: tagForm.category })
    ElMessage.success('创建成功')
    showCreateTag.value = false
    tagForm.name = ''
    tagForm.category = ''
    loadTags()
  } catch (e: any) {
    ElMessage.error(e.message || '创建失败')
  }
}

function goBack() {
  goToList()
}

function goToDraftBox() {
  router.push('/task/draft-box')
}

function goToList() {
  router.push('/asset')
}

onMounted(async () => {
  // Fetch fresh user data from backend to get deptName
  try {
    const res = await getCurrentUser()
    currentUser.value = res.data
    userStore.setUserInfo(res.data)
  } catch (e) {
    currentUser.value = userStore.userInfo
  }

  form.maintainerId = currentUser.value?.id
  form.deptId = currentUser.value?.deptId

  await Promise.all([
    loadWorkflows(),
    loadTags()
  ])

  // 如果是编辑模式，加载申请单数据
  if (isEditMode.value) {
    await loadApplication()
  }
})
</script>

<style scoped>
.file-section {
  margin-top: 20px;
}
.file-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: bold;
}
.action-buttons {
  margin-top: 30px;
  text-align: center;
}
.action-buttons .el-button {
  margin: 0 10px;
}
</style>
