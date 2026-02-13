/**
 * 审批API模块
 * <p>提供审批任务、实例管理、草稿箱相关接口</p>
 * <p>统一入口：所有审批相关的API都在此文件中</p>
 *
 * @author system
 * @version 2.0
 */
import request from '@/utils/request'

/**
 * 获取我的待审批任务
 * @param params 分页参数
 */
export function getMyTasks(params: { pageNum?: number; pageSize?: number }) {
  return request.post('/approval/getMyTasks', params)
}

/**
 * 获取我的申请列表
 * @param params 查询参数
 */
export function getMyApplied(params: {
  pageNum?: number
  pageSize?: number
  businessType?: string
  forAllUsers?: boolean
  applicantId?: number
  deptId?: number
  roleType?: string
  status?: string
}) {
  return request.post('/approval/getMyApplied', params)
}

/**
 * 获取任务详情
 * @param id 任务ID
 */
export function getTaskDetail(id: number) {
  return request.post('/approval/getTaskDetail', { id })
}

/**
 * 获取审批实例详情
 * @param instanceId 实例ID
 */
export function getInstanceDetail(instanceId: number) {
  return request.post('/approval/getInstanceDetail', { id: instanceId })
}

/**
 * 撤回审批实例
 * @param instanceId 实例ID
 * @param comment 撤回意见
 */
export function withdrawInstance(instanceId: number, comment?: string) {
  return request.post(`/approval/instances/${instanceId}/withdraw`, null, {
    params: { comment }
  })
}

/**
 * 审批通过或驳回
 * @param id 任务ID
 * @param data 审批数据（包含审批意见和是否通过）
 */
export function approve(id: number, data: { comment: string; passed: boolean }) {
  return request.post(`/approval/tasks/${id}/approve`, data)
}

/**
 * 退回任务
 * @param id 任务ID
 * @param data 退回数据（包含退回意见）
 */
export function returnTask(id: number, data?: { comment?: string }) {
  return request.post(`/approval/tasks/${id}/return`, data || {})
}

/**
 * 重新发起子流程
 * @param id 任务ID
 * @param approverIds 审批人ID列表
 */
export function restartSubWorkflow(id: number, approverIds: number[]) {
  return request.post(`/approval/tasks/${id}/restart-sub-workflow`, approverIds)
}

// ========== 别名函数，提供更直观的命名 ==========

/**
 * 获取待审批任务列表（别名）
 * 提供更直观的函数命名
 * @param params 查询参数
 */
export function getPendingApproval(params: {
  pageNum?: number
  pageSize?: number
  businessType?: string
}) {
  return getMyTasks(params)
}

/**
 * 获取我发起的审批列表（别名）
 * 提供更直观的函数命名
 * @param params 查询参数
 */
export function getMyInitiated(params: {
  pageNum?: number
  pageSize?: number
  businessType?: string
}) {
  return getMyApplied(params)
}

/**
 * 获取待办任务数量
 * @returns 待办任务数量
 */
export function getMyTasksCount() {
  return request.post('/approval/getMyTasksCount')
}

/**
 * 查询草稿列表
 * @param params 查询参数
 */
export function getDrafts(params: {
  pageNum?: number
  pageSize?: number
  businessType?: string
}) {
  return request.post('/task/queryDrafts', params)
}
