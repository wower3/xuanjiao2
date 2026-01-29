import request from '@/utils/request'

export function getCurrentUser() {
  return request.get('/user/current')
}

export function getUserList() {
  return request.get('/user/list')
}

export function getUserListWithFilter(params: {
  roleIds?: number[]
  deptId?: number
  includeSubDept?: boolean
}) {
  // 手动构建查询参数，将数组转换为逗号分隔的字符串
  const queryParams: Record<string, any> = {}
  if (params.deptId !== undefined) {
    queryParams.deptId = params.deptId
  }
  if (params.includeSubDept !== undefined) {
    queryParams.includeSubDept = params.includeSubDept
  }
  // 将 roleIds 数组转换为逗号分隔的字符串
  if (params.roleIds && params.roleIds.length > 0) {
    queryParams.roleIds = params.roleIds.join(',')
  }
  return request.get('/user/listWithFilter', { params: queryParams })
}

export function getDefaultFilterDept() {
  return request.get('/user/defaultFilterDept')
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
