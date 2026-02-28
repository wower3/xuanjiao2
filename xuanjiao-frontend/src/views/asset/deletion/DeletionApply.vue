<template>
  <div class="deletion-apply-page">
    <!-- 删除申请表单 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>素材删除申请</span>
          <div class="actions">
            <el-button v-if="props.selectedAssetIds.length > 0" @click="handleBackToAssets">返回素材列表</el-button>
            <el-button @click="goToList">我的申请</el-button>
            <el-button @click="goToDrafts">草稿箱</el-button>
          </div>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="申请标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入申请标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="删除原因" prop="deleteReason">
          <el-input v-model="form.deleteReason" type="textarea" :rows="4" placeholder="请详细说明删除原因" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            action="#"
          >
            <el-button type="primary">选择附件</el-button>
            <template #tip>
              <div style="color: #909399; font-size: 12px; margin-top: 5px">
                支持上传相关证明文件（可选）
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 已选素材列表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="header">
          <span>已选素材 ({{ localSelectedAssets.length }})</span>
          <el-button type="primary" @click="showAssetSelector = true">添加素材</el-button>
        </div>
      </template>

      <el-table :data="localSelectedAssets" v-loading="loadingAssets" size="small">
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
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else type="info">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ $index }">
            <el-button link type="danger" @click="removeAsset($index)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="localSelectedAssets.length === 0" description="请点击「添加素材」选择要删除的素材" />
    </el-card>

    <!-- 操作按钮 -->
    <div class="footer-actions">
      <el-button @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
      <el-button type="primary" @click="handleSubmitDialog" :disabled="localSelectedAssets.length === 0">提交审批</el-button>
    </div>

    <!-- 素材选择器对话框 -->
    <el-dialog v-model="showAssetSelector" title="选择素材" width="1000px">
      <asset-selector
        :selected-ids="localSelectedAssetIds"
        @select="handleAssetSelect"
        @cancel="showAssetSelector = false"
      />
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
        <el-form-item label="第一层审批人" v-if="firstStageApproverConfigs.length > 0">
          <div style="width: 100%">
            <div style="margin-bottom: 12px; padding: 10px; background-color: #ECF5FF; border-radius: 4px; border-left: 3px solid #409EFF; font-size: 13px;">
              <template v-if="firstStageApproveType === 'OR'">
                或签：请从以下配置中选择 1 个审批人
              </template>
              <template v-else>
                会签：请为每个配置项选择 1 个审批人
              </template>
            </div>

            <div v-for="(config, index) in firstStageApproverConfigs" :key="config.configId" style="margin-bottom: 12px">
              <div style="font-weight: 500; margin-bottom: 8px; font-size: 13px">
                {{ index + 1 }}. {{ config.approverTypeName }}：{{ config.approverName }}
              </div>
              <el-select
                v-model="selectedFirstStageApprovers[config.configId]"
                filterable
                placeholder="请选择审批人"
                style="width: 100%;"
                clearable
              >
                <el-option
                  v-for="user in (config.availableUsers || [])"
                  :key="user.id"
                  :label="user.realName || user.username"
                  :value="user.id"
                />
              </el-select>
            </div>
          </div>
        </el-form-item>

        <!-- 无需选择审批人的提示 -->
        <el-form-item v-if="boundWorkflow && hasLoadedInitialApprovers && firstStageApproverConfigs.length === 0 && subWorkflows.length === 0">
          <div style="color: #E6A23C; font-size: 13px">
            该流程无需手动选择审批人，系统将自动分配
          </div>
        </el-form-item>

        <!-- 子流程审批人选择 -->
        <template v-for="subWorkflow in subWorkflows" :key="subWorkflow.id">
          <el-form-item :label="'子流程：' + (subWorkflow.name || '未命名')">
            <div style="width: 100%">
              <div v-if="subWorkflow.loading" v-loading="true" style="min-height: 50px"></div>
              <div v-else>
                <!-- 子流程标题栏 -->
                <div style="margin-bottom: 10px; padding: 8px; background-color: #FDF6EC; border-radius: 4px; border-left: 3px solid #E6A23C; display: flex; align-items: center;">
                  <span style="font-weight: bold; color: #E6A23C; font-size: 14px">子流程：{{ subWorkflow.name || '未命名' }} (ID: {{ subWorkflow.id }})</span>
                  <el-tag v-if="subWorkflow.approveType === 'OR'" type="warning" size="small" style="margin-left: auto">或签</el-tag>
                </div>

                <div v-if="!subWorkflow.approverConfigs || subWorkflow.approverConfigs.length === 0" style="color: #F56C6C; font-size: 13px">
                  该子流程未配置阶段或审批人，请在流程设计器中配置。
                </div>
                <template v-else>
                  <div style="margin-bottom: 8px; font-size: 13px; color: #606266;">
                    <template v-if="subWorkflow.approveType === 'OR'">
                      或签：请从以下配置中选择 1 个审批人
                    </template>
                    <template v-else>
                      会签：请按照配置顺序为每个配置项选择审批人，共需要选择 {{ subWorkflow.approverCount }} 个审批人。
                    </template>
                  </div>
                  <div v-for="(config, index) in subWorkflow.approverConfigs" :key="config.configId" style="margin-bottom: 15px">
                    <div style="font-weight: 500; margin-bottom: 8px; font-size: 13px">
                      {{ index + 1 }}. {{ config.approverTypeName }}：{{ config.approverName }}
                    </div>
                    <el-select
                      v-model="subWorkflow.selectedApprovers[config.configId]"
                      filterable
                      placeholder="请选择审批人"
                      style="width: 100%;"
                      clearable
                      @change="handleSubWorkflowApproverChange(subWorkflow)"
                    >
                      <el-option
                        v-for="user in (config.availableUsers || [])"
                        :key="user.id"
                        :label="user.realName || user.username"
                        :value="user.id"
                      />
                    </el-select>
                  </div>
                  <div style="font-size: 12px; color: #909399; margin-top: 8px;">
                    已选择 {{ Object.values(subWorkflow.selectedApprovers).filter(v => v !== null && v !== undefined).length }} / {{ subWorkflow.approveType === 'OR' ? 1 : subWorkflow.approverCount }} 位审批人
                  </div>
                </template>
              </div>
            </div>
          </el-form-item>
        </template>
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

    <!-- 提交成功状态显示 -->
    <el-alert
      v-if="submitted"
      title="申请已提交"
      type="success"
      :closable="false"
      style="margin-top: 20px"
    >
      <template #default>
        <div>您的删除申请已提交，请等待审批。</div>
        <div style="margin-top: 10px">
          <el-button type="primary" link @click="goToList">查看我的申请</el-button>
          <el-button type="primary" link @click="handleCreateNew">创建新申请</el-button>
        </div>
      </template>
    </el-alert>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoCamera, Document } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AssetSelector from '@/components/AssetSelector.vue'
import {
  createDeletionApplication,
  updateDeletionApplication,
  submitDeletionApplication,
  getMyDeletionApplications,
  getDeletionApplicationById
} from '@/api/assetDeletion'
import { getWorkflowList, getWorkflowById, getFirstStageApprovers, selectFirstStageApproversWithSubWorkflows, getSubWorkflowFirstStageApprovers } from '@/api/workflow'
import { getCurrentUser } from '@/api/user'

interface Props {
  selectedAssetIds?: number[]
  selectedAssets?: any[]
  applicationId?: number | null
}

const props = withDefaults(defineProps<Props>(), {
  selectedAssetIds: () => [],
  selectedAssets: () => [],
  applicationId: null
})

const emit = defineEmits<{
  backToAssets: []
  reset: []
}>()

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const uploadRef = ref()
const saving = ref(false)
const submitting = ref(false)
const submitted = ref(false)
const loadingAssets = ref(false)
const showAssetSelector = ref(false)
const showSubmitDialog = ref(false)

// 当前用户
const currentUser = ref<any>(null)

// 审批流程相关
const boundWorkflow = ref<any>(null)
const firstStageApproverConfigs = ref<any[]>([])
const firstStageApproveType = ref('')
const firstStageApproverCount = ref(0)
const selectedFirstStageApprovers = ref<Record<number, number>>({})
const hasLoadedInitialApprovers = ref(false)
const subWorkflows = ref<any[]>([]) // 第一层包含的子流程列表

// 本地维护的选中素材
const localSelectedAssets = ref<any[]>([...props.selectedAssets])
const localSelectedAssetIds = computed(() => localSelectedAssets.value.map(a => a.id))

// 编辑模式（用于草稿编辑）
const isEdit = ref(false)
const currentId = ref<number | null>(null)

const form = reactive({
  title: '',
  deleteReason: '',
  attachmentPath: ''
})

const uploadFile = ref<File | null>(null)

const rules = {
  title: [{ required: true, message: '请输入申请标题', trigger: 'blur' }],
  deleteReason: [{ required: true, message: '请输入删除原因', trigger: 'blur' }]
}

// 加载当前用户信息
async function loadCurrentUser() {
  try {
    const res = await getCurrentUser()
    currentUser.value = res.data
    userStore.setUserInfo(res.data)
  } catch (e) {
    currentUser.value = userStore.userInfo
  }
}

// 监听父组件传入的预选素材
watch(
  () => props.selectedAssets,
  (newAssets) => {
    if (newAssets && newAssets.length > 0) {
      localSelectedAssets.value = [...newAssets]
    }
  },
  { immediate: true }
)

// 加载绑定的审批流程
async function loadWorkflows() {
  try {
    if (currentUser.value?.roleId) {
      const res = await getWorkflowList()
      if (res.data) {
        // 在客户端过滤：找到绑定到当前角色、类型为ASSET_DELETION、状态为启用(1)的流程
        const matched = res.data.find((w: any) =>
          w.boundRoleId === currentUser.value.roleId &&
          w.workflowType === 'ASSET_DELETION' &&
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

// 加载第一层审批人配置
async function loadFirstStageApprovers() {
  if (!boundWorkflow.value || !currentUser.value?.id) return

  loadingAssets.value = true
  try {
    const res = await getFirstStageApprovers({
      workflowId: boundWorkflow.value.id,
      applicantId: currentUser.value.id
    })
    firstStageApproverConfigs.value = res.data?.approverConfigs || []
    firstStageApproveType.value = res.data?.approveType || ''
    firstStageApproverCount.value = res.data?.approverCount || 0
    hasLoadedInitialApprovers.value = true
    // 加载子流程信息
    await loadSubWorkflows()
  } catch (e: any) {
    console.error('加载第一层审批人配置失败', e)
    ElMessage.error(e.message || '加载审批人配置失败')
  } finally {
    loadingAssets.value = false
  }
}

// 加载子流程信息
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

// 处理子流程审批人选择变化（或签时只允许选1个）
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

function getPreviewUrl(assetId: number) {
  return `/api/asset/preview/${assetId}`
}

function handleAssetSelect(selection: any[]) {
  localSelectedAssets.value = selection
  showAssetSelector.value = false
}

function removeAsset(index: number) {
  localSelectedAssets.value.splice(index, 1)
}

function handleFileChange(file: any) {
  uploadFile.value = file.raw
}

function handleFileRemove() {
  uploadFile.value = null
  form.attachmentPath = ''
}

function handleBackToAssets() {
  emit('backToAssets')
}

// 保存草稿（不校验任何内容，草稿阶段不限制）
async function handleSaveDraft() {
  if (localSelectedAssets.value.length === 0) {
    ElMessage.warning('请先选择要删除的素材')
    return
  }

  saving.value = true
  try {
    const data = {
      title: form.title,
      deleteReason: form.deleteReason,
      attachmentPath: form.attachmentPath,
      assetIds: localSelectedAssetIds.value
    }

    console.log('保存草稿 - 请求数据:', data)

    if (isEdit.value && currentId.value) {
      const res = await updateDeletionApplication(currentId.value, data)
      console.log('更新草稿 - 响应:', res)
      ElMessage.success('草稿已更新')
    } else {
      const res = await createDeletionApplication(data)
      console.log('创建草稿 - 响应:', res)
      console.log('创建草稿 - 返回ID:', res.data?.id)
      currentId.value = res.data?.id
      isEdit.value = true
      if (currentId.value) {
        ElMessage.success('草稿已保存，ID: ' + currentId.value)
      } else {
        ElMessage.error('保存失败：未返回申请ID')
      }
    }
  } catch (e: any) {
    console.error('保存草稿失败', e)
    console.error('错误详情:', e.response?.data)
    const errorMsg = e.response?.data?.message || e.message || '保存失败'
    ElMessage.error(errorMsg)
  } finally {
    saving.value = false
  }
}

// 打开提交审批对话框
async function handleSubmitDialog() {
  if (localSelectedAssets.value.length === 0) {
    ElMessage.warning('请先选择要删除的素材')
    return
  }

  // 提交审批时校验标题和删除原因
  if (!form.title || form.title.trim() === '') {
    ElMessage.warning('请输入申请标题')
    return
  }

  if (!form.deleteReason || form.deleteReason.trim() === '') {
    ElMessage.warning('请输入删除原因')
    return
  }

  // 先保存草稿
  if (!isEdit.value || !currentId.value) {
    await handleSaveDraft()
  }

  if (!currentId.value) {
    ElMessage.error('请先保存草稿')
    return
  }

  // 加载审批流程
  await loadWorkflows().then(() => {
    showSubmitDialog.value = true
    selectedFirstStageApprovers.value = {}
    loadFirstStageApprovers()
  })
}

// 提交审批
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

  // 检查是否需要选择子流程审批人（根据子流程自己的或签/会签类型）
  for (const subWorkflow of subWorkflows.value) {
    if (subWorkflow.approverCount > 0) {
      const selectedCount = Object.values(subWorkflow.selectedApprovers).filter(v => v !== null && v !== undefined).length
      if (subWorkflow.approveType === 'OR') {
        // 子流程或签：至少选1个
        if (selectedCount < 1) {
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

  if (!currentId.value) {
    ElMessage.error('请先保存草稿')
    return
  }

  submitting.value = true
  try {
    // 提交审批，获取实例ID
    const submitRes = await submitDeletionApplication(currentId.value, boundWorkflow.value.id)
    const instanceId = submitRes.data

    if (!instanceId) {
      ElMessage.error('提交失败：未返回审批实例ID')
      return
    }

    // 如果有子流程或主流程审批人，使用新API
    if ((firstStageApproverCount.value > 0 || subWorkflows.value.length > 0) && instanceId) {
      // 构建主流程审批人选择
      const mainApproverIds: number[] = []
      for (const configId in selectedFirstStageApprovers.value) {
        const approverId = selectedFirstStageApprovers.value[configId]
        if (approverId !== null && approverId !== undefined) {
          mainApproverIds.push(approverId)
        }
      }

      // 构建子流程审批人选择映射（从 configId -> userId 转换为数组）
      const subWorkflowApproverIds: Record<number, number[]> = {}
      for (const subWorkflow of subWorkflows.value) {
        const selectedIds = Object.values(subWorkflow.selectedApprovers).filter(v => v !== null && v !== undefined) as number[]
        if (selectedIds.length > 0) {
          subWorkflowApproverIds[subWorkflow.id] = selectedIds
        }
      }

      await selectFirstStageApproversWithSubWorkflows({
        instanceId: instanceId,
        approverIds: mainApproverIds,
        subWorkflowApproverIds: subWorkflowApproverIds
      })
    }

    ElMessage.success('提交成功')
    showSubmitDialog.value = false
    // 提交成功后跳转到素材列表页面
    router.push('/asset')
  } catch (e: any) {
    console.error('提交失败', e)
    const errorMsg = e.response?.data?.message || e.message || '提交失败'
    ElMessage.error(errorMsg)
  } finally {
    submitting.value = false
  }
}

function handleCreateNew() {
  submitted.value = false
  isEdit.value = false
  currentId.value = null
  localSelectedAssets.value = []
  formRef.value?.resetFields()
  uploadFile.value = null
  form.attachmentPath = ''
  uploadRef.value?.clearFiles()
}

function goToList() {
  router.push('/task/my-initiated')
}

function goToDrafts() {
  router.push('/task/draft-box')
}

function getTypeText(type: string) {
  const typeMap: Record<string, string> = {
    IMAGE: '图片',
    VIDEO: '视频',
    DOCUMENT: '文档'
  }
  return typeMap[type] || type
}

function getTypeColor(type: string) {
  const colorMap: Record<string, string> = {
    IMAGE: 'success',
    VIDEO: 'warning',
    DOCUMENT: 'info'
  }
  return colorMap[type] || ''
}

// 加载草稿数据
async function loadData() {
  if (props.applicationId) {
    isEdit.value = true
    currentId.value = props.applicationId
    loadingAssets.value = true
    try {
      const res = await getDeletionApplicationById(props.applicationId)
      const data = res.data
      form.title = data.title || ''
      form.deleteReason = data.deleteReason || ''
      form.attachmentPath = data.attachmentPath || ''

      // 加载素材列表 - 后端返回的 assets 字段已经包含 id, name, type 兼容字段
      if (data.assets && data.assets.length > 0) {
        localSelectedAssets.value = data.assets.map((asset: any) => ({
          id: asset.id || asset.assetId,
          name: asset.name || asset.assetName,
          type: asset.type || asset.assetType,
          status: 'APPROVED'
        }))
      }
    } catch (e) {
      ElMessage.error('加载草稿失败')
    } finally {
      loadingAssets.value = false
    }
  }
}

onMounted(async () => {
  await loadCurrentUser()
  await loadData()
})
</script>

<style scoped>
.deletion-apply-page {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style>
