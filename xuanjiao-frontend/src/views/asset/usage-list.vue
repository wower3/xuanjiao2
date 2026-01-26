<template>
  <div class="usage-list-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>{{ isDraftMode ? '草稿箱' : '我的使用申请' }}</span>
          <el-button type="primary" @click="createNew">新建申请</el-button>
        </div>
      </template>

      <el-table :data="applications" v-loading="loading">
        <el-table-column prop="title" label="申请标题" min-width="200" />
        <el-table-column label="素材数量" width="100">
          <template #default="{ row }">
            {{ row.assets?.length || 0 }} 个
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'DRAFT'" type="info">草稿</el-tag>
            <el-tag v-else-if="row.status === 'PENDING'" type="warning">待审批</el-tag>
            <el-tag v-else-if="row.status === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else-if="row.status === 'REJECTED'" type="danger">已驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button
              v-if="row.status === 'DRAFT' || row.status === 'REJECTED'"
              link
              type="primary"
              @click="editApplication(row)"
            >
              {{ row.status === 'DRAFT' ? '编辑' : '重新提交' }}
            </el-button>
            <el-button v-if="row.status === 'DRAFT'" link type="danger" @click="deleteApplication(row)">
              删除
            </el-button>
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

    <!-- 查看详情对话框 -->
    <el-dialog v-model="showDetail" title="申请详情" width="900px">
      <div v-if="currentApplication">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请标题">{{ currentApplication.title }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentApplication.status === 'DRAFT'" type="info">草稿</el-tag>
            <el-tag v-else-if="currentApplication.status === 'PENDING'" type="warning">待审批</el-tag>
            <el-tag v-else-if="currentApplication.status === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else-if="currentApplication.status === 'REJECTED'" type="danger">已驳回</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentApplication.userName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentApplication.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="asset-section">
          <div class="asset-header">素材列表 ({{ currentApplication.assets?.length || 0 }})</div>
          <el-table :data="currentApplication.assets" size="small">
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
            <el-table-column prop="name" label="素材名称" min-width="150" />
            <el-table-column prop="type" label="类型" width="80" />
            <el-table-column label="使用配置" min-width="200">
              <template #default="{ row }">
                <div class="config-list">
                  <div v-if="row.usageDescription" class="config-item">
                    <span class="config-label">说明:</span> {{ row.usageDescription }}
                  </div>
                  <div v-if="row.usagePublishChannel" class="config-item">
                    <span class="config-label">渠道:</span> {{ row.usagePublishChannel }}
                  </div>
                  <div v-if="row.usageIsSecondaryCreation === 1" class="config-item">
                    <el-tag size="small" type="warning">二次创作</el-tag>
                  </div>
                  <div v-if="row.usageAttachmentPath" class="config-item">
                    <el-link :href="row.usageAttachmentPath" target="_blank" type="primary">查看附件</el-link>
                  </div>
                  <div v-if="!row.usageDescription && !row.usagePublishChannel && row.usageIsSecondaryCreation !== 1 && !row.usageAttachmentPath" class="config-item unconfigured">
                    未配置
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button link type="primary" @click="previewAsset(row)">预览</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoCamera, Document } from '@element-plus/icons-vue'
import { getMyUsageApplies, deleteUsageApply, getUsageApplyById } from '@/api/usageApply'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const applications = ref<any[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const showDetail = ref(false)
const currentApplication = ref<any>(null)

const isDraftMode = computed(() => route.query.status === 'DRAFT')

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
    const res = await getMyUsageApplies(params)
    let list = res.data.list || []

    // 如果是草稿模式，只显示草稿
    if (isDraftMode.value) {
      list = list.filter((item: any) => item.status === 'DRAFT')
    }

    applications.value = list
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function createNew() {
  router.push('/asset/usage-apply')
}

async function viewDetail(row: any) {
  try {
    const res = await getUsageApplyById(row.id)
    currentApplication.value = res.data
    showDetail.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

function editApplication(row: any) {
  router.push(`/asset/usage-apply?id=${row.id}`)
}

async function deleteApplication(row: any) {
  try {
    await ElMessageBox.confirm('确定要删除此申请吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteUsageApply(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

function getPreviewUrl(id: number) {
  return `/api/asset/preview/${id}`
}

function previewAsset(asset: any) {
  window.open(getPreviewUrl(asset.id), '_blank')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.usage-list-page {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.asset-section {
  margin-top: 20px;
}

.asset-header {
  font-weight: bold;
  margin-bottom: 10px;
}

.config-list {
  font-size: 12px;
  line-height: 1.8;
}

.config-item {
  margin-bottom: 2px;
}

.config-label {
  color: #909399;
  margin-right: 4px;
}

.config-item.unconfigured {
  color: #C0C4CC;
}
</style>
