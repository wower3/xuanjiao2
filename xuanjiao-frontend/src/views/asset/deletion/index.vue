<template>
  <div class="asset-deletion-page">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="已录入素材" name="assets">
          <MyAssets
            v-if="activeTab === 'assets'"
            @selection-change="handleSelectionChange"
            @go-to-deletion="handleGoToDeletion"
          />
        </el-tab-pane>
        <el-tab-pane label="删除申请" name="deletion">
          <DeletionApply
            v-if="activeTab === 'deletion'"
            :key="deletionKey"
            :selected-asset-ids="selectedAssetIds"
            :selected-assets="selectedAssets"
            :application-id="applicationId"
            @back-to-assets="handleBackToAssets"
            @reset="handleResetDeletion"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MyAssets from './MyAssets.vue'
import DeletionApply from './DeletionApply.vue'

const route = useRoute()

const activeTab = ref('assets')
const selectedAssetIds = ref<number[]>([])
const selectedAssets = ref<any[]>([])
const deletionKey = ref(0)
const applicationId = ref<number | null>(null)

// 检查 URL 参数，如果有 id 则进入编辑模式
onMounted(() => {
  const id = route.query.id as number
  if (id) {
    applicationId.value = id
    activeTab.value = 'deletion'
    deletionKey.value++
  }
})

function handleSelectionChange(data: { assetIds: number[]; assets: any[] }) {
  selectedAssetIds.value = data.assetIds
  selectedAssets.value = data.assets
}

function handleGoToDeletion() {
  activeTab.value = 'deletion'
}

function handleBackToAssets() {
  activeTab.value = 'assets'
  selectedAssetIds.value = []
  selectedAssets.value = []
}

function handleResetDeletion() {
  // 重置删除申请页面的状态
  selectedAssetIds.value = []
  selectedAssets.value = []
  applicationId.value = null
  deletionKey.value++
}
</script>

<style scoped>
.asset-deletion-page {
  padding: 20px;
}
</style>
