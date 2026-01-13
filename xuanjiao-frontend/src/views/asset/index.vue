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

    <el-dialog v-model="showApply" title="申请使用素材" width="500px">
      <el-form :model="applyForm" label-width="80px">
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
          <el-select v-model="applyForm.workflowId" placeholder="不选择则直接通过" clearable>
            <el-option
              v-for="wf in usageWorkflowList"
              :key="wf.id"
              :label="wf.name"
              :value="wf.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApply = false">取消</el-button>
        <el-button type="primary" @click="handleApply" :loading="applying">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAssetList, deleteAsset } from '@/api/asset'
import { getWorkflowList } from '@/api/workflow'
import { applyUsage, downloadAsset } from '@/api/usageApply'
import { ElMessageBox, ElMessage } from 'element-plus'
import { View, List, VideoCamera, Document } from '@element-plus/icons-vue'

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
const workflowList = ref<any[]>([])
const usageWorkflowList = ref<any[]>([])
const currentAsset = ref<any>(null)
const applyForm = reactive({ purpose: '', scope: '', workflowId: null as number | null })

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
    const res = await getWorkflowList()
    const workflows = (res.data || []).filter((w: any) => w.status === 1)
    workflowList.value = workflows
    // 使用审批流程
    usageWorkflowList.value = workflows.filter((w: any) => w.type === 'ASSET_USAGE')
  } catch (e) {
    console.error('加载审批流程失败', e)
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

function showApplyDialog(row: any) {
  currentAsset.value = row
  applyForm.purpose = ''
  applyForm.scope = ''
  applyForm.workflowId = null
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
  applying.value = true
  try {
    await applyUsage({
      assetId: currentAsset.value.id,
      purpose: applyForm.purpose,
      scope: applyForm.scope,
      workflowId: applyForm.workflowId
    })
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
  loadWorkflows()
})
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
.clickable-rows :deep(.el-table__body tr) { cursor: pointer; }
.clickable-rows :deep(.el-table__body tr:hover) { background-color: var(--el-fill-color-light); }
</style>
