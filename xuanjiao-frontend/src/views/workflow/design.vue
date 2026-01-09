<template>
  <div class="workflow-design">
    <el-card>
      <template #header>
        <div class="header">
          <el-input v-model="workflow.name" placeholder="流程名称" style="width:200px" />
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
                <el-button link type="danger" @click="removeStage(index)">删除</el-button>
              </div>
            </div>
            <div class="stage-body">
              <el-tag v-for="a in stage.approvers" :key="a.approverId" closable @close="removeApprover(index, a)">
                {{ a.approverName }}
              </el-tag>
              <el-button link type="primary" @click="editStage(index)">+ 添加审批人</el-button>
            </div>
          </div>
          <el-button @click="addStage">+ 添加审批层</el-button>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="showApproverDialog" title="选择审批人" width="600px">
      <el-tabs v-model="approverType">
        <el-tab-pane label="指定用户" name="USER">
          <el-table :data="userList" @selection-change="handleUserSelection" v-loading="loadingData" max-height="300">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="realName" label="姓名" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="指定角色" name="ROLE">
          <el-table :data="roleList" @selection-change="handleRoleSelection" v-loading="loadingData" max-height="300">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="code" label="角色编码" />
            <el-table-column prop="name" label="角色名称" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="指定部门" name="DEPT">
          <el-table :data="deptList" @selection-change="handleDeptSelection" v-loading="loadingData" max-height="300">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="name" label="部门名称" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showApproverDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmApprovers">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getWorkflowById, saveWorkflow, updateWorkflow } from '@/api/workflow'
import { getUserList } from '@/api/user'
import { getRoleList } from '@/api/role'
import { getDeptList } from '@/api/dept'

const route = useRoute()
const router = useRouter()
const workflow = reactive({ id: null as any, name: '', description: '' })
const stages = ref<any[]>([])
const showApproverDialog = ref(false)
const approverType = ref('USER')
const userList = ref<any[]>([])
const roleList = ref<any[]>([])
const deptList = ref<any[]>([])
const loadingData = ref(false)
const currentStageIndex = ref(-1)
const selectedUsers = ref<any[]>([])
const selectedRoles = ref<any[]>([])
const selectedDepts = ref<any[]>([])

async function loadData() {
  const id = route.params.id
  if (id) {
    const res = await getWorkflowById(Number(id))
    Object.assign(workflow, res.data)
    stages.value = res.data.stages || []
  }
}

function addStage() {
  stages.value.push({ name: `审批层${stages.value.length + 1}`, approveType: 'OR', approvers: [] })
}

function removeStage(index: number) {
  stages.value.splice(index, 1)
}

function editStage(index: number) {
  currentStageIndex.value = index
  approverType.value = 'USER'
  selectedUsers.value = []
  selectedRoles.value = []
  selectedDepts.value = []
  loadAllData()
  showApproverDialog.value = true
}

async function loadAllData() {
  loadingData.value = true
  try {
    const [userRes, roleRes, deptRes] = await Promise.all([
      getUserList(),
      getRoleList(),
      getDeptList()
    ])
    userList.value = userRes.data || []
    roleList.value = roleRes.data || []
    deptList.value = deptRes.data || []
  } finally {
    loadingData.value = false
  }
}

function handleUserSelection(selection: any[]) {
  selectedUsers.value = selection
}

function handleRoleSelection(selection: any[]) {
  selectedRoles.value = selection
}

function handleDeptSelection(selection: any[]) {
  selectedDepts.value = selection
}

function confirmApprovers() {
  const stage = stages.value[currentStageIndex.value]
  // 添加用户
  selectedUsers.value.forEach(user => {
    const key = `USER_${user.id}`
    if (!stage.approvers.find((a: any) => `${a.approverType}_${a.approverId}` === key)) {
      stage.approvers.push({
        approverType: 'USER',
        approverId: user.id,
        approverName: `[用户] ${user.realName || user.username}`
      })
    }
  })
  // 添加角色
  selectedRoles.value.forEach(role => {
    const key = `ROLE_${role.id}`
    if (!stage.approvers.find((a: any) => `${a.approverType}_${a.approverId}` === key)) {
      stage.approvers.push({
        approverType: 'ROLE',
        approverId: role.id,
        approverName: `[角色] ${role.name}`
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
        approverName: `[部门] ${dept.name}`
      })
    }
  })
  showApproverDialog.value = false
}

function removeApprover(stageIndex: number, approver: any) {
  const stage = stages.value[stageIndex]
  stage.approvers = stage.approvers.filter((a: any) =>
    !(a.approverType === approver.approverType && a.approverId === approver.approverId)
  )
}

async function save() {
  if (!workflow.name) {
    ElMessage.warning('请输入流程名称')
    return
  }
  // 验证每层必须有审批人
  for (let i = 0; i < stages.value.length; i++) {
    if (!stages.value[i].approvers || stages.value[i].approvers.length === 0) {
      ElMessage.warning(`第${i + 1}层"${stages.value[i].name}"必须配置审批人`)
      return
    }
  }
  const data = { ...workflow, stages: stages.value }
  if (workflow.id) {
    await updateWorkflow(data)
  } else {
    await saveWorkflow(data)
  }
  ElMessage.success('保存成功')
  router.push('/workflow')
}

onMounted(loadData)
</script>

<style scoped>
.header { display: flex; justify-content: space-between; }
.stages { padding: 20px; }
.stage-item { border: 1px solid #ddd; border-radius: 4px; margin-bottom: 10px; }
.stage-header { padding: 10px; background: #f5f5f5; display: flex; justify-content: space-between; }
.stage-body { padding: 10px; }
</style>
