<template>
  <div class="workflow-design">
    <el-card>
      <template #header>
        <div class="header">
          <div class="header-left">
            <el-input v-model="workflow.name" placeholder="流程名称" style="width:200px" />
            <span style="color: #909399; font-size: 12px; margin-left: 10px">
              提示：角色绑定请在流程列表页面进行操作
            </span>
          </div>
          <div>
            <el-button @click="$router.back()">返回</el-button>
            <el-button type="primary" @click="save">保存</el-button>
          </div>
        </div>
      </template>
      <div class="design-area">
        <div class="stages">
          <div class="stage-item" v-for="(stage, index) in stages" :key="index">
            <div class="stage-header">
              <el-input v-model="stage.name" size="small" style="width:150px" />
              <div>
                <el-select v-model="stage.approveType" size="small" style="width:80px">
                  <el-option label="或签" value="OR" />
                  <el-option label="会签" value="AND" />
                </el-select>
                <el-button link type="danger" @click="removeStage(index)" style="margin-left: 10px">删除</el-button>
              </div>
            </div>
            <div class="stage-body">
              <!-- 显示普通审批人 -->
              <el-tag v-for="a in getNormalApprovers(stage)" :key="`approver_${a.approverType}_${a.approverId}`" closable @close="removeApprover(index, a)">
                {{ a.approverName }}
                <span v-if="a.checkSecondaryDept" style="color: #67C23A; margin-left: 5px;">(校验二级部门)</span>
              </el-tag>
              <!-- 显示子流程 -->
              <el-tag v-for="sw in getSubWorkflows(stage)" :key="`subwf_${sw.subWorkflowId}`" type="warning" closable @close="removeSubWorkflow(index, sw)">
                子流程: {{ sw.subWorkflowName }}
              </el-tag>
              <el-button link type="primary" @click="editStage(index)">
                + 添加审批人/子流程
              </el-button>
            </div>
          </div>
          <el-button @click="addStage">+ 添加审批层</el-button>
        </div>
      </div>
    </el-card>

    <!-- 审批人/子流程选择对话框 -->
    <el-dialog v-model="showApproverDialog" title="添加审批人或子流程" width="700px">
      <el-tabs v-model="approverType">
        <el-tab-pane label="指定用户" name="USER">
          <el-input
            v-model="userKeyword"
            placeholder="输入用户名或姓名搜索"
            style="margin-bottom: 10px"
            clearable
            @input="filterUsers"
          />
          <el-table :data="filteredUserList" @selection-change="handleUserSelection" v-loading="loadingData" max-height="300">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="deptName" label="部门" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="指定角色" name="ROLE">
          <div style="margin-bottom: 10px;">
            <el-checkbox v-model="checkSecondaryDept">校验二级部门（勾选后审批人需与发起人在同一二级部门）</el-checkbox>
          </div>
          <el-table :data="roleList" @selection-change="handleRoleSelection" v-loading="loadingData" max-height="300">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="code" label="角色编码" />
            <el-table-column prop="name" label="角色名称" />
            <el-table-column prop="description" label="描述" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="指定部门" name="DEPT">
          <el-table :data="deptList" @selection-change="handleDeptSelection" v-loading="loadingData" max-height="300">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="name" label="部门名称" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="嵌入子流程" name="SUB_WORKFLOW">
          <el-alert
            title="提示"
            type="info"
            :closable="false"
            style="margin-bottom: 15px"
          >
            只有未绑定角色的流程才能作为子流程嵌入。已绑定角色的流程不会显示在下方列表中。
            子流程作为该层的一个节点，与普通审批人并行执行。
          </el-alert>
          <el-select v-model="selectedSubWorkflowId" placeholder="请选择要嵌入的子流程" style="width: 100%">
            <el-option
              v-for="wf in availableSubWorkflows"
              :key="wf.id"
              :label="`${wf.name}${wf.workflowType ? ' (' + (wf.workflowType === 'ASSET_UPLOAD' ? '素材录入' : '素材使用') + ')' : ''}`"
              :value="wf.id"
              :disabled="wf.id === workflow.id || isSubWorkflowAlreadyAdded(index, wf.id)"
            >
              <span>{{ wf.name }}</span>
              <span v-if="wf.workflowType" style="color: #8492a6; font-size: 12px; margin-left: 10px">
                {{ wf.workflowType === 'ASSET_UPLOAD' ? '素材录入' : '素材使用' }}
              </span>
              <span v-if="isSubWorkflowAlreadyAdded(index, wf.id)" style="color: #F56C6C; font-size: 12px; margin-left: 10px">
                (已添加)
              </span>
            </el-option>
          </el-select>
          <div v-if="availableSubWorkflows.length === 0" style="color: #F56C6C; margin-top: 10px;">
            没有可用的子流程。请确保有未绑定角色的流程。
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showApproverDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddition">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWorkflowById, saveWorkflow, updateWorkflow, getWorkflowList } from '@/api/workflow'
import { getUserList } from '@/api/user'
import { getRoleList } from '@/api/role'
import { getDeptList } from '@/api/dept'

const route = useRoute()
const router = useRouter()

const workflow = reactive({
  id: null as any,
  name: '',
  description: '',
  workflowType: null as string | null,
  boundRoleId: null as number | null
})

const stages = ref<any[]>([])
const showApproverDialog = ref(false)
const approverType = ref('USER')
const userList = ref<any[]>([])
const filteredUserList = ref<any[]>([])
const roleList = ref<any[]>([])
const deptList = ref<any[]>([])
const availableWorkflows = ref<any[]>([])
const loadingData = ref(false)

// 计算可用作子流程的流程（只包含未绑定角色的流程）
const availableSubWorkflows = computed(() => {
  return (availableWorkflows.value || []).filter((w: any) => !w.boundRoleId)
})

const currentStageIndex = ref(-1)
const selectedSubWorkflowId = ref<number | null>(null)
const selectedUsers = ref<any[]>([])
const selectedRoles = ref<any[]>([])
const selectedDepts = ref<any[]>([])
const userKeyword = ref('')
const checkSecondaryDept = ref(false)

// 获取普通审批人（非子流程）
function getNormalApprovers(stage: any) {
  return (stage.approvers || []).filter((a: any) => !a.subWorkflowId)
}

// 获取子流程列表
function getSubWorkflows(stage: any) {
  return (stage.approvers || []).filter((a: any) => a.subWorkflowId)
}

// 检查子流程是否已添加
function isSubWorkflowAlreadyAdded(stageIndex: number, subWorkflowId: number): boolean {
  const stage = stages.value[stageIndex]
  if (!stage || !stage.approvers) return false
  return stage.approvers.some((a: any) => a.subWorkflowId === subWorkflowId)
}

// 加载数据
async function loadData() {
  const id = route.params.id
  if (id) {
    const res = await getWorkflowById(Number(id))
    Object.assign(workflow, res.data)
    stages.value = (res.data.stages || []).map((stage: any) => ({
      ...stage,
      approvers: (stage.approvers || []).map((a: any) => ({
        ...a,
        checkSecondaryDept: a.checkSecondaryDept || 0
      }))
    }))
  }
}

// 加载所有需要的数据
async function loadAllData() {
  loadingData.value = true
  try {
    const [userRes, roleRes, deptRes, workflowRes] = await Promise.all([
      getUserList(),
      getRoleList(),
      getDeptList(),
      getWorkflowList()
    ])
    userList.value = userRes.data || []
    filteredUserList.value = userList.value
    roleList.value = roleRes.data || []
    deptList.value = deptRes.data || []
    availableWorkflows.value = (workflowRes.data || []).filter((w: any) => w.status === 1)
  } finally {
    loadingData.value = false
  }
}

// 用户搜索过滤
function filterUsers() {
  const keyword = userKeyword.value.toLowerCase().trim()
  if (!keyword) {
    filteredUserList.value = userList.value
    return
  }
  filteredUserList.value = userList.value.filter((user: any) =>
    (user.username && user.username.toLowerCase().includes(keyword)) ||
    (user.realName && user.realName.toLowerCase().includes(keyword)) ||
    (user.realName && user.realName.toLowerCase().includes(keyword))
  )
}

// 添加审批层
function addStage() {
  stages.value.push({
    name: `审批层${stages.value.length + 1}`,
    approveType: 'OR',
    approvers: []
  })
}

// 删除审批层
function removeStage(index: number) {
  stages.value.splice(index, 1)
}

// 编辑审批层
function editStage(index: number) {
  currentStageIndex.value = index
  approverType.value = 'USER'
  selectedUsers.value = []
  selectedRoles.value = []
  selectedDepts.value = []
  selectedSubWorkflowId.value = null
  checkSecondaryDept.value = false
  userKeyword.value = ''
  filterUsers()
  loadAllData()
  showApproverDialog.value = true
}

// 用户选择
function handleUserSelection(selection: any[]) {
  selectedUsers.value = selection
}

// 角色选择
function handleRoleSelection(selection: any[]) {
  selectedRoles.value = selection
}

// 部门选择
function handleDeptSelection(selection: any[]) {
  selectedDepts.value = selection
}

// 确认添加（审批人或子流程）
function confirmAddition() {
  const stage = stages.value[currentStageIndex.value]

  if (approverType.value === 'SUB_WORKFLOW') {
    // 添加子流程
    if (selectedSubWorkflowId.value) {
      const selectedWorkflow = availableWorkflows.value.find((w: any) => w.id === selectedSubWorkflowId.value)
      // 检查是否已添加
      if (!stage.approvers.some((a: any) => a.subWorkflowId === selectedSubWorkflowId.value)) {
        stage.approvers.push({
          approverType: 'SUB_WORKFLOW',
          approverId: null, // 子流程不需要 approverId
          subWorkflowId: selectedSubWorkflowId.value,
          subWorkflowName: selectedWorkflow?.name || '',
          approverName: `子流程: ${selectedWorkflow?.name || ''}`,
          checkSecondaryDept: 0
        })
      }
    }
  } else {
    // 添加普通审批人
    // 添加用户
    selectedUsers.value.forEach(user => {
      const key = `USER_${user.id}`
      if (!stage.approvers.find((a: any) => `${a.approverType}_${a.approverId}` === key)) {
        stage.approvers.push({
          approverType: 'USER',
          approverId: user.id,
          approverName: `[用户] ${user.realName || user.username}`,
          checkSecondaryDept: 0
        })
      }
    })

    // 添加角色（带二级部门校验选项）
    selectedRoles.value.forEach(role => {
      const key = `ROLE_${role.id}`
      if (!stage.approvers.find((a: any) => `${a.approverType}_${a.approverId}` === key)) {
        stage.approvers.push({
          approverType: 'ROLE',
          approverId: role.id,
          approverName: `[角色] ${role.name}`,
          checkSecondaryDept: checkSecondaryDept.value ? 1 : 0
        })
      }
    })

    // 添加部门
    selectedDepts.value.forEach(dept => {
      const key = `DEPT_${dept.id}`
      if (!stage.approvers.find((a: any) => `${a.approverType}_${a.approverId}` === key)) {
        stage.approvers.push({
          approverType: 'DEPT',
          approverId: dept.id,
          approverName: `[部门] ${dept.name}`,
          checkSecondaryDept: 0
        })
      }
    })
  }

  showApproverDialog.value = false
}

// 移除审批人
function removeApprover(stageIndex: number, approver: any) {
  const stage = stages.value[stageIndex]
  stage.approvers = stage.approvers.filter((a: any) =>
    !(a.approverType === approver.approverType && a.approverId === approver.approverId)
  )
}

// 移除子流程
function removeSubWorkflow(stageIndex: number, subWorkflow: any) {
  const stage = stages.value[stageIndex]
  stage.approvers = stage.approvers.filter((a: any) => a.subWorkflowId !== subWorkflow.subWorkflowId)
}

// 保存流程
async function save() {
  if (!workflow.name) {
    ElMessage.warning('请输入流程名称')
    return
  }

  // 验证每层必须有至少一个审批人（包括子流程）
  for (let i = 0; i < stages.value.length; i++) {
    const stage = stages.value[i]
    if (!stage.approvers || stage.approvers.length === 0) {
      ElMessage.warning(`第${i + 1}层"${stage.name}"必须配置至少一个审批人或子流程`)
      return
    }
    // 验证至少有一个普通审批人（保证主流程正常运转）
    const hasNormalApprover = stage.approvers.some((a: any) => !a.subWorkflowId)
    if (!hasNormalApprover) {
      ElMessage.warning(`第${i + 1}层"${stage.name}"必须至少有一个普通审批人（用户/角色/部门），不能只有子流程`)
      return
    }
  }

  const data = {
    ...workflow,
    stages: stages.value
  }

  try {
    if (workflow.id) {
      await updateWorkflow(data)
      ElMessage.success('保存成功')
      router.push('/workflow')
    } else {
      // 新建流程：保存后跳转到编辑页面
      const res = await saveWorkflow(data)
      ElMessage.success('保存成功')
      // 后端应该返回新创建的流程ID，跳转到编辑页面
      if (res.data && res.data.id) {
        router.push(`/workflow/design/${res.data.id}`)
      } else {
        // 如果没有返回ID，可能是后端问题，跳转到列表页
        router.push('/workflow')
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.header { display: flex; justify-content: space-between; }
.header-left { display: flex; gap: 10px; align-items: center; }
.stages { padding: 20px; }
.stage-item { border: 1px solid #ddd; border-radius: 4px; margin-bottom: 10px; }
.stage-header { padding: 10px; background: #f5f5f5; display: flex; justify-content: space-between; align-items: center; }
.stage-body { padding: 10px; }
</style>
