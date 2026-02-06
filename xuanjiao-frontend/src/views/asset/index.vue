<template>
  <div class="asset-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>素材管理</span>
          <div class="header-actions">
            <!-- 管理员功能：执行清理定时任务 -->
            <el-button v-if="isAdmin" type="warning" @click="handleTriggerCleanup" :loading="cleanupLoading">
              执行清理
            </el-button>
            <el-button @click="previewMode = previewMode === 'image' ? 'list' : 'image'">
              <el-icon><View v-if="previewMode === 'image'" /><List v-else /></el-icon>
              {{ previewMode === 'image' ? '列表模式' : '预览模式' }}
            </el-button>
          </div>
        </div>
      </template>
      <el-form :inline="true" :model="query">
        <el-form-item label="名称">
          <el-input v-model="query.name" placeholder="素材名称" clearable @change="handleQueryChange" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="全部" clearable @change="handleQueryChange">
            <el-option label="视频" value="VIDEO" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="文档" value="DOCUMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable @change="handleQueryChange">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已删除" value="DELETED" />
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
            <el-button
              v-else-if="row.type === 'VIDEO'"
              link
              type="primary"
              @click.stop="preview(row)"
              class="video-thumbnail"
            >
              <el-image
                v-if="row.thumbnailPath"
                :src="`/api/asset/thumbnail/${row.id}`"
                fit="cover"
                style="width:80px;height:60px"
              >
                <template #error>
                  <div class="video-icon-wrapper">
                    <el-icon :size="30"><VideoCamera /></el-icon>
                    <el-icon class="play-icon"><VideoPlay /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="video-icon-wrapper">
                <el-icon :size="40"><VideoCamera /></el-icon>
                <el-icon class="play-icon"><VideoPlay /></el-icon>
              </div>
            </el-button>
            <el-button
              v-else
              link
              type="primary"
              @click.stop="preview(row)"
            >
              <el-icon :size="40"><Document /></el-icon>
            </el-button>
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
            <el-button
              v-if="row.status === 'APPROVED'"
              link
              type="primary"
              :disabled="!row.canDownload"
              @click="handleDownload(row)">下载</el-button>
            <el-button
              v-if="row.status === 'APPROVED'"
              link
              type="primary"
              :disabled="row.canDownload"
              @click="showApplyDialog(row)">申请使用</el-button>
            <el-button v-if="previewMode === 'image' && row.status === 'APPROVED'" link type="success" @click="showUsageDetails(row)">使用详情</el-button>
            <!-- 管理员功能 -->
            <template v-if="isAdmin">
              <el-button v-if="row.status === 'DELETED'" link type="warning" @click="handleAdjustDeleteTime(row)">模拟时间</el-button>
              <el-button v-if="row.status === 'APPROVED'" link type="danger" @click="handleAdminDelete(row)">彻底删除</el-button>
            </template>
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
          <img v-if="previewAsset.type === 'IMAGE'" :src="previewUrl" style="max-width:100%;max-height:500px" />
          <video
            v-else-if="previewAsset.type === 'VIDEO'"
            :src="previewUrl"
            controls
            style="max-width:100%;max-height:500px"
            crossorigin="anonymous"
            @error="handleVideoError"
          >
            您的浏览器不支持视频播放
          </video>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getAssetList, adminDeleteAsset, adjustAssetDeleteTime, triggerCleanupTask } from '@/api/asset'
import { downloadAsset } from '@/api/usageApply'
import { getAssetUsageLogs } from '@/api/usageLog'
import { ElMessageBox, ElMessage } from 'element-plus'
import { View, List, VideoCamera, Document, VideoPlay } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const cleanupLoading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ name: '', type: '', status: '', pageNum: 1, pageSize: 10 })
const showPreview = ref(false)
const previewAsset = ref<any>(null)
const previewUrl = ref('')
const previewMode = ref<'image' | 'list'>('image')

// 判断是否是管理员
const isAdmin = computed(() => {
  return userStore.userInfo?.roleId === 1
})

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

// 从URL初始化筛选条件
function initQueryFromUrl() {
  const routeQuery = router.currentRoute.value.query
  if (routeQuery.name) query.name = routeQuery.name as string
  if (routeQuery.type) query.type = routeQuery.type as string
  if (routeQuery.status) query.status = routeQuery.status as string
  if (routeQuery.pageNum) query.pageNum = parseInt(routeQuery.pageNum as string)
  if (routeQuery.pageSize) query.pageSize = parseInt(routeQuery.pageSize as string)
}

// 更新URL中的筛选条件
function updateUrlQuery() {
  router.replace({
    query: {
      ...router.currentRoute.value.query,
      name: query.name || undefined,
      type: query.type || undefined,
      status: query.status || undefined,
      pageNum: query.pageNum.toString(),
      pageSize: query.pageSize.toString()
    }
  })
}

// 筛选条件变化时处理
function handleQueryChange() {
  query.pageNum = 1
  updateUrlQuery()
  loadData()
}

async function loadData() {
  loading.value = true
  updateUrlQuery()
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
  previewUrl.value = `/api/asset/preview/${row.id}?t=${Date.now()}`
  showPreview.value = true
  // 重置使用记录分页并加载
  logsQuery.pageNum = 1
  await loadUsageLogs()
}

function handleVideoError(e: any) {
  console.error('视频加载失败:', e)
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
  } catch (e) {
    ElMessage.error('下载失败')
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

// 管理员彻底删除素材
async function handleAdminDelete(row: any) {
  try {
    await ElMessageBox.prompt('请输入删除理由（必填）', '彻底删除素材', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '删除理由不能为空'
    })
    const reason = (document.querySelector('.el-message-box__input input') as HTMLInputElement)?.value || ''

    await ElMessageBox.confirm('彻底删除后素材将无法恢复，确定要删除吗？', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await adminDeleteAsset(row.id, reason)
    ElMessage.success('彻底删除成功')
    loadData()
  } catch (e) {
    // 用户取消操作
  }
}

// 管理员模拟删除时间（测试功能）
async function handleAdjustDeleteTime(row: any) {
  try {
    await ElMessageBox.confirm(
      '此操作将素材的删除审批时间改为一周前，用于测试定时清理功能。确定执行吗？',
      '模拟删除时间',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await adjustAssetDeleteTime(row.id)
    ElMessage.success('删除时间调整成功')
    loadData()
  } catch (e) {
    // 用户取消操作
  }
}

// 管理员手动触发定时任务（测试功能）
async function handleTriggerCleanup() {
  try {
    await ElMessageBox.confirm(
      '此操作将执行素材清理定时任务，删除所有状态为DELETED且删除审批时间超过一周的素材。确定执行吗？',
      '执行清理任务',
      {
        confirmButtonText: '确定执行',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    cleanupLoading.value = true
    const res = await triggerCleanupTask()
    const count = res.data || 0
    ElMessage.success(`清理任务执行完成，共清理 ${count} 个素材`)
    loadData()
  } catch (e) {
    // 用户取消操作
  } finally {
    cleanupLoading.value = false
  }
}

onMounted(() => {
  initQueryFromUrl()
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

.video-thumbnail {
  padding: 0;
  border: none;
  height: auto;
}

.video-thumbnail:hover {
  background: transparent;
}

.video-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 50px;
}

.play-icon {
  position: absolute;
  font-size: 24px;
  color: #409EFF;
  opacity: 0.8;
}
</style>
