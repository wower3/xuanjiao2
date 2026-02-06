import request from '@/utils/request'

export function getCurrentUser() {
  return request.get('/user/current')
}

export function getUserList() {
  return request.post('/user/getList', {})
}

export function getUserListWithFilter(params: {
  roleIds?: number[]
  deptId?: number
  includeSubDept?: boolean
}) {
  return request.post('/user/getListWithFilter', params)
}

/**
 * 搜索用户（支持角色/部门/姓名筛选，带分页）
 */
export function searchUsers(params: {
  roleIds?: number[]
  deptId?: number
  includeSubDept?: boolean
  keyword?: string
  pageNum?: number
  pageSize?: number
}) {
  return request.post('/user/search', params)
}

export function getDefaultFilterDept() {
  return request.post('/user/getDefaultFilterDept', {})
}

export function createUser(data: any) {
  return request.post('/user/create', data)
}

export function updateUser(data: any) {
  return request.post('/user/update', data)
}

export function deleteUser(id: number) {
  return request.post('/user/delete', { id })
}
