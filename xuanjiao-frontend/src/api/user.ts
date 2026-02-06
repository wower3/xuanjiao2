/**
 * 用户API模块
 * <p>提供用户CRUD、查询、筛选相关接口</p>
 *
 * @author system
 * @version 1.0
 */
import request from '@/utils/request'

/**
 * 获取当前登录用户信息
 */
export function getCurrentUser() {
  return request.get('/user/current')
}

/**
 * 获取用户列表（全部）
 */
export function getUserList() {
  return request.post('/user/getList', {})
}

/**
 * 获取用户列表（带筛选条件）
 * @param params 筛选参数，支持按角色ID列表、部门ID筛选
 */
export function getUserListWithFilter(params: {
  roleIds?: number[]
  deptId?: number
  includeSubDept?: boolean
}) {
  return request.post('/user/getListWithFilter', params)
}

/**
 * 搜索用户（支持角色/部门/姓名筛选，带分页）
 * @param params 搜索参数
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

/**
 * 获取默认筛选部门（根据当前用户权限）
 */
export function getDefaultFilterDept() {
  return request.post('/user/getDefaultFilterDept', {})
}

/**
 * 创建用户
 * @param data 用户信息
 */
export function createUser(data: any) {
  return request.post('/user/create', data)
}

/**
 * 更新用户
 * @param data 用户信息（包含ID）
 */
export function updateUser(data: any) {
  return request.post('/user/update', data)
}

/**
 * 删除用户
 * @param id 用户ID
 */
export function deleteUser(id: number) {
  return request.post('/user/delete', { id })
}
