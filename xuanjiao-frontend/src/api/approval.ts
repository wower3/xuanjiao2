import request from '@/utils/request'

export function getMyTasks(params: { pageNum?: number; pageSize?: number }) {
  return request.post('/approval/getMyTasks', params)
}

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

export function getTaskDetail(id: number) {
  return request.post('/approval/getTaskDetail', { id })
}

export function getInstanceDetail(instanceId: number) {
  return request.post('/approval/getInstanceDetail', { id: instanceId })
}

export function withdrawInstance(instanceId: number, comment?: string) {
  return request.post(`/approval/instances/${instanceId}/withdraw`, null, {
    params: { comment }
  })
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

export function restartSubWorkflow(id: number, approverIds: number[]) {
  return request.post(`/approval/tasks/${id}/restart-sub-workflow`, approverIds)
}
