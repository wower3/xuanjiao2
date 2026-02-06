/**
 * 任务与审批API模块
 * <p>提供草稿箱、我发起的、待办任务等接口</p>
 *
 * @author system
 * @version 1.0
 */
import request from '@/utils/request'

// ========== 草稿箱相关API ==========
// 由 TaskController 处理：/task/drafts

/**
 * 查询草稿列表
 * @param params 查询参数
 */
export function getDrafts(params: any) {
  return request.post('/task/queryDrafts', params)
}

// ========== 审批相关API ==========
// 由 ApprovalController 处理：/approval/*

/**
 * 获取我发起的审批列表
 * @param params 查询参数
 */
export function getMyInitiated(params: any) {
  return request.post('/approval/getMyApplied', params)
}

/**
 * 获取待审批任务列表
 * @param params 查询参数
 */
export function getPendingApproval(params: any) {
  return request.post('/approval/getMyTasks', params)
}

/**
 * 获取待办任务数量
 */
export function getMyTasksCount() {
  return request.post('/approval/getMyTasksCount')
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
 * 重新发起子流程
 * @param id 任务ID
 * @param approverIds 子流程审批人ID列表
 */
export function restartSubWorkflow(id: number, approverIds: number[]) {
  return request.post(`/approval/tasks/${id}/restart-sub-workflow`, approverIds)
}

// ========== 工作流审批人选择相关API ==========
// 由 ApproverSelectionController 处理：/workflow/*
// 注意：workflow 相关 API 已统一到 @/api/workflow.ts
