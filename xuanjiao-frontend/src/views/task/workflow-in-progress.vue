<template>
  <div class="workflow-in-progress-page">
    <el-card>
      <template #header>
        <span>流经事项中</span>
      </template>

      <el-table :data="applications" v-loading="loading">
        <el-table-column prop="title" label="事项标题" />
        <el-table-column prop="maintainerName" label="维护人" />
        <el-table-column prop="deptName" label="归属部门" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="warning">待审批</el-tag>
            <el-tag v-else-if="row.status === 'APPROVED'" type="success">已通过</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文件数量" width="100">
          <template #default="{ row }">
            {{ row.assets?.length || 0 }} 个
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        @change="loadApplications"
      />
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="showDetail" title="申请单详情" width="900px">
      <div v-if="currentApplication">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="事项标题">{{ currentApplication.title }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentApplication.status === 'PENDING'" type="warning">待审批</el-tag>
            <el-tag v-else-if="currentApplication.status === 'APPROVED'" type="success">已通过</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="维护人">{{ currentApplication.maintainerName }}</el-descriptions-item>
          <el-descriptions-item label="归属部门">{{ currentApplication.deptName }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentApplication.createTime }}</el-descriptions-item>
        </el-descriptions>

        <!-- 素材文件列表 - 只读 -->
        <div class="file-section">
          <div class="file-header">
            <span>素材文件 ({{ currentApplication.assets?.length || 0 }})</span>
          </div>
          <el-table :data="currentApplication.assets" size="small">
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
import { getMyApplications } from '@/api/materialApplication'

const loading = ref(false)
const applications = ref<any[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const showDetail = ref(false)
const currentApplication = ref<any>(null)

async function loadApplications() {
  loading.value = true
  try {
    const res = await getMyApplications(query)
    // 只显示 PENDING 和 APPROVED 状态的工单
    applications.value = res.data.list.filter((app: any) =>
      app.status === 'PENDING' || app.status === 'APPROVED'
    )
    total.value = applications.value.length
  } finally {
    loading.value = false
  }
}

function viewDetail(row: any) {
  currentApplication.value = row
  showDetail.value = true
}

onMounted(() => {
  loadApplications()
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
