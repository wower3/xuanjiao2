<template>
  <div class="asset-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>素材管理</span>
          <el-button type="primary" @click="showUpload = true">上传素材</el-button>
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
      <el-table :data="list" v-loading="loading">
        <el-table-column label="预览" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.type === 'IMAGE'"
              :src="`/api/asset/preview/${row.id}`"
              :preview-src-list="[`/api/asset/preview/${row.id}`]"
              fit="cover"
              style="width:80px;height:60px"
            />
            <el-icon v-else-if="row.type === 'VIDEO'" :size="40"><VideoCamera /></el-icon>
            <el-icon v-else :size="40"><Document /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
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

    <el-dialog v-model="showUpload" title="上传素材" width="500px">
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="素材名称">
          <el-input v-model="uploadForm.name" />
        </el-form-item>
        <el-form-item label="素材类型">
          <el-select v-model="uploadForm.type">
            <el-option label="视频" value="VIDEO" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="文档" value="DOCUMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="审批流程">
          <el-select v-model="uploadForm.workflowId" placeholder="不选择则直接通过" clearable>
            <el-option
              v-for="wf in workflowList"
              :key="wf.id"
              :label="wf.name"
              :value="wf.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUpload = false">取消</el-button>
        <el-button type="primary" @click="handleUpload" :loading="uploading">上传</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPreview" title="素材预览" width="800px">
      <div class="preview-content">
        <img v-if="previewAsset?.type === 'IMAGE'" :src="previewUrl" style="max-width:100%" />
        <video v-else-if="previewAsset?.type === 'VIDEO'" :src="previewUrl" controls style="max-width:100%" />
        <iframe v-else :src="previewUrl" style="width:100%;height:500px" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAssetList, deleteAsset, uploadAsset } from '@/api/asset'
import { getWorkflowList } from '@/api/workflow'
import { ElMessageBox, ElMessage } from 'element-plus'

const loading = ref(false)
const showUpload = ref(false)
const uploading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ name: '', type: '', pageNum: 1, pageSize: 10 })
const uploadForm = reactive({ name: '', type: 'IMAGE', workflowId: null as number | null })
const uploadFile = ref<File | null>(null)
const showPreview = ref(false)
const previewAsset = ref<any>(null)
const previewUrl = ref('')
const workflowList = ref<any[]>([])

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
    // 只显示已启用的流程
    workflowList.value = (res.data || []).filter((w: any) => w.status === 1)
  } catch (e) {
    console.error('加载审批流程失败', e)
  }
}

function preview(row: any) {
  previewAsset.value = row
  previewUrl.value = `/api/asset/preview/${row.id}`
  showPreview.value = true
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm('确定删除该素材?', '提示')
  await deleteAsset(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function handleFileChange(file: any) {
  uploadFile.value = file.raw
  if (!uploadForm.name) {
    uploadForm.name = file.name
  }
}

async function handleUpload() {
  if (!uploadFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  uploading.value = true
  try {
    await uploadAsset(uploadFile.value, uploadForm)
    ElMessage.success('上传成功')
    showUpload.value = false
    uploadFile.value = null
    uploadForm.name = ''
    uploadForm.workflowId = null
    loadData()
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  loadData()
  loadWorkflows()
})
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
