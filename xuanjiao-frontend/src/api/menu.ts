import request from '@/utils/request'

export function getMenuTree() {
  return request.get('/menu/tree')
}

export function getCurrentMenus() {
  return request.get('/menu/current')
}

export function getMenuById(id: number) {
  return request.get(`/menu/${id}`)
}

export function saveMenu(data: any) {
  return request.post('/menu', data)
}

export function updateMenu(data: any) {
  return request.put('/menu', data)
}

export function deleteMenu(id: number) {
  return request.delete(`/menu/${id}`)
}
