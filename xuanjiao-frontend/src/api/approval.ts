import request from '@/utils/request'

export function getMyTasks(params: any) {
  return request.get('/approval/tasks', { params })
}

export function getMyApplied(params: any) {
  return request.get('/approval/applied', { params })
}

export function getTaskDetail(id: number) {
  return request.get(`/approval/tasks/${id}/detail`)
}

export function getInstanceDetail(instanceId: number) {
  return request.get(`/approval/instances/${instanceId}/detail`)
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
