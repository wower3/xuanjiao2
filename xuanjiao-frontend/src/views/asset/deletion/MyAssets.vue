<template>
  <div class="my-assets-page">
    <div class="search-bar">
      <el-input
        v-model="queryParams.name"
        placeholder="请输入素材名称"
        clearable
        style="width: 200px; margin-right: 10px"
        @clear="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select
        v-model="queryParams.type"
        placeholder="请选择类型"
        clearable
        style="width: 120px; margin-right: 10px"
        @change="handleSearch"
      >
        <el-option label="图片" value="IMAGE" />
        <el-option label="视频" value="VIDEO" />
        <el-option label="文档" value="DOCUMENT" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="search-bar-right">
        <span v-if="selectedAssets.length > 0" class="selection-count">
          已选择 {{ selectedAssets.length }} 个素材
        </span>
        <el-button
          type="danger"
          :disabled="selectedAssets.length === 0"
          @click="handleGoToDeletionTab"
        >
          申请删除
        </el-button>
      </div>
    </div>

    <el-table
      :data="assetList"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      style="margin-top: 20px"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="素材名称" min-width="200" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getTypeColor(row.type)" size="small">
            {{ getTypeText(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusColor(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="creatorName" label="创建人" width="120" />
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      @change="loadData"
      style="margin-top: 20px; justify-content: flex-end"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAssetList } from '@/api/asset'

const emit = defineEmits<{
  selectionChange: [data: { assetIds: number[]; assets: any[] }]
  goToDeletion: []
}>()

const loading = ref(false)
const assetList = ref<any[]>([])
const selectedAssets = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  name: '',
  type: '',
  status: 'APPROVED',
  pageNum: 1,
  pageSize: 10
})

async function loadData() {
  loading.value = true
  try {
    const res = await getAssetList(queryParams)
    assetList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载素材列表失败')
  } finally {
    loading.value = false
  }
}

function handleSelectionChange(selection: any[]) {
  selectedAssets.value = selection
  const assetIds = selection.map(item => item.id)
  emit('selectionChange', { assetIds, assets: selection })
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

function handleReset() {
  queryParams.name = ''
  queryParams.type = ''
  queryParams.status = 'APPROVED'
  queryParams.pageNum = 1
  loadData()
}

function handleGoToDeletionTab() {
  emit('selectionChange', {
    assetIds: selectedAssets.value.map(item => item.id),
    assets: selectedAssets.value
  })
  emit('goToDeletion')
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

function getStatusText(status: string) {
  const statusMap: Record<string, string> = {
    APPROVED: '已通过',
    PENDING: '审批中',
    REJECTED: '已驳回'
  }
  return statusMap[status] || status
}

function getStatusColor(status: string) {
  const colorMap: Record<string, string> = {
    APPROVED: 'success',
    PENDING: 'warning',
    REJECTED: 'danger'
  }
  return colorMap[status] || ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-assets-page {
  padding: 10px;
}

.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.search-bar-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.selection-count {
  color: #606266;
  font-size: 14px;
}
</style>
