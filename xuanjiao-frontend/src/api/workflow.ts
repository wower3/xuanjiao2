/**
 * 审批流程API模块
 * <p>提供流程定义、审批人选择、角色绑定等接口</p>
 *
 * @author system
 * @version 1.0
 */
import request from '@/utils/request'

/**
 * 获取流程定义列表
 */
export function getWorkflowList() {
  return request.post('/workflow/getList', {})
}

/**
 * 获取流程详情
 * @param id 流程ID
 */
export function getWorkflowById(id: number) {
  return request.post('/workflow/getDetail', { id })
}

/**
 * 创建流程
 * @param data 流程信息
 */
export function saveWorkflow(data: any) {
  return request.post('/workflow/create', data)
}

/**
 * 更新流程
 * @param data 流程信息
 */
export function updateWorkflow(data: any) {
  return request.post('/workflow/update', data)
}

/**
 * 更新流程状态（启用/禁用）
 * @param id 流程ID
 * @param status 状态（1启用/0禁用）
 */
export function updateWorkflowStatus(id: number, status: number) {
  return request.post('/workflow/updateStatus', { id, status })
}

/**
 * 获取审批实例进度
 * @param instanceId 实例ID
 */
export function getApprovalProgress(instanceId: number) {
  return request.post('/workflow/getApprovalProgress', { instanceId })
}

/**
 * 删除流程
 * @param id 流程ID
 */
export function deleteWorkflow(id: number) {
  return request.post('/workflow/delete', { id })
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
  return request.post('/workflow/bindRole', data)
}

/**
 * 解除角色绑定
 * @param id 流程ID
 */
export function unbindRole(id: number) {
  return request.post('/workflow/unbindRole', { id })
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
  return request.post('/workflow/getFirstStageApprovers', params)
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
  return request.post('/workflow/getSubWorkflowFirstStageApprovers', params)
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
