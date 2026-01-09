<template>
  <div class="log-page">
    <el-card>
      <template #header>使用日志</template>
      <el-form :inline="true" :model="query">
        <el-form-item label="操作类型">
          <el-select v-model="query.action" clearable>
            <el-option label="上传" value="UPLOAD" />
            <el-option label="下载" value="DOWNLOAD" />
            <el-option label="预览" value="PREVIEW" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="assetName" label="素材名称" />
        <el-table-column prop="userName" label="操作人" />
        <el-table-column prop="action" label="操作类型" />
        <el-table-column prop="ip" label="IP地址" />
        <el-table-column prop="createTime" label="操作时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getLogList } from '@/api/log'

const loading = ref(false)
const list = ref([])
const query = reactive({ action: '', pageNum: 1, pageSize: 10 })

async function loadData() {
  loading.value = true
  try {
    const res = await getLogList(query)
    list.value = res.data?.list || []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
