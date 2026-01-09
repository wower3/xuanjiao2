import request from '@/utils/request'

export function getAssetList(params: any) {
  return request.get('/asset/list', { params })
}

export function getAssetById(id: number) {
  return request.get(`/asset/${id}`)
}

export function deleteAsset(id: number) {
  return request.delete(`/asset/${id}`)
}

export function uploadAsset(file: File, data: any) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('name', data.name)
  formData.append('type', data.type)
  if (data.workflowId) {
    formData.append('workflowId', data.workflowId)
  }
  return request.post('/asset/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
