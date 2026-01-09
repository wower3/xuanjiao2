<template>
  <div class="dept-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>部门管理</span>
          <el-button type="primary" @click="handleAdd">新增部门</el-button>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" row-key="id">
        <el-table-column prop="name" label="部门名称" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDeptList, deleteDept } from '@/api/dept'
import { ElMessageBox, ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])

async function loadData() {
  loading.value = true
  try {
    const res = await getDeptList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  ElMessage.info('新增功能开发中')
}

function handleEdit(row: any) {
  ElMessage.info('编辑功能开发中')
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm('确定删除该部门?', '提示')
  await deleteDept(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
