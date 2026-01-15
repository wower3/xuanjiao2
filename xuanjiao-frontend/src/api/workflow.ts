import request from '@/utils/request'

export function getWorkflowList() {
  return request.get('/workflow/list')
}

export function getWorkflowById(id: number) {
  return request.get(`/workflow/${id}`)
}

export function saveWorkflow(data: any) {
  return request.post('/workflow', data)
}

export function updateWorkflow(data: any) {
  return request.put('/workflow', data)
}

export function updateWorkflowStatus(id: number, status: number) {
  return request.put(`/workflow/${id}/status`, null, { params: { status } })
}

/**
 * 获取下一层可选审批人
 * @param params 查询参数
 */
export function getNextStageApprovers(params: {
  stageId: number
  instanceId: number
  applicantId: number
  keyword?: string
}) {
  return request.get('/workflow/next-stage-approvers', { params })
}

/**
 * 选择下一层审批人
 * @param data 请求数据
 */
export function selectNextStageApprovers(data: {
  taskId: number
  approverIds: number[]
}) {
  return request.post('/workflow/select-next-stage-approvers', data)
}

/**
 * 获取审批实例进度
 * @param instanceId 实例ID
 */
export function getApprovalProgress(instanceId: number) {
  return request.get(`/workflow/progress/${instanceId}`)
}

/**
 * 根据角色获取绑定的审批流程
 * @param params 查询参数
 */
export function getWorkflowByRole(params: {
  roleId: number
  workflowType: string
}) {
  return request.get('/workflow/by-role', { params })
}

/**
 * 删除流程
 * @param id 流程ID
 */
export function deleteWorkflow(id: number) {
  return request.delete(`/workflow/${id}`)
}

/**
 * 绑定角色
 * @param data 请求数据
 */
export function bindRole(data: {
  id: number
  roleId: number
  workflowType: string
}) {
  return request.put(`/workflow/${data.id}/bind-role`, null, {
    params: { roleId: data.roleId, workflowType: data.workflowType }
  })
}

/**
 * 解除角色绑定
 * @param id 流程ID
 */
export function unbindRole(id: number) {
  return request.put(`/workflow/${id}/unbind-role`)
}

/**
 * 复制流程
 * @param id 流程ID
 */
export function copyWorkflow(id: number) {
  return request.post(`/workflow/${id}/copy`)
}

/**
 * 获取第一层可选审批人
 * @param params 查询参数
 */
export function getFirstStageApprovers(params: {
  workflowId: number
  applicantId: number
  keyword?: string
}) {
  return request.get('/workflow/first-stage-approvers', { params })
}

/**
 * 选择第一层审批人
 * @param data 请求数据
 */
export function selectFirstStageApprovers(data: {
  instanceId: number
  approverIds: number[]
}) {
  return request.post('/workflow/select-first-stage-approvers', data)
}

/**
 * 选择第一层审批人（包括子流程）
 * @param data 请求数据
 */
export function selectFirstStageApproversWithSubWorkflows(data: {
  instanceId: number
  approverIds: number[]
  subWorkflowApproverIds: Record<number, number[]>
}) {
  return request.post('/workflow/select-first-stage-approvers-with-subworkflows', data)
}

/**
 * 获取子流程第一层可选审批人
 * @param params 查询参数
 */
export function getSubWorkflowFirstStageApprovers(params: {
  subWorkflowId: number
  applicantId: number
  keyword?: string
}) {
  return request.get('/workflow/sub-workflow-approvers', { params })
}

/**
 * 选择下一层审批人（包括子流程）
 * @param data 请求数据
 */
export function selectNextStageApproversWithSubWorkflows(data: {
  taskId: number
  approverIds: number[]
  subWorkflowApproverIds: Record<number, number[]>
}) {
  return request.post('/workflow/select-next-stage-approvers-with-subworkflows', data)
}
