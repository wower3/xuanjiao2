import request from '@/utils/request'

export function getRoleList() {
  return request.post('/role/getList', {})
}

export function getRoleById(id: number) {
  return request.post('/role/getDetail', { id })
}

export function createRole(data: any) {
  return request.post('/role/create', data)
}

export function updateRole(data: any) {
  return request.post('/role/update', data)
}

export function deleteRole(id: number) {
  return request.post('/role/delete', { id })
}

export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return request.post(`/role/${roleId}/menus`, menuIds)
}

export function getRoleMenus(roleId: number) {
  return request.post('/role/getRoleMenus', { roleId })
}
