import request from '@/utils/request'

export function createMaterialApplication(data: any) {
  return request.post('/material-application', data)
}

export function updateMaterialApplication(id: number, data: any) {
  return request.put(`/material-application/${id}`, data)
}

export function submitMaterialApplication(id: number, workflowId: number) {
  return request.post(`/material-application/${id}/submit`, null, {
    params: { workflowId }
  })
}

export function deleteMaterialApplication(id: number) {
  return request.delete(`/material-application/${id}`)
}

export function getMaterialApplicationById(id: number) {
  return request.get(`/material-application/${id}`)
}

export function getMyApplications(params: any) {
  return request.get('/material-application/my', { params })
}
