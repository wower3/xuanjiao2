<!-- 知会对话框组件 - 提供向其他用户发送知会通知的功能 -->
<template>
  <el-dialog
    v-model="visible"
    title="知会"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="form" label-width="80px" @submit.prevent="handleConfirm">
      <el-form-item label="工单">
        <div class="instance-info">
          <div class="instance-title">{{ instanceTitle }}</div>
          <el-tag :type="getInstanceStatusType(instanceStatus)" size="small">
            {{ getInstanceStatusText(instanceStatus) }}
          </el-tag>
        </div>
      </el-form-item>
      <el-form-item label="知会给" required>
        <UserSelector v-model="form.recipientIds" />
      </el-form-item>
      <el-form-item label="附加消息">
        <el-input
          v-model="form.message"
          type="textarea"
          :rows="3"
          placeholder="请输入附加消息（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="submitting">
        知会
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import UserSelector from '@/components/UserSelector.vue'
import { notifyUsers } from '@/api/notification'

const props = defineProps<{
  modelValue: boolean
  instanceId?: number
  instanceTitle?: string
  instanceStatus?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = ref(props.modelValue)
const submitting = ref(false)

const form = reactive({
  recipientIds: [] as number[],
  message: ''
})

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function handleClose() {
  visible.value = false
  form.recipientIds = []
  form.message = ''
}

async function handleConfirm() {
  if (!props.instanceId) {
    ElMessage.warning('工单ID不能为空')
    return
  }
  if (form.recipientIds.length === 0) {
    ElMessage.warning('请选择要知会的用户')
    return
  }

  submitting.value = true
  try {
    await notifyUsers({
      instanceId: props.instanceId,
      recipientIds: form.recipientIds,
      message: form.message
    })
    ElMessage.success('知会成功')
    emit('success')
    handleClose()
  } catch (e: any) {
    ElMessage.error(e.message || '知会失败')
  } finally {
    submitting.value = false
  }
}

function getInstanceStatusType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

function getInstanceStatusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '审批中',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

// 暴露open方法供父组件调用
function open(instance: { id: number; title: string; status: string }) {
  emit('update:modelValue', true)
  // 等待dialog打开后再更新数据
  setTimeout(() => {
    // 通过props传递数据，这里不需要额外处理
  }, 0)
}

defineExpose({
  open
})
</script>

<style scoped>
.instance-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.instance-title {
  flex: 1;
  font-weight: 500;
  color: #303133;
}
</style>
