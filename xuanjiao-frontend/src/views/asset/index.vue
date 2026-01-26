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
            <el-button v-if="previewMode === 'image' && row.status === 'APPROVED'" link type="success" @click="showUsageDetails(row)">使用详情</el-button>
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

    <el-dialog v-model="showPreview" title="素材预览" width="900px">
      <div v-if="previewAsset">
        <div class="preview-content">
          <img v-if="previewAsset.type === 'IMAGE'" :src="previewUrl" style="max-width:100%" />
          <video v-else-if="previewAsset.type === 'VIDEO'" :src="previewUrl" controls style="max-width:100%" />
          <iframe v-else :src="previewUrl" style="width:100%;height:500px" />
        </div>

        <div class="usage-logs-section">
          <div class="section-header">
            <span>使用记录 ({{ usageLogsTotal }})</span>
          </div>
          <el-table :data="usageLogs" size="small" v-loading="loadingLogs">
            <el-table-column prop="userName" label="使用人" width="100" />
            <el-table-column prop="deptName" label="部门" width="120" />
            <el-table-column prop="usageDescription" label="使用说明" min-width="150" show-overflow-tooltip />
            <el-table-column prop="usagePublishChannel" label="发布渠道" width="120" show-overflow-tooltip />
            <el-table-column prop="createTime" label="使用时间" width="180" />
          </el-table>
          <el-pagination
            v-if="usageLogsTotal > 0"
            v-model:current-page="logsQuery.pageNum"
            v-model:page-size="logsQuery.pageSize"
            :total="usageLogsTotal"
            @change="loadUsageLogs"
            small
            style="margin-top: 10px"
          />
          <el-empty v-if="!loadingLogs && usageLogsTotal === 0" description="暂无使用记录" :image-size="60" />
        </div>
      </div>
    </el-dialog>

    <!-- 使用详情弹窗 -->
    <el-dialog v-model="showUsageDetailsDialog" :title="`使用详情 - ${usageDetailsAsset?.name || ''}`" width="800px">
      <div v-if="usageDetailsAsset">
        <!-- 使用次数统计 -->
        <div class="usage-stats">
          <div class="stat-item">
            <span class="stat-label">素材名称</span>
            <span class="stat-value">{{ usageDetailsAsset.name }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">使用次数</span>
            <span class="stat-value stat-highlight">{{ usageDetailsTotal }} 次</span>
          </div>
        </div>

        <el-divider />

        <!-- 使用记录明细 -->
        <div class="usage-details-section">
          <div class="section-header">使用记录明细</div>
          <el-table :data="usageDetailsList" size="small" v-loading="loadingUsageDetails" max-height="400">
            <el-table-column prop="userName" label="使用人" width="100" />
            <el-table-column prop="deptName" label="所在机构" width="120" show-overflow-tooltip />
            <el-table-column prop="usageDescription" label="申请说明" min-width="150" show-overflow-tooltip />
            <el-table-column prop="usagePublishChannel" label="使用渠道" width="120" show-overflow-tooltip />
            <el-table-column prop="createTime" label="使用时间" width="180" />
          </el-table>
          <el-pagination
            v-if="usageDetailsTotal > 0"
            v-model:current-page="usageDetailsQuery.pageNum"
            v-model:page-size="usageDetailsQuery.pageSize"
            :total="usageDetailsTotal"
            @change="loadUsageDetails"
            small
            style="margin-top: 10px"
          />
          <el-empty v-if="!loadingUsageDetails && usageDetailsTotal === 0" description="暂无使用记录" :image-size="60" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAssetList, deleteAsset } from '@/api/asset'
import { downloadAsset, checkCanUseAsset } from '@/api/usageApply'
import { getAssetUsageLogs } from '@/api/usageLog'
import { ElMessageBox, ElMessage } from 'element-plus'
import { View, List, VideoCamera, Document } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ name: '', type: '', pageNum: 1, pageSize: 10 })
const showPreview = ref(false)
const previewAsset = ref<any>(null)
const previewUrl = ref('')
const previewMode = ref<'image' | 'list'>('image')

// 使用记录相关（预览弹窗用）
const usageLogs = ref<any[]>([])
const usageLogsTotal = ref(0)
const loadingLogs = ref(false)
const logsQuery = reactive({ pageNum: 1, pageSize: 5 })

// 使用详情弹窗相关
const showUsageDetailsDialog = ref(false)
const usageDetailsAsset = ref<any>(null)
const usageDetailsList = ref<any[]>([])
const usageDetailsTotal = ref(0)
const loadingUsageDetails = ref(false)
const usageDetailsQuery = reactive({ pageNum: 1, pageSize: 10 })

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

async function loadUsageLogs() {
  if (!previewAsset.value) return
  loadingLogs.value = true
  try {
    const res = await getAssetUsageLogs(previewAsset.value.id, {
      pageNum: logsQuery.pageNum,
      pageSize: logsQuery.pageSize
    })
    usageLogs.value = res.data.list || []
    usageLogsTotal.value = res.data.total || 0
  } catch (e) {
    console.error('加载使用记录失败:', e)
  } finally {
    loadingLogs.value = false
  }
}

async function loadUsageDetails() {
  if (!usageDetailsAsset.value) return
  loadingUsageDetails.value = true
  try {
    const res = await getAssetUsageLogs(usageDetailsAsset.value.id, {
      pageNum: usageDetailsQuery.pageNum,
      pageSize: usageDetailsQuery.pageSize
    })
    usageDetailsList.value = res.data.list || []
    usageDetailsTotal.value = res.data.total || 0
  } catch (e) {
    console.error('加载使用详情失败:', e)
    ElMessage.error('加载使用详情失败')
  } finally {
    loadingUsageDetails.value = false
  }
}

function handleRowClick(row: any) {
  preview(row)
}

async function preview(row: any) {
  previewAsset.value = row
  previewUrl.value = `/api/asset/preview/${row.id}`
  showPreview.value = true
  // 重置使用记录分页并加载
  logsQuery.pageNum = 1
  await loadUsageLogs()
}

async function handleDownload(row: any) {
  try {
    // 先检查是否有下载权限
    const checkRes = await checkCanUseAsset(row.id)
    if (!checkRes.data) {
      ElMessageBox.confirm(
        '您还没有该素材的使用权限，是否前往申请使用？',
        '需要权限',
        {
          confirmButtonText: '前往申请',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        showApplyDialog(row)
      }).catch(() => {
        // 用户取消
      })
      return
    }

    // 有权限则下载
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
  // 跳转到素材使用申请页面，带上素材ID
  router.push(`/asset/usage-apply?assetId=${row.id}`)
}

async function showUsageDetails(row: any) {
  usageDetailsAsset.value = row
  usageDetailsQuery.pageNum = 1
  showUsageDetailsDialog.value = true
  await loadUsageDetails()
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm('确定删除该素材?', '提示')
  await deleteAsset(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
.clickable-rows :deep(.el-table__body tr) { cursor: pointer; }
.clickable-rows :deep(.el-table__body tr:hover) { background-color: var(--el-fill-color-light); }

.preview-content {
  text-align: center;
  margin-bottom: 20px;
}

.usage-logs-section {
  border-top: 1px solid var(--el-border-color);
  padding-top: 20px;
}

.section-header {
  font-weight: bold;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 使用详情弹窗样式 */
.usage-stats {
  display: flex;
  gap: 40px;
  padding: 15px 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.stat-value {
  font-size: 18px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.stat-highlight {
  font-size: 24px;
  font-weight: bold;
  color: var(--el-color-primary);
}

.usage-details-section {
  margin-top: 15px;
}
</style>
