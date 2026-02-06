/**
 * 审批API模块
 * <p>提供审批任务、实例管理相关接口</p>
 * <p>注意：task.ts 已包含审批相关API，此文件为冗余保留</p>
 *
 * @author system
 * @version 1.0
 * @deprecated 请使用 @/api/task.ts
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
 * @param comment 审批意见
 * @param passed 是否通过
 */
export function approve(id: number, comment: string, passed: boolean) {
  return request.post(`/approval/tasks/${id}/approve`, null, {
    params: { comment, passed }
  })
}

/**
 * 退回任务
 * @param id 任务ID
 * @param comment 退回意见
 */
export function returnTask(id: number, comment?: string) {
  return request.post(`/approval/tasks/${id}/return`, null, {
    params: { comment }
  })
}

/**
 * 重新发起子流程
 * @param id 任务ID
 * @param approverIds 审批人ID列表
 */
export function restartSubWorkflow(id: number, approverIds: number[]) {
  return request.post(`/approval/tasks/${id}/restart-sub-workflow`, approverIds)
}
