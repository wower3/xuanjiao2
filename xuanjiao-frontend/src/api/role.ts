import request from '@/utils/request'

export function getRoleList() {
  return request.get('/role/list')
}

export function createRole(data: any) {
  return request.post('/role', data)
}

export function updateRole(data: any) {
  return request.put('/role', data)
}

export function deleteRole(id: number) {
  return request.delete(`/role/${id}`)
}
