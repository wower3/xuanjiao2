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
          <el-option label="素材删除" value="ASSET_DELETION" />
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
            <el-tag v-else-if="row.type === 'ASSET_USAGE'" type="success">使用申请</el-tag>
            <el-tag v-else-if="row.type === 'ASSET_DELETION'" type="danger">素材删除</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="事项标题" min-width="200">
          <template #default="{ row }">
            {{ row.data.title || row.data.businessName }}
          </template>
        </el-table-column>
        <el-table-column label="维护人/申请人" width="120">
          <template #default="{ row }">
            {{ row.data.maintainerName || row.data.userName || row.data.applicantName || '-' }}
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
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDrafts } from '@/api/task'
import { deleteMaterialApplication } from '@/api/materialApplication'
import { deleteDeletionApplication } from '@/api/assetDeletion'
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

function continueEdit(row: any) {
  if (row.type === 'MATERIAL_ENTRY') {
    // 跳转到素材录入页面，带上工单ID
    router.push(`/asset/material-entry?id=${row.data.id}`)
  } else if (row.type === 'ASSET_USAGE') {
    // 跳转到使用申请页面，带上工单ID
    router.push(`/asset/usage-apply?id=${row.data.id}`)
  } else if (row.type === 'ASSET_DELETION') {
    // 跳转到素材删除页面，带上工单ID
    router.push(`/asset/deletion?id=${row.data.id}`)
  }
}

async function deleteDraft(row: any) {
  await ElMessageBox.confirm('确定删除该草稿?', '提示')
  if (row.type === 'MATERIAL_ENTRY') {
    await deleteMaterialApplication(row.data.id)
    ElMessage.success('删除成功')
  } else if (row.type === 'ASSET_DELETION') {
    await deleteDeletionApplication(row.data.id)
    ElMessage.success('删除成功')
  } else {
    // 使用申请删除
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
