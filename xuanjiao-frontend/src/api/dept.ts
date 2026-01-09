import request from '@/utils/request'

export function getDeptList() {
  return request.get('/dept/list')
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
