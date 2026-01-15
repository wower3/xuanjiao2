<template>
  <div class="asset-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>素材管理</span>
          <div class="header-actions">
            <el-button @click="previewMode = previewMode === 'image' ? 'list' : 'image'">
              <el-icon><View v-if="previewMode === 'image'" /><List v-else /></el-icon>
              {{ previewMode === 'image' ? '列表模式' : '预览模式' }}
            </el-button>
          </div>
        </div>
      </template>
      <el-form :inline="true" :model="query">
        <el-form-item label="名称">
          <el-input v-model="query.name" placeholder="素材名称" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="全部" clearable>
            <el-option label="视频" value="VIDEO" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="文档" value="DOCUMENT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table
        :data="list"
        v-loading="loading"
        @row-click="previewMode === 'list' ? handleRowClick : undefined"
        :class="{ 'clickable-rows': previewMode === 'list' }"
      >
        <el-table-column v-if="previewMode === 'image'" label="预览" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.type === 'IMAGE'"
              :src="`/api/asset/preview/${row.id}`"
              :preview-src-list="[`/api/asset/preview/${row.id}`]"
              :preview-teleported="true"
              :z-index="99999"
              fit="cover"
              style="width:80px;height:60px"
            />
            <el-icon v-else-if="row.type === 'VIDEO'" :size="40"><VideoCamera /></el-icon>
            <el-icon v-else :size="40"><Document /></el-icon>
          </template>
        </el-table-column>
        <el-table-column v-else label="预览" width="80">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="preview(row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDownload(row)">下载</el-button>
            <el-button link type="primary" @click="showApplyDialog(row)">申请使用</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="showPreview" title="素材预览" width="800px">
      <div class="preview-content">
        <img v-if="previewAsset?.type === 'IMAGE'" :src="previewUrl" style="max-width:100%" />
        <video v-else-if="previewAsset?.type === 'VIDEO'" :src="previewUrl" controls style="max-width:100%" />
        <iframe v-else :src="previewUrl" style="width:100%;height:500px" />
      </div>
    </el-dialog>

    <el-dialog v-model="showApply" title="申请使用素材" width="600px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="素材名称">
          <el-input :value="currentAsset?.name" disabled />
        </el-form-item>
        <el-form-item label="使用用途">
          <el-input v-model="applyForm.purpose" type="textarea" :rows="3" placeholder="请说明使用用途" />
        </el-form-item>
        <el-form-item label="使用范围">
          <el-select v-model="applyForm.scope" placeholder="请选择使用范围">
            <el-option label="仅个人使用" value="PERSONAL" />
            <el-option label="部门内部使用" value="DEPARTMENT" />
            <el-option label="公司内部使用" value="COMPANY" />
            <el-option label="对外发布" value="PUBLIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批流程">
          <div v-if="boundWorkflow">
            <el-tag type="success">{{ boundWorkflow.name }}</el-tag>
            <div style="color: #909399; font-size: 12px; margin-top: 5px">
              根据您的角色自动匹配的审批流程
            </div>
          </div>
          <div v-else>
            <span style="color: #F56C6C">您的角色未绑定素材使用审批流程，无法提交申请</span>
          </div>
        </el-form-item>

        <!-- 第一层审批人选择 -->
        <el-form-item label="第一层审批人" v-if="firstStageApprovers.length > 0 || (hasLoadedInitialApprovers && approverKeyword)">
          <div style="width: 100%">
            <!-- 搜索框 -->
            <el-input
              v-model="approverKeyword"
              placeholder="搜索审批人（用户名或姓名）"
              clearable
              @clear="loadFirstStageApprovers"
              @keyup.enter="loadFirstStageApprovers"
              style="margin-bottom: 10px"
            >
              <template #append>
                <el-button :icon="Search" @click="loadFirstStageApprovers" />
              </template>
            </el-input>

            <!-- 审批人列表 -->
            <div v-loading="loadingApprovers" style="max-height: 250px; overflow-y: auto; border: 1px solid #DCDFE6; border-radius: 4px; padding: 8px">
              <el-checkbox-group v-model="selectedApproverIds">
                <div v-for="approver in firstStageApprovers" :key="approver.id" style="padding: 8px; border-bottom: 1px solid #EBEEF5">
                  <el-checkbox :label="approver.id">
                    <div style="display: flex; align-items: center; justify-content: space-between">
                      <div>
                        <span style="font-weight: 500">{{ approver.realName || approver.username }}</span>
                        <span v-if="approver.realName && approver.username" style="color: #909399; margin-left: 5px">({{ approver.username }})</span>
                      </div>
                      <div style="font-size: 12px; color: #909399">
                        {{ approver.deptName }}
                        <span v-if="approver.roleName" style="margin-left: 5px">{{ approver.roleName }}</span>
                      </div>
                    </div>
                  </el-checkbox>
                </div>
              </el-checkbox-group>
              <!-- 搜索无结果 -->
              <div v-if="firstStageApprovers.length === 0 && !loadingApprovers && approverKeyword" style="text-align: center; padding: 20px; color: #909399">
                未找到匹配的审批人
              </div>
              <!-- 初始加载无结果 -->
              <div v-else-if="firstStageApprovers.length === 0 && !loadingApprovers && !approverKeyword" style="text-align: center; padding: 20px; color: #909399">
                暂无可选审批人
              </div>
            </div>

            <!-- 已选择提示 -->
            <div v-if="selectedApproverIds.length > 0" style="margin-top: 8px; color: #67C23A; font-size: 12px">
              已选择 {{ selectedApproverIds.length }} 位审批人
            </div>
          </div>
        </el-form-item>

        <!-- 无需选择审批人的提示 -->
        <el-form-item v-else-if="boundWorkflow && hasLoadedInitialApprovers && firstStageApprovers.length === 0 && !approverKeyword">
          <div style="color: #E6A23C; font-size: 13px">
            <el-icon><WarningFilled /></el-icon>
            该流程第一层为子流程阶段，将由子流程自动选择审批人，无需手动选择
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApply = false">取消</el-button>
        <el-button type="primary" @click="handleApply" :loading="applying" :disabled="!boundWorkflow">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAssetList, deleteAsset } from '@/api/asset'
import { getWorkflowByRole, getFirstStageApprovers, selectFirstStageApprovers } from '@/api/workflow'
import { applyUsage, downloadAsset } from '@/api/usageApply'
import { ElMessageBox, ElMessage } from 'element-plus'
import { View, List, VideoCamera, Document, Search, WarningFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const loading = ref(false)
const showApply = ref(false)
const applying = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ name: '', type: '', pageNum: 1, pageSize: 10 })
const showPreview = ref(false)
const previewAsset = ref<any>(null)
const previewUrl = ref('')
const previewMode = ref<'image' | 'list'>('image')
const boundWorkflow = ref<any>(null) // 角色绑定的审批流程
const currentAsset = ref<any>(null)
const applyForm = reactive({ purpose: '', scope: '' })
const userStore = useUserStore()

// 第一层审批人相关
const firstStageApprovers = ref<any[]>([])
const selectedApproverIds = ref<number[]>([])
const approverKeyword = ref('')
const loadingApprovers = ref(false)
const hasLoadedInitialApprovers = ref(false) // 标记是否已加载过初始审批人列表

async function loadData() {
  loading.value = true
  try {
    const res = await getAssetList(query)
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadWorkflows() {
  try {
    // 检查当前用户角色是否绑定了素材使用审批流程
    if (userStore.userInfo?.roleId) {
      const res = await getWorkflowByRole({
        roleId: userStore.userInfo.roleId,
        workflowType: 'ASSET_USAGE'
      })
      if (res.data) {
        boundWorkflow.value = res.data
      }
    }
  } catch (e: any) {
    console.error('加载审批流程失败', e)
  }
}

async function loadFirstStageApprovers() {
  if (!boundWorkflow.value || !userStore.userInfo?.id) return

  loadingApprovers.value = true
  try {
    const res = await getFirstStageApprovers({
      workflowId: boundWorkflow.value.id,
      applicantId: userStore.userInfo.id,
      keyword: approverKeyword.value
    })
    firstStageApprovers.value = res.data || []
    // 标记已加载过初始列表（无关键词时的加载）
    if (!approverKeyword.value) {
      hasLoadedInitialApprovers.value = true
    }
  } catch (e: any) {
    console.error('加载第一层审批人失败', e)
    ElMessage.error(e.message || '加载第一层审批人失败')
  } finally {
    loadingApprovers.value = false
  }
}

function handleRowClick(row: any) {
  preview(row)
}

function preview(row: any) {
  previewAsset.value = row
  previewUrl.value = `/api/asset/preview/${row.id}`
  showPreview.value = true
}

async function handleDownload(row: any) {
  try {
    const blob = await downloadAsset(row.id)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = row.name
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (e: any) {
    if (e.response?.status === 403) {
      ElMessage.error('您没有下载此素材的权限，请先申请使用')
    } else {
      ElMessage.error('下载失败')
    }
  }
}

async function showApplyDialog(row: any) {
  currentAsset.value = row
  applyForm.purpose = ''
  applyForm.scope = ''
  // 先尝试加载绑定的流程，然后加载第一层审批人
  await loadWorkflows()
  // 重置状态
  approverKeyword.value = ''
  selectedApproverIds.value = []
  hasLoadedInitialApprovers.value = false
  loadFirstStageApprovers()
  showApply.value = true
}

async function handleApply() {
  if (!applyForm.purpose) {
    ElMessage.warning('请填写使用用途')
    return
  }
  if (!applyForm.scope) {
    ElMessage.warning('请选择使用范围')
    return
  }
  // 检查是否有绑定的审批流程
  if (!boundWorkflow.value) {
    ElMessage.warning('您的角色未绑定素材使用审批流程，无法提交申请')
    return
  }
  // 检查是否需要选择第一层审批人
  if (firstStageApprovers.value.length > 0 && selectedApproverIds.value.length === 0) {
    ElMessage.warning('请选择第一层审批人')
    return
  }
  applying.value = true
  try {
    const applyRes = await applyUsage({
      assetId: currentAsset.value.id,
      purpose: applyForm.purpose,
      scope: applyForm.scope,
      workflowId: boundWorkflow.value.id
    })
    // 如果有第一层审批人需要选择，先选择审批人
    if (firstStageApprovers.value.length > 0 && applyRes.data?.instanceId) {
      await selectFirstStageApprovers({
        instanceId: applyRes.data.instanceId,
        approverIds: selectedApproverIds.value
      })
    }
    ElMessage.success('申请已提交，请等待审批')
    showApply.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '申请失败')
  } finally {
    applying.value = false
  }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm('确定删除该素材?', '提示')
  await deleteAsset(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
  // 不在这里加载审批流程，只在用户点击"申请使用"时才加载
})
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
.clickable-rows :deep(.el-table__body tr) { cursor: pointer; }
.clickable-rows :deep(.el-table__body tr:hover) { background-color: var(--el-fill-color-light); }
</style>
