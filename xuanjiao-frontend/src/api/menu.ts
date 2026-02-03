import request from '@/utils/request'

export function getMenuTree() {
  return request.post('/menu/getTree', {})
}

export function getCurrentMenus() {
  return request.post('/menu/getCurrent', {})
}

export function getMenuById(id: number) {
  return request.post('/menu/getDetail', { id })
}

export function saveMenu(data: any) {
  return request.post('/menu/create', data)
}

export function updateMenu(data: any) {
  return request.post('/menu/update', data)
}

export function deleteMenu(id: number) {
  return request.post('/menu/delete', { id })
}

export function getRoleMenus(roleId: number) {
  return request.post('/menu/getRoleMenus', { roleId })
}
