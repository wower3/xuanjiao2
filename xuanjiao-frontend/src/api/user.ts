import request from '@/utils/request'

export function getCurrentUser() {
  return request.get('/user/current')
}

export function getUserList() {
  return request.get('/user/list')
}

export function createUser(data: any) {
  return request.post('/user', data)
}

export function updateUser(data: any) {
  return request.put('/user', data)
}

export function deleteUser(id: number) {
  return request.delete(`/user/${id}`)
}
