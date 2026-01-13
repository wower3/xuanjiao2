<template>
  <div class="material-entry-page">
    <el-card>
      <template #header>
        <span>{{ isEditMode ? '编辑申请单' : '新建申请单' }}</span>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="事项标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入事项标题" />
        </el-form-item>
        <el-form-item label="维护人" prop="maintainerId">
          <el-input :value="currentUser?.realName" disabled />
        </el-form-item>
        <el-form-item label="归属部门">
          <el-input :value="currentUser?.deptName" disabled />
        </el-form-item>
        <el-form-item label="保证声明">
          <el-checkbox v-model="form.guaranteeDeclaration">我保证所上传的素材符合版权要求</el-checkbox>
        </el-form-item>
      </el-form>

      <!-- 素材文件列表 -->
      <div class="file-section">
        <div class="file-header">
          <span>素材文件 ({{ fileList.length }})</span>
          <el-button type="primary" size="small" @click="showAddFile = true">
            添加文件
          </el-button>
        </div>
        <el-table :data="fileList" size="small">
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
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="danger" @click="removeFile(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="action-buttons">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="success" @click="handleSubmitDialog" :loading="submitting">提交审批</el-button>
      </div>
    </el-card>

    <!-- 添加文件对话框 -->
    <el-dialog v-model="showAddFile" title="添加素材文件" width="600px">
      <el-form :model="fileForm" :rules="fileRules" ref="fileFormRef" label-width="120px">
        <el-form-item label="文件名称" prop="name">
          <el-input v-model="fileForm.name" placeholder="请输入文件名称" />
        </el-form-item>
        <el-form-item label="文件类型" prop="type">
          <el-select v-model="fileForm.type">
            <el-option label="视频" value="VIDEO" />
            <el-option label="图片" value="IMAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件" prop="file">
          <el-upload
            ref="fileUploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="素材标签">
          <el-select v-model="fileForm.tagIds" multiple placeholder="请选择标签">
            <el-option
              v-for="tag in tagList"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
          <el-button link type="primary" @click="showCreateTag = true" style="margin-left: 10px">
            新建标签
          </el-button>
        </el-form-item>
        <el-form-item label="版权声明">
          <el-radio-group v-model="copyrightType">
            <el-radio label="none">无</el-radio>
            <el-radio label="text">文本</el-radio>
            <el-radio label="file">文件</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="copyrightType === 'text'" label="版权文本">
          <el-input v-model="fileForm.copyrightText" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item v-if="copyrightType === 'file'" label="版权文件">
          <el-upload
            ref="copyrightUploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleCopyrightFileChange"
          >
            <el-button type="primary">选择版权文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="申请说明">
          <el-input v-model="fileForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="发布渠道">
          <el-input v-model="fileForm.publishChannel" placeholder="请输入发布渠道" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddFile = false">取消</el-button>
        <el-button type="primary" @click="handleAddFile" :loading="addingFile">添加</el-button>
      </template>
    </el-dialog>

    <!-- 新建标签对话框 -->
    <el-dialog v-model="showCreateTag" title="新建标签" width="400px">
      <el-form :model="tagForm" label-width="80px">
        <el-form-item label="标签名称">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="tagForm.category" placeholder="请选择分类">
            <el-option label="图片" value="IMAGE" />
            <el-option label="视频" value="VIDEO" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTag = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTag">创建</el-button>
      </template>
    </el-dialog>

    <!-- 提交审批对话框 -->
    <el-dialog v-model="showSubmitDialog" title="提交审批" width="500px">
      <el-form label-width="100px">
        <el-form-item label="审批流程">
          <el-select v-model="submitWorkflowId" placeholder="请选择审批流程">
            <el-option
              v-for="wf in workflowList"
              :key="wf.id"
              :label="wf.name"
              :value="wf.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSubmitDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import {
  createMaterialApplication,
  updateMaterialApplication,
  submitMaterialApplication,
  getMaterialApplicationById
} from '@/api/materialApplication'
import { getTagList, createTag } from '@/api/tag'
import { uploadAsset, deleteAsset } from '@/api/asset'
import { getWorkflowList } from '@/api/workflow'
import { useUserStore } from '@/stores/user'
import { getCurrentUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentUser = ref<any>(null)
const saving = ref(false)
const submitting = ref(false)
const addingFile = ref(false)
const fileList = reactive<any[]>([])

const showAddFile = ref(false)
const showCreateTag = ref(false)
const showSubmitDialog = ref(false)

const workflowList = ref<any[]>([])
const tagList = ref<any[]>([])
const submitWorkflowId = ref<number | null>(null)
const copyrightType = ref('none')

// 判断是编辑模式还是新建模式
const isEditMode = computed(() => !!route.query.id)
const applicationId = ref<number | null>(route.query.id ? Number(route.query.id) : null)
const applicationStatus = ref<string>('DRAFT')

// 标记是否有未保存的更改
const hasUnsavedChanges = ref(false)

// 保存初始状态用于比较
const initialForm = ref({
  title: '',
  guaranteeDeclaration: false
})
const initialFileCount = ref(0)

const form = reactive({
  title: '',
  maintainerId: null as number | null,
  deptId: null as number | null,
  guaranteeDeclaration: false
})

const fileForm = reactive({
  name: '',
  type: 'IMAGE',
  tagIds: [] as number[],
  copyrightText: '',
  copyrightFilePath: '',
  description: '',
  publishChannel: ''
})

const tagForm = reactive({
  name: '',
  category: ''
})

const uploadFile = ref<File | null>(null)
const copyrightFile = ref<File | null>(null)

const rules = {
  title: [{ required: true, message: '请输入事项标题', trigger: 'blur' }]
}

const fileRules = {
  name: [{ required: true, message: '请输入文件名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择文件类型', trigger: 'change' }]
}

const formRef = ref()
const fileFormRef = ref()

async function loadWorkflows() {
  try {
    const res = await getWorkflowList()
    workflowList.value = (res.data || []).filter((w: any) =>
      w.status === 1 && (!w.type || w.type === 'MATERIAL_ENTRY')
    )
  } catch (e) {
    console.error('加载审批流程失败', e)
  }
}

async function loadTags() {
  try {
    const res = await getTagList()
    tagList.value = res.data || []
  } catch (e) {
    console.error('加载标签失败', e)
  }
}

async function loadApplication() {
  if (!applicationId.value) return

  try {
    const res = await getMaterialApplicationById(applicationId.value)
    const app = res.data
    form.title = app.title
    form.maintainerId = app.maintainerId
    form.deptId = app.deptId
    form.guaranteeDeclaration = app.guaranteeDeclaration === 1
    applicationStatus.value = app.status || 'DRAFT'
    // reactive 数组需要清空后重新填充
    fileList.splice(0, fileList.length)
    fileList.push(...(app.assets || []))
    // 记录初始状态
    initialForm.value = {
      title: app.title,
      guaranteeDeclaration: app.guaranteeDeclaration === 1
    }
    initialFileCount.value = (app.assets || []).length
    hasUnsavedChanges.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '加载申请单失败')
  }
}

// 检查是否有未保存的更改
function checkUnsavedChanges(): boolean {
  if (isEditMode.value) {
    // 编辑模式：与初始状态比较
    return form.title !== initialForm.value.title ||
           form.guaranteeDeclaration !== initialForm.value.guaranteeDeclaration ||
           fileList.length !== initialFileCount.value
  } else {
    // 新建模式：只要有输入就认为有更改
    return form.title !== '' || form.guaranteeDeclaration || fileList.length > 0
  }
}

// 监听表单和文件列表变化
watch([() => form.title, () => form.guaranteeDeclaration, () => fileList.length], () => {
  hasUnsavedChanges.value = checkUnsavedChanges()
})

// 路由守卫：离开前检查未保存的更改
onBeforeRouteLeave((to, from, next) => {
  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm(
      '您有未保存的内容，是否保存为草稿？',
      '提示',
      {
        distinguishCancelAndClose: true,
        confirmButtonText: '保存',
        cancelButtonText: '不保存',
        type: 'warning'
      }
    )
      .then(() => {
        // 用户选择保存
        saveDraftAndNavigate(to)
      })
      .catch((action) => {
        if (action === 'cancel') {
          // 用户选择不保存，直接离开
          next()
        } else {
          // 用户点击关闭按钮，取消导航
          next(false)
        }
      })
  } else {
    next()
  }
})

// 保存草稿后导航
async function saveDraftAndNavigate(to: any) {
  await formRef.value?.validate()

  if (!form.guaranteeDeclaration) {
    ElMessage.warning('请勾选保证声明')
    return
  }

  saving.value = true
  try {
    const submitData = {
      ...form,
      guaranteeDeclaration: form.guaranteeDeclaration ? 1 : 0
    }

    if (isEditMode.value) {
      await updateMaterialApplication(applicationId.value!, submitData)
      ElMessage.success('保存成功')
    } else {
      const res = await createMaterialApplication(submitData)
      ElMessage.success('保存成功')
    }
    hasUnsavedChanges.value = false
    router.push(to)
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleSaveDraft() {
  await formRef.value?.validate()

  if (!form.guaranteeDeclaration) {
    ElMessage.warning('请勾选保证声明')
    return
  }

  saving.value = true
  try {
    const submitData = {
      ...form,
      guaranteeDeclaration: form.guaranteeDeclaration ? 1 : 0
    }

    if (isEditMode.value) {
      await updateMaterialApplication(applicationId.value!, submitData)
      ElMessage.success('保存成功')
    } else {
      const res = await createMaterialApplication(submitData)
      applicationId.value = res.data.id
      ElMessage.success('创建成功')
    }
    // 更新初始状态，标记为已保存
    initialForm.value = {
      title: form.title,
      guaranteeDeclaration: form.guaranteeDeclaration
    }
    initialFileCount.value = fileList.length
    hasUnsavedChanges.value = false
    // 保存草稿后跳转到草稿箱
    goToDraftBox()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function handleSubmitDialog() {
  if (!form.guaranteeDeclaration) {
    ElMessage.warning('请勾选保证声明')
    return
  }

  if (fileList.length === 0) {
    ElMessage.warning('请至少添加一个素材文件')
    return
  }

  showSubmitDialog.value = true
}

async function handleSubmit() {
  if (!submitWorkflowId.value) {
    ElMessage.warning('请选择审批流程')
    return
  }

  submitting.value = true
  try {
    // 先保存基本信息
    if (!applicationId.value) {
      const submitData = {
        ...form,
        guaranteeDeclaration: form.guaranteeDeclaration ? 1 : 0
      }
      const res = await createMaterialApplication(submitData)
      applicationId.value = res.data.id
    }

    await submitMaterialApplication(applicationId.value!, submitWorkflowId.value!)
    ElMessage.success('提交成功')
    showSubmitDialog.value = false
    goToList()
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function handleFileChange(file: any) {
  uploadFile.value = file.raw
  if (!fileForm.name) {
    fileForm.name = file.name
  }
}

function handleCopyrightFileChange(file: any) {
  copyrightFile.value = file.raw
}

async function handleAddFile() {
  await fileFormRef.value?.validate()

  if (!uploadFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  // 确保有申请单ID
  if (!applicationId.value) {
    await formRef.value?.validate()
    if (!form.title) {
      ElMessage.warning('请先输入事项标题')
      return
    }
    if (!form.guaranteeDeclaration) {
      ElMessage.warning('请先勾选保证声明')
      return
    }

    const submitData = {
      ...form,
      guaranteeDeclaration: form.guaranteeDeclaration ? 1 : 0
    }
    const res = await createMaterialApplication(submitData)
    applicationId.value = res.data.id
  }

  addingFile.value = true
  try {
    await uploadAsset(uploadFile.value, {
      ...fileForm,
      applicationId: applicationId.value
    })
    ElMessage.success('添加成功')
    showAddFile.value = false
    resetFileForm()
    // 重新加载文件列表
    await loadApplication()
  } catch (e: any) {
    ElMessage.error(e.message || '添加失败')
  } finally {
    addingFile.value = false
  }
}

function resetFileForm() {
  fileForm.name = ''
  fileForm.type = 'IMAGE'
  fileForm.tagIds = []
  fileForm.copyrightText = ''
  fileForm.copyrightFilePath = ''
  fileForm.description = ''
  fileForm.publishChannel = ''
  uploadFile.value = null
  copyrightFile.value = null
  copyrightType.value = 'none'
}

async function removeFile(row: any) {
  // 检查申请单状态，只有草稿状态可以移除文件
  if (applicationStatus.value !== 'DRAFT') {
    ElMessage.warning('只有草稿状态可以移除文件')
    return
  }

  try {
    await ElMessageBox.confirm('确定要移除该文件吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    // 调用后端 API 删除文件
    await deleteAsset(row.id)
    ElMessage.success('移除成功')

    // 从列表中移除
    const index = fileList.findIndex(f => f.id === row.id)
    if (index > -1) {
      fileList.splice(index, 1)
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '移除失败')
    }
  }
}

async function handleCreateTag() {
  if (!tagForm.name) {
    ElMessage.warning('请输入标签名称')
    return
  }
  try {
    await createTag({ name: tagForm.name, category: tagForm.category })
    ElMessage.success('创建成功')
    showCreateTag.value = false
    tagForm.name = ''
    tagForm.category = ''
    loadTags()
  } catch (e: any) {
    ElMessage.error(e.message || '创建失败')
  }
}

function goBack() {
  goToList()
}

function goToDraftBox() {
  router.push('/task/draft')
}

function goToList() {
  router.push('/asset/material-list')
}

onMounted(async () => {
  // Fetch fresh user data from backend to get deptName
  try {
    const res = await getCurrentUser()
    currentUser.value = res.data
    userStore.setUserInfo(res.data)
  } catch (e) {
    currentUser.value = userStore.userInfo
  }

  form.maintainerId = currentUser.value?.id
  form.deptId = currentUser.value?.deptId

  await Promise.all([
    loadWorkflows(),
    loadTags()
  ])

  // 如果是编辑模式，加载申请单数据
  if (isEditMode.value) {
    await loadApplication()
  }
})
</script>

<style scoped>
.file-section {
  margin-top: 20px;
}
.file-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: bold;
}
.action-buttons {
  margin-top: 30px;
  text-align: center;
}
.action-buttons .el-button {
  margin: 0 10px;
}
</style>
