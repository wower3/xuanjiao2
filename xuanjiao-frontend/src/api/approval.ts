import request from '@/utils/request'

export function getMyTasks(params: any) {
  return request.get('/approval/tasks', { params })
}

export function getMyApplied(params: any) {
  return request.get('/approval/applied', { params })
}

export function approve(id: number, comment: string, passed: boolean) {
  return request.post(`/approval/tasks/${id}/approve`, null, {
    params: { comment, passed }
  })
}
