import request from '@/utils/request'

export function createDeletionApplication(data: any) {
  return request.post('/deletion', data)
}

export function updateDeletionApplication(id: number, data: any) {
  return request.put(`/deletion/${id}`, data)
}

export function submitDeletionApplication(id: number, workflowId: number) {
  return request.post(`/deletion/${id}/submit`, null, {
    params: { workflowId }
  })
}

export function deleteDeletionApplication(id: number) {
  return request.delete(`/deletion/${id}`)
}

export function getDeletionApplicationById(id: number) {
  return request.get(`/deletion/${id}`)
}

export function getMyDeletionApplications(params: any) {
  return request.get('/deletion/my', { params })
}
