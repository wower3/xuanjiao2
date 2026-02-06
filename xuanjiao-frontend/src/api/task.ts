import request from '@/utils/request'

// ========== 草稿箱相关API ==========
// 由 TaskController 处理：/task/drafts
export function getDrafts(params: any) {
  return request.post('/task/queryDrafts', params)
}

// ========== 审批相关API ==========
// 由 ApprovalController 处理：/approval/*
export function getMyInitiated(params: any) {
  return request.post('/approval/getMyApplied', params)
}

export function getPendingApproval(params: any) {
  return request.post('/approval/getMyTasks', params)
}

export function getMyTasksCount() {
  return request.post('/approval/getMyTasksCount')
}

export function getTaskDetail(id: number) {
  return request.post('/approval/getTaskDetail', { id })
}

export function getInstanceDetail(instanceId: number) {
  return request.post('/approval/getInstanceDetail', { id: instanceId })
}

export function approve(id: number, comment: string, passed: boolean) {
  return request.post(`/approval/tasks/${id}/approve`, null, {
    params: { comment, passed }
  })
}

export function returnTask(id: number, comment?: string) {
  return request.post(`/approval/tasks/${id}/return`, null, {
    params: { comment }
  })
}

export function withdrawInstance(instanceId: number, comment?: string) {
  return request.post(`/approval/instances/${instanceId}/withdraw`, null, {
    params: { comment }
  })
}

export function restartSubWorkflow(id: number, approverIds: number[]) {
  return request.post(`/approval/tasks/${id}/restart-sub-workflow`, approverIds)
}

// ========== 工作流审批人选择相关API ==========
// 由 ApproverSelectionController 处理：/workflow/*
// 注意：workflow 相关 API 已统一到 @/api/workflow.ts
