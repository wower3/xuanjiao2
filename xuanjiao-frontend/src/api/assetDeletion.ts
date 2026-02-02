import request from '@/utils/request'

export function createDeletionApplication(data: any) {
  return request.post('/deletion/create', data)
}

export function updateDeletionApplication(id: number, data: any) {
  return request.post('/deletion/update', { id, ...data })
}

export function submitDeletionApplication(id: number, workflowId: number) {
  return request.post(`/deletion/${id}/submit`, null, {
    params: { workflowId }
  })
}

export function deleteDeletionApplication(id: number) {
  return request.post('/deletion/delete', { id })
}

export function getDeletionApplicationById(id: number) {
  return request.post('/deletion/getDetail', { id })
}

export function getMyDeletionApplications(params: any) {
  return request.post('/deletion/getMyApplications', params)
}

// 复制删除申请
export function copyApplication(id: number) {
  return request.post(`/deletion/${id}/copy`)
}
