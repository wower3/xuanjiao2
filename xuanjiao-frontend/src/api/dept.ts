import request from '@/utils/request'

export function getDeptList() {
  return request.get('/dept/list')
}

export function getDeptTree() {
  return request.get('/dept/tree')
}

export function getDeptById(id: number) {
  return request.get(`/dept/${id}`)
}

export function saveDept(data: any) {
  return request.post('/dept', data)
}

export function updateDept(data: any) {
  return request.put('/dept', data)
}

export function deleteDept(id: number) {
  return request.delete(`/dept/${id}`)
}

export function generateDeptCode() {
  return request.get('/dept/generate-code')
}

