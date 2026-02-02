import request from '@/utils/request'

export function getDeptList() {
  return request.post('/dept/getList', {})
}

export function getDeptTree() {
  return request.post('/dept/getTree', {})
}

export function getDeptById(id: number) {
  return request.post('/dept/getDetail', { id })
}

export function saveDept(data: any) {
  return request.post('/dept/create', data)
}

export function updateDept(data: any) {
  return request.post('/dept/update', data)
}

export function deleteDept(id: number) {
  return request.post('/dept/delete', { id })
}

export function generateDeptCode() {
  return request.get('/dept/generate-code')
}
