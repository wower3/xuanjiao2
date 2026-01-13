import request from '@/utils/request'

export function getRoleList() {
  return request.get('/role/list')
}

export function getRoleById(id: number) {
  return request.get(`/role/${id}`)
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

export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return request.post(`/role/${roleId}/menus`, menuIds)
}

export function getRoleMenus(roleId: number) {
  return request.get(`/role/${roleId}/menus`)
}

