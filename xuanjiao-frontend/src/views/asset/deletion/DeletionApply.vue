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
        <el-form-item v-if="boundWorkflow && hasLoadedInitialApprovers && firstStageApproverConfigs.length === 0">
          <div style="color: #E6A23C; font-size: 13px">
            该流程无需手动选择审批人，系统将自动分配
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
  getMyDeletionApplications
} from '@/api/assetDeletion'
import { getWorkflowList, getFirstStageApprovers, selectFirstStageApproversWithSubWorkflows } from '@/api/workflow'
import { getCurrentUser } from '@/api/user'

interface Props {
  selectedAssetIds?: number[]
  selectedAssets?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  selectedAssetIds: () => [],
  selectedAssets: () => []
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
  } catch (e: any) {
    console.error('加载第一层审批人配置失败', e)
    ElMessage.error(e.message || '加载审批人配置失败')
  } finally {
    loadingAssets.value = false
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

// 保存草稿
async function handleSaveDraft() {
  if (localSelectedAssets.value.length === 0) {
    ElMessage.warning('请先选择要删除的素材')
    return
  }

  await formRef.value?.validate()

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

    // 如果需要选择第一层审批人，调用选择接口
    if (firstStageApproverCount.value > 0) {
      const approverIds: number[] = []
      // 从 selectedFirstStageApprovers 中提取选中的审批人ID
      for (const configId in selectedFirstStageApprovers.value) {
        const approverId = selectedFirstStageApprovers.value[configId]
        if (approverId !== null && approverId !== undefined) {
          approverIds.push(approverId)
        }
      }

      if (approverIds.length > 0) {
        await selectFirstStageApproversWithSubWorkflows({
          instanceId: instanceId,
          approverIds: approverIds,
          subWorkflowApproverIds: {}
        })
      }
    }

    ElMessage.success('提交成功')
    showSubmitDialog.value = false
    submitted.value = true
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

onMounted(async () => {
  await loadCurrentUser()
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
