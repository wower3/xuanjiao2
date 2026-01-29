<template>
  <div class="draft-box-page">
    <el-card>
      <template #header>
        <span>草稿箱</span>
      </template>

      <!-- 筛选条件 -->
      <div style="margin-bottom: 15px; display: flex; align-items: center; gap: 10px;">
        <span style="color: #606266; font-size: 14px;">筛选：</span>
        <el-select
          v-model="filterForm.draftType"
          placeholder="全部类型"
          clearable
          style="width: 150px"
          @change="handleFilterChange"
        >
          <el-option label="素材录入" value="MATERIAL_ENTRY" />
          <el-option label="使用申请" value="ASSET_USAGE" />
        </el-select>
        <el-input
          v-model="filterForm.title"
          placeholder="请输入标题搜索"
          clearable
          style="width: 200px"
          @clear="handleFilterChange"
          @keyup.enter="handleFilterChange"
        >
          <template #append>
            <el-button @click="handleFilterChange">搜索</el-button>
          </template>
        </el-input>
      </div>

      <el-table :data="drafts" v-loading="loading">
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'MATERIAL_ENTRY'" type="primary">素材录入</el-tag>
            <el-tag v-else type="success">使用申请</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="事项标题" min-width="200">
          <template #default="{ row }">
            {{ row.data.title || row.data.businessName }}
          </template>
        </el-table-column>
        <el-table-column label="维护人/申请人" width="120">
          <template #default="{ row }">
            {{ row.data.maintainerName || row.data.userName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="归属部门" width="120">
          <template #default="{ row }">
            {{ row.data.deptName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="文件数量" width="100">
          <template #default="{ row }">
            {{ row.data.assets?.length || 0 }} 个
          </template>
        </el-table-column>
        <el-table-column prop="data.createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
            <el-button link type="primary" @click="continueEdit(row)">继续编辑</el-button>
            <el-button link type="danger" @click="deleteDraft(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        @change="loadDrafts"
      />
    </el-card>

    <!-- 查看详情对话框 - 只读 -->
    <el-dialog v-model="showDetail" title="申请单详情" width="900px">
      <div v-if="currentDraft">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="类型" :span="2">
            <el-tag v-if="currentDraft.type === 'MATERIAL_ENTRY'" type="primary">素材录入</el-tag>
            <el-tag v-else type="success">使用申请</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="事项标题" :span="2">{{ currentDraft.data.title || currentDraft.data.businessName }}</el-descriptions-item>
          <el-descriptions-item label="维护人/申请人">
            {{ currentDraft.data.maintainerName || currentDraft.data.userName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="归属部门">
            {{ currentDraft.data.deptName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentDraft.data.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="file-section">
          <div class="file-header">
            <span>素材文件 ({{ currentDraft.data.assets?.length || 0 }})</span>
          </div>
          <el-table :data="currentDraft.data.assets" size="small" v-if="currentDraft.type === 'MATERIAL_ENTRY'">
            <el-table-column prop="name" label="文件名称" />
            <el-table-column prop="type" label="类型" width="80" />
            <el-table-column label="标签" width="150">
              <template #default="{ row }">
                <el-tag
                  v-for="tag in row.tags"
                  :key="tag.id"
                  size="small"
                  style="margin-right: 5px"
                >
                  {{ tag.name }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" show-overflow-tooltip />
          </el-table>
          <el-table :data="currentDraft.data.assets" size="small" v-else>
            <el-table-column prop="assetName" label="文件名称" />
            <el-table-column prop="assetType" label="类型" width="80" />
            <el-table-column prop="usageDescription" label="使用说明" show-overflow-tooltip />
            <el-table-column prop="usagePublishChannel" label="发布渠道" width="120" />
            <el-table-column label="二次创作" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.usageIsSecondaryCreation === 1" type="success" size="small">是</el-tag>
                <el-tag v-else type="info" size="small">否</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDrafts } from '@/api/task'
import { deleteMaterialApplication } from '@/api/materialApplication'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const drafts = ref<any[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const filterForm = reactive({
  draftType: '',
  title: ''
})
const showDetail = ref(false)
const currentDraft = ref<any>(null)

async function loadDrafts() {
  loading.value = true
  try {
    const params: any = {
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
    if (filterForm.draftType) {
      params.draftType = filterForm.draftType
    }
    if (filterForm.title) {
      params.title = filterForm.title
    }
    const res = await getDrafts(params)
    drafts.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  query.pageNum = 1
  loadDrafts()
}

function viewDetail(row: any) {
  currentDraft.value = row
  showDetail.value = true
}

function continueEdit(row: any) {
  if (row.type === 'MATERIAL_ENTRY') {
    // 跳转到素材录入页面，带上工单ID
    router.push(`/asset/material-entry?id=${row.data.id}`)
  } else if (row.type === 'ASSET_USAGE') {
    // 跳转到使用申请页面，带上工单ID
    router.push(`/asset/usage-apply?id=${row.data.id}`)
  }
}

async function deleteDraft(row: any) {
  await ElMessageBox.confirm('确定删除该草稿?', '提示')
  if (row.type === 'MATERIAL_ENTRY') {
    await deleteMaterialApplication(row.data.id)
    ElMessage.success('删除成功')
  } else {
    // TODO: 使用申请删除
    ElMessage.info('使用申请删除功能待实现')
  }
  loadDrafts()
}

onMounted(() => {
  loadDrafts()
})
</script>

<style scoped>
.file-section {
  margin-top: 20px;
}
.file-header {
  font-weight: bold;
  margin-bottom: 10px;
}
</style>
