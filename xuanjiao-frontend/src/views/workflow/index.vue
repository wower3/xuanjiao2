<template>
  <div class="workflow-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>流程管理</span>
          <el-button type="primary" @click="$router.push('/workflow/design')">新建流程</el-button>
        </div>
      </template>
      <el-form :inline="true">
        <el-form-item label="流程类型">
          <el-select v-model="typeFilter" placeholder="全部" clearable @change="loadData">
            <el-option label="素材录入审批" value="ASSET_UPLOAD" />
            <el-option label="素材使用审批" value="ASSET_USAGE" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="name" label="流程名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="!row.type || row.type === 'ASSET_UPLOAD'" type="primary">素材录入</el-tag>
            <el-tag v-else type="success">素材使用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/workflow/design/${row.id}`)">编辑</el-button>
            <el-button link type="primary" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWorkflowList, updateWorkflowStatus } from '@/api/workflow'

const loading = ref(false)
const list = ref([])
const typeFilter = ref('')

async function loadData() {
  loading.value = true
  try {
    const res = await getWorkflowList()
    let workflows = res.data || []
    if (typeFilter.value) {
      workflows = workflows.filter((w: any) => w.type === typeFilter.value)
    } else if (typeFilter.value === '') {
      // 不过滤
    }
    list.value = workflows
  } finally {
    loading.value = false
  }
}

async function toggleStatus(row: any) {
  const newStatus = row.status === 1 ? 0 : 1
  await updateWorkflowStatus(row.id, newStatus)
  ElMessage.success('操作成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
