import request from '@/utils/request'

export function createMaterialApplication(data: any) {
  return request.post('/material-application/create', data)
}

export function updateMaterialApplication(id: number, data: any) {
  return request.post('/material-application/update', { id, ...data })
}

export function submitMaterialApplication(id: number, workflowId: number) {
  return request.post(`/material-application/${id}/submit`, null, {
    params: { workflowId }
  })
}

export function deleteMaterialApplication(id: number) {
  return request.post('/material-application/delete', { id })
}

export function getMaterialApplicationById(id: number) {
  return request.post('/material-application/getDetail', { id })
}

export function getDraftApplications(params: any) {
  return request.post('/material-application/getDrafts', params)
}

export function getMyApplications(params: any) {
  return request.post('/material-application/getMyApplications', params)
}

export function copyApplication(id: number) {
  return request.post(`/material-application/${id}/copy`)
}
