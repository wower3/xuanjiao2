<template>
  <div class="asset-selector">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="query" style="margin-bottom: 15px">
      <el-form-item label="名称">
        <el-input v-model="query.name" placeholder="素材名称" clearable @change="loadData" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.type" placeholder="全部" clearable @change="loadData">
          <el-option label="视频" value="VIDEO" />
          <el-option label="图片" value="IMAGE" />
          <el-option label="文档" value="DOCUMENT" />
        </el-select>
      </el-form-item>
    </el-form>

    <!-- 素材列表 -->
    <el-table
      :data="assets"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      height="400"
    >
      <el-table-column type="selection" width="55" :selectable="isSelectable" />
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
      <el-table-column prop="name" label="素材名称" min-width="200" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'APPROVED'" type="success">已通过</el-tag>
          <el-tag v-else type="info">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      @change="loadData"
      style="margin-top: 15px; text-align: right"
    />

    <!-- 操作按钮 -->
    <div style="margin-top: 15px; text-align: right">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="selectedAssets.length === 0">
        确定 (已选择 {{ selectedAssets.length }} 个)
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoCamera, Document } from '@element-plus/icons-vue'
import { getAssetList } from '@/api/asset'

interface Props {
  selectedIds?: number[]
}

interface Emits {
  (e: 'select', assets: any[]): void
  (e: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  selectedIds: () => []
})

const emit = defineEmits<Emits>()

const loading = ref(false)
const assets = ref<any[]>([])
const selectedAssets = ref<any[]>([])
const total = ref(0)
const query = reactive({
  name: '',
  type: '',
  status: 'APPROVED', // 只显示已通过的素材
  pageNum: 1,
  pageSize: 10
})

async function loadData() {
  loading.value = true
  try {
    const res = await getAssetList(query)
    assets.value = res.data.list || []
    total.value = res.data.total || 0

    // 恢复已选择的状态
    assets.value.forEach(asset => {
      if (props.selectedIds.includes(asset.id)) {
        // 在下一帧设置选中状态，确保表格已渲染
        setTimeout(() => {
          const table = document.querySelector('.el-table')
          if (table) {
            const rows = table.querySelectorAll('.el-table__row')
            rows.forEach((row, index) => {
              if (assets.value[index]?.id === asset.id) {
                const checkbox = row.querySelector('.el-checkbox__input')
                if (checkbox && !checkbox.classList.contains('is-checked')) {
                  (checkbox as HTMLElement).click()
                }
              }
            })
          }
        }, 50)
      }
    })
  } catch (e) {
    console.error('加载素材列表失败:', e)
    ElMessage.error('加载素材列表失败')
  } finally {
    loading.value = false
  }
}

function isSelectable(row: any) {
  // 只能选择已通过且未选择的素材
  return row.status === 'APPROVED' && !props.selectedIds.includes(row.id)
}

function handleSelectionChange(selection: any[]) {
  selectedAssets.value = selection
}

function handleConfirm() {
  emit('select', selectedAssets.value)
}

function getPreviewUrl(id: number) {
  return `/api/asset/preview/${id}`
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.asset-selector {
  padding: 10px;
}
</style>
