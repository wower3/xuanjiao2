<!-- 素材使用申请页面 - 提供素材使用申请的新建和编辑功能 -->
<template>
  <div class="usage-apply-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>{{ isEdit ? '编辑使用申请' : '素材使用申请' }}</span>
          <div class="actions">
            <el-button @click="goToList">我的申请</el-button>
            <el-button @click="goToDrafts">草稿箱</el-button>
            <el-button @click="goBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="申请标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入申请标题" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 素材列表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="header">
          <span>选择素材 ({{ selectedAssets.length }})</span>
          <el-button type="primary" @click="showAssetSelector = true">添加素材</el-button>
        </div>
      </template>

      <el-table :data="selectedAssets" v-loading="loadingAssets">
        <el-table-column label="预览" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.type === 'IMAGE'"
              :src="getPreviewUrl(row.id)"
              style="width: 60px; height: 40px"
              fit="cover"
              :preview-src-list="[getPreviewUrl(row.id)]"
              :preview-teleported="true"
            />
            <el-image
              v-else-if="row.type === 'VIDEO' && row.thumbnailPath"
              :src="`/api/asset/thumbnail/${row.id}`"
              fit="cover"
              style="width: 60px; height: 40px"
            />
            <el-icon v-else-if="row.type === 'VIDEO'" :size="30"><VideoCamera /></el-icon>
            <el-icon v-else :size="30"><Document /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="素材名称" min-width="200" />
        <el-table-column prop="type" label="类型" width="80" />
        <el-table-column label="使用配置" width="200">
          <template #default="{ row }">
            <div v-if="isAssetConfigured(row)" class="config-summary">
              <div v-if="row.usagePublishChannel" class="config-item">
                渠道: {{ row.usagePublishChannel }}
              </div>
              <div v-if="row.usageIsSecondaryCreation === 1" class="config-item">
                <el-tag size="small" type="warning">二次创作</el-tag>
              </div>
            </div>
            <div v-else class="config-item unconfigured">
              未配置
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row, $index }">
            <el-button link type="primary" @click="openConfigDialog(row)">配置</el-button>
            <el-button link type="danger" @click="removeAsset($index)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="selectedAssets.length === 0" description="请点击「添加素材」选择要使用的素材" />
    </el-card>

    <div class="footer-actions">
      <el-button @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
      <el-button type="primary" @click="handleSubmitDialog" :disabled="selectedAssets.length === 0">提交审批</el-button>
    </div>

    <!-- 素材选择器对话框 -->
    <el-dialog v-model="showAssetSelector" title="选择素材" width="1000px">
      <asset-selector
        :selected-ids="selectedAssetIds"
        @select="handleAssetSelect"
        @cancel="showAssetSelector = false"
      />
    </el-dialog>

    <!-- 素材使用配置对话框 -->
    <el-dialog v-model="showConfigDialog" title="配置素材使用信息" width="600px">
      <el-form :model="configForm" :rules="configRules" ref="configFormRef" label-width="120px">
        <el-form-item label="素材名称">
          <el-input :value="currentAsset?.name" disabled />
        </el-form-item>
        <el-form-item label="申请说明" prop="usageDescription">
          <el-input v-model="configForm.usageDescription" type="textarea" :rows="3" placeholder="请说明使用用途" />
        </el-form-item>
        <el-form-item label="发布渠道" prop="usagePublishChannel">
          <el-input v-model="configForm.usagePublishChannel" placeholder="请输入发布渠道" />
        </el-form-item>
        <el-form-item label="二次创作">
          <el-checkbox v-model="configForm.usageIsSecondaryCreation">是否进行二次创作</el-checkbox>
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :file-list="configFileList"
            :limit="1"
          >
            <el-button>上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持上传单个附件文件</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showConfigDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
      </template>
    </el-dialog>

    <!-- 提交审批对话框 -->
    <el-dialog v-model="showSubmitDialog" title="提交审批" width="800px">
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
                <el-icon style="color: #409EFF; margin-right: 8px"><Document /></el-icon>
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
          <div style="color: #67C23A; font-size: 13px">
            <el-icon><SuccessFilled /></el-icon>
            该流程第一层为子流程阶段，将由子流程自动选择审批人。请直接点击「提交」按钮完成提交。
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoCamera, Document, InfoFilled, WarningFilled, Folder } from '@element-plus/icons-vue'
import AssetSelector from '@/components/AssetSelector.vue'
import {
  createUsageDraft,
  updateUsageDraft,
  submitUsageApply,
  getUsageApplyById
} from '@/api/usageApply'
import { getAssetById } from '@/api/asset'
import { getWorkflowList, getWorkflowById, getFirstStageApprovers, selectFirstStageApproversWithSubWorkflows, getSubWorkflowFirstStageApprovers } from '@/api/workflow'
import { getCurrentUser } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const configFormRef = ref()
const loading = ref(false)
const loadingAssets = ref(false)
const saving = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const showAssetSelector = ref(false)
const showConfigDialog = ref(false)
const showSubmitDialog = ref(false)
const selectedAssets = ref<any[]>([])
const currentAsset = ref<any>(null)

// 审批相关
const currentUser = ref<any>(null)
const boundWorkflow = ref<any>(null)
const loadingApprovers = ref(false)
const approverKeyword = ref('')
const hasLoadedInitialApprovers = ref(false)

// 第一层审批人相关
const firstStageApproverConfigs = ref<any[]>([])
const selectedFirstStageApprovers = ref<Record<number, number>>({})
const firstStageApproveType = ref('')
const firstStageApproverCount = ref(0)

// 子流程相关
const subWorkflows = ref<any[]>([])

const uploadUrl = computed(() => '/api/file/upload')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const form = reactive({
  title: ''
})

const configForm = reactive({
  usageDescription: '',
  usagePublishChannel: '',
  usageIsSecondaryCreation: false,
  usageAttachmentPath: ''
})

const configFileList = ref<any[]>([])

const rules = {
  title: [{ required: true, message: '请输入申请标题', trigger: 'blur' }]
}

const configRules = {
  usageDescription: [{ required: true, message: '请输入申请说明', trigger: 'blur' }],
  usagePublishChannel: [{ required: true, message: '请输入发布渠道', trigger: 'blur' }]
}

const selectedAssetIds = computed(() => selectedAssets.value.map(a => a.id))

// 标记是否有未保存的更改
const hasUnsavedChanges = ref(false)

// 监听表单变化
watch([() => form.title, () => selectedAssets.value], () => {
  hasUnsavedChanges.value = true
}, { deep: true })

async function loadCurrentUser() {
  try {
    const res = await getCurrentUser()
    currentUser.value = res.data
    userStore.setUserInfo(res.data)
  } catch (e) {
    currentUser.value = userStore.userInfo
  }
}

async function loadWorkflows() {
  try {
    if (currentUser.value?.roleId) {
      const res = await getWorkflowList()
      if (res.data) {
        // 在客户端过滤：找到绑定到当前角色、类型为ASSET_USAGE、状态为启用(1)的流程
        const matched = res.data.find((w: any) =>
          w.boundRoleId === currentUser.value.roleId &&
          w.workflowType === 'ASSET_USAGE' &&
          w.status === 1
        )
        if (matched) {
          // 流程列表不包含stages信息，需要单独调用getWorkflowById获取完整信息
          const detailRes = await getWorkflowById(matched.id)
          boundWorkflow.value = detailRes.data
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
    firstStageApproverConfigs.value = res.data?.approverConfigs || []
    firstStageApproveType.value = res.data?.approveType || ''
    firstStageApproverCount.value = res.data?.approverCount || 0

    if (!approverKeyword.value) {
      hasLoadedInitialApprovers.value = true
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

  const subWorkflowApprovers = firstStage.approvers.filter((a: any) => a.subWorkflowId)

  if (subWorkflowApprovers.length === 0) return

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
      keyword: ''
    })
    subWorkflow.approveType = res.data?.approveType || ''
    subWorkflow.approverConfigs = res.data?.approverConfigs || []
    subWorkflow.approverCount = res.data?.approverCount || 0
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

function handleFirstStageApproverChange() {
  if (firstStageApproveType.value === 'OR') {
    const selectedKeys = Object.keys(selectedFirstStageApprovers.value).filter(
      key => selectedFirstStageApprovers.value[key] !== null && selectedFirstStageApprovers.value[key] !== undefined
    )
    if (selectedKeys.length > 1) {
      const lastKey = selectedKeys[selectedKeys.length - 1]
      const lastValue = selectedFirstStageApprovers.value[lastKey]
      selectedFirstStageApprovers.value = {}
      selectedFirstStageApprovers.value[lastKey] = lastValue
    }
  }
}

function handleSubWorkflowApproverChange(subWorkflow: any) {
  if (subWorkflow.approveType === 'OR') {
    const selectedKeys = Object.keys(subWorkflow.selectedApprovers).filter(
      key => subWorkflow.selectedApprovers[key] !== null && subWorkflow.selectedApprovers[key] !== undefined
    )
    if (selectedKeys.length > 1) {
      const lastKey = selectedKeys[selectedKeys.length - 1]
      const lastValue = subWorkflow.selectedApprovers[lastKey]
      subWorkflow.selectedApprovers = {}
      subWorkflow.selectedApprovers[lastKey] = lastValue
    }
  }
}

async function loadData() {
  const id = route.query.id as number
  const assetId = route.query.assetId as number

  if (id) {
    isEdit.value = true
    currentId.value = id
    loading.value = true
    try {
      const res = await getUsageApplyById(id)
      const data = res.data
      form.title = data.title
      // 转换 assets 字段：将 assetId/assetName/assetType 映射为 id/name/type
      selectedAssets.value = (data.assets || []).map((asset: any) => ({
        ...asset,
        id: asset.assetId,
        name: asset.assetName,
        type: asset.assetType
      }))
      hasUnsavedChanges.value = false
    } catch (e) {
      ElMessage.error('加载失败')
    } finally {
      loading.value = false
    }
  } else if (assetId) {
    await loadSingleAsset(assetId)
  }
}

async function loadSingleAsset(assetId: number) {
  loadingAssets.value = true
  try {
    const res = await getAssetById(assetId)
    if (res.data) {
      const asset = res.data
      if (asset.status === 'APPROVED') {
        selectedAssets.value.push({
          ...asset,
          usageDescription: null,
          usagePublishChannel: null,
          usageIsSecondaryCreation: 0,
          usageAttachmentPath: null
        })
        openConfigDialog(selectedAssets.value[0])
      } else {
        ElMessage.warning('只能使用已通过的素材')
      }
    }
  } catch (e) {
    console.error('加载素材失败:', e)
    ElMessage.error('加载素材失败')
  } finally {
    loadingAssets.value = false
  }
}

function handleAssetSelect(assets: any[]) {
  for (const asset of assets) {
    if (!selectedAssetIds.value.includes(asset.id)) {
      selectedAssets.value.push({
        ...asset,
        usageDescription: null,
        usagePublishChannel: null,
        usageIsSecondaryCreation: 0,
        usageAttachmentPath: null
      })
    }
  }
  showAssetSelector.value = false
  hasUnsavedChanges.value = true
}

function removeAsset(index: number) {
  selectedAssets.value.splice(index, 1)
  hasUnsavedChanges.value = true
}

function getPreviewUrl(id: number) {
  return `/api/asset/preview/${id}`
}

function isAssetConfigured(asset: any): boolean {
  return !!(asset.usageDescription || asset.usagePublishChannel || asset.usageIsSecondaryCreation === 1 || asset.usageAttachmentPath)
}

function openConfigDialog(asset: any) {
  currentAsset.value = asset
  configForm.usageDescription = asset.usageDescription || ''
  configForm.usagePublishChannel = asset.usagePublishChannel || ''
  configForm.usageIsSecondaryCreation = asset.usageIsSecondaryCreation === 1
  configForm.usageAttachmentPath = asset.usageAttachmentPath || ''

  if (asset.usageAttachmentPath) {
    configFileList.value = [{
      name: asset.usageAttachmentPath.split('/').pop(),
      url: asset.usageAttachmentPath
    }]
  } else {
    configFileList.value = []
  }

  showConfigDialog.value = true
}

function handleUploadSuccess(response: any) {
  if (response.code === 200) {
    configForm.usageAttachmentPath = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

async function handleSaveConfig() {
  try {
    await configFormRef.value?.validate()
  } catch {
    return
  }

  if (currentAsset.value) {
    currentAsset.value.usageDescription = configForm.usageDescription
    currentAsset.value.usagePublishChannel = configForm.usagePublishChannel
    currentAsset.value.usageIsSecondaryCreation = configForm.usageIsSecondaryCreation ? 1 : 0
    currentAsset.value.usageAttachmentPath = configForm.usageAttachmentPath
    hasUnsavedChanges.value = true
  }

  showConfigDialog.value = false
  ElMessage.success('配置已保存')
}

async function handleSaveDraft() {
  // 保存草稿时只验证标题，不验证素材配置信息
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  if (selectedAssets.value.length === 0) {
    ElMessage.warning('请至少选择一个素材')
    return
  }

  // 注意：保存草稿时不检查素材配置信息，允许用户保存未完成的草稿
  // 只有在提交审批时才检查所有素材是否都已配置

  saving.value = true
  try {
    const data = buildSubmitData()

    if (isEdit.value && currentId.value) {
      await updateUsageDraft(currentId.value, data)
      ElMessage.success('保存成功')
    } else {
      const res = await createUsageDraft(data)
      currentId.value = res.data.id  // 修复：获取 ID 而不是整个对象
      isEdit.value = true
      ElMessage.success('草稿已保存')
    }
    hasUnsavedChanges.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleSubmitDialog() {
  // 先验证表单
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  if (selectedAssets.value.length === 0) {
    ElMessage.warning('请至少选择一个素材')
    return
  }

  // 检查所有素材是否都已配置
  const unconfigured = selectedAssets.value.filter(a => !isAssetConfigured(a))
  if (unconfigured.length > 0) {
    ElMessage.warning(`还有 ${unconfigured.length} 个素材未配置使用信息`)
    return
  }

  // 先保存草稿（如果有未保存的更改）
  if (hasUnsavedChanges.value) {
    await handleSaveDraft()
  }

  // 如果是新建且没有保存成功，提示用户
  if (!currentId.value) {
    ElMessage.warning('请先保存草稿后再提交审批')
    return
  }

  // 先尝试加载绑定的流程，然后加载第一层审批人
  await loadWorkflows()
  showSubmitDialog.value = true
  approverKeyword.value = ''
  firstStageApproverConfigs.value = []
  selectedFirstStageApprovers.value = {}
  firstStageApproveType.value = ''
  firstStageApproverCount.value = 0
  subWorkflows.value = []
  hasLoadedInitialApprovers.value = false
  await loadFirstStageApprovers()
}

async function handleSubmit() {
  if (!boundWorkflow.value) {
    ElMessage.error('您的角色未绑定审批流程，无法提交审批')
    return
  }

  // 检查是否需要选择第一层审批人
  if (firstStageApproveType.value === 'OR') {
    if (firstStageApproverCount.value > 0) {
      const selectedCount = Object.values(selectedFirstStageApprovers.value).filter(v => v !== null && v !== undefined).length
      if (selectedCount === 0) {
        ElMessage.warning('请选择第一层审批人（或签需要选择1位）')
        return
      }
    }
  } else {
    if (firstStageApproverCount.value > 0) {
      const selectedCount = Object.values(selectedFirstStageApprovers.value).filter(v => v !== null && v !== undefined).length
      if (selectedCount < firstStageApproverCount.value) {
        ElMessage.warning(`请为所有配置项选择审批人（已选择 ${selectedCount}/${firstStageApproverCount.value}）`)
        return
      }
    }
  }

  // 检查是否需要选择子流程审批人
  for (const subWorkflow of subWorkflows.value) {
    if (subWorkflow.approverCount > 0) {
      const selectedCount = Object.values(subWorkflow.selectedApprovers).filter(v => v !== null && v !== undefined).length

      if (subWorkflow.approveType === 'OR') {
        if (selectedCount === 0) {
          ElMessage.warning(`请为子流程"${subWorkflow.name}"选择至少 1 位审批人（或签）`)
          return
        }
      } else {
        if (selectedCount < subWorkflow.approverCount) {
          ElMessage.warning(`请为子流程"${subWorkflow.name}"选择所有 ${subWorkflow.approverCount} 位审批人（当前已选 ${selectedCount} 位）`)
          return
        }
      }
    }
  }

  // 确保有草稿ID（已在 handleSubmitDialog 中保存过）
  if (!currentId.value) {
    ElMessage.error('请先保存草稿')
    return
  }

  submitting.value = true
  try {
    // 提交审批（创建审批实例）
    const instanceId = await submitUsageApply(currentId.value, boundWorkflow.value.id)

    // 如果有第一层审批人需要选择，先选择审批人
    if ((firstStageApproverCount.value > 0 || subWorkflows.value.length > 0) && instanceId.data) {
      const subWorkflowApproverIds: Record<number, number[]> = {}
      for (const subWorkflow of subWorkflows.value) {
        const selectedIds = Object.values(subWorkflow.selectedApprovers).filter(v => v !== null && v !== undefined) as number[]
        if (selectedIds.length > 0) {
          subWorkflowApproverIds[subWorkflow.id] = selectedIds
        }
      }

      const mainApproverIds: number[] = []
      for (const config of firstStageApproverConfigs.value) {
        const selectedUserId = selectedFirstStageApprovers.value[config.configId]
        if (selectedUserId) {
          mainApproverIds.push(selectedUserId)
        }
      }

      if (Object.keys(subWorkflowApproverIds).length > 0 || mainApproverIds.length > 0) {
        await selectFirstStageApproversWithSubWorkflows({
          instanceId: instanceId.data,
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

function buildSubmitData() {
  return {
    title: form.title,
    assetConfigs: selectedAssets.value.map(asset => ({
      assetId: asset.id,
      usageDescription: asset.usageDescription || '',
      usagePublishChannel: asset.usagePublishChannel || '',
      usageIsSecondaryCreation: asset.usageIsSecondaryCreation || 0,
      usageAttachmentPath: asset.usageAttachmentPath || ''
    }))
  }
}

function goToList() {
  router.push('/task/my-initiated')
}

function goToDrafts() {
  router.push('/task/draft-box')
}

function goBack() {
  router.back()
}

// 离开前确认
onBeforeRouteLeave((to, from, next) => {
  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm(
      '您有未保存的内容，确定要离开吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
      .then(() => {
        next()
      })
      .catch(() => {
        next(false)
      })
  } else {
    next()
  }
})

onMounted(async () => {
  await loadCurrentUser()
  await loadWorkflows()
  loadData()
})
</script>

<style scoped>
.usage-apply-page {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  display: flex;
  gap: 10px;
}

.footer-actions {
  margin-top: 20px;
  text-align: center;
}

.footer-actions .el-button {
  margin: 0 10px;
}

.config-summary {
  font-size: 12px;
  line-height: 1.5;
}

.config-item {
  margin-bottom: 2px;
  color: #606266;
}

.config-item.unconfigured {
  color: #C0C4CC;
}
</style>
