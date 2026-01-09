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
