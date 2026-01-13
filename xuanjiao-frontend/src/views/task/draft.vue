<template>
  <div class="draft-box-page">
    <el-card>
      <template #header>
        <span>草稿箱</span>
      </template>

      <el-table :data="drafts" v-loading="loading">
        <el-table-column prop="title" label="事项标题" />
        <el-table-column prop="maintainerName" label="维护人" />
        <el-table-column prop="deptName" label="归属部门" />
        <el-table-column label="文件数量" width="100">
          <template #default="{ row }">
            {{ row.assets?.length || 0 }} 个
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
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
          <el-descriptions-item label="事项标题" :span="2">{{ currentDraft.title }}</el-descriptions-item>
          <el-descriptions-item label="维护人">{{ currentDraft.maintainerName }}</el-descriptions-item>
          <el-descriptions-item label="归属部门">{{ currentDraft.deptName }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentDraft.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="file-section">
          <div class="file-header">
            <span>素材文件 ({{ currentDraft.assets?.length || 0 }})</span>
          </div>
          <el-table :data="currentDraft.assets" size="small">
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
            <el-table-column prop="publishChannel" label="发布渠道" width="120" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getDraftApplications,
  deleteMaterialApplication
} from '@/api/materialApplication'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const drafts = ref<any[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const showDetail = ref(false)
const currentDraft = ref<any>(null)

async function loadDrafts() {
  loading.value = true
  try {
    const res = await getDraftApplications(query)
    drafts.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function viewDetail(row: any) {
  currentDraft.value = row
  showDetail.value = true
}

function continueEdit(row: any) {
  // 跳转到素材录入页面，带上工单ID
  router.push(`/asset/material-entry?id=${row.id}`)
}

async function deleteDraft(row: any) {
  await ElMessageBox.confirm('确定删除该草稿?', '提示')
  await deleteMaterialApplication(row.id)
  ElMessage.success('删除成功')
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
