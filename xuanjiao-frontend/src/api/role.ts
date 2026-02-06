/**
 * 角色API模块
 * <p>提供角色的CRUD、菜单权限分配等接口</p>
 *
 * @author system
 * @version 1.0
 */
import request from '@/utils/request'

/**
 * 获取角色列表
 */
export function getRoleList() {
  return request.post('/role/getList', {})
}

/**
 * 获取角色详情
 * @param id 角色ID
 */
export function getRoleById(id: number) {
  return request.post('/role/getDetail', { id })
}

/**
 * 创建角色
 * @param data 角色信息
 */
export function createRole(data: any) {
  return request.post('/role/create', data)
}

/**
 * 更新角色
 * @param data 角色信息
 */
export function updateRole(data: any) {
  return request.post('/role/update', data)
}

/**
 * 删除角色
 * @param id 角色ID
 */
export function deleteRole(id: number) {
  return request.post('/role/delete', { id })
}

/**
 * 为角色分配菜单权限
 * @param roleId 角色ID
 * @param menuIds 菜单ID数组
 */
export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return request.post(`/role/${roleId}/menus`, menuIds)
}

/**
 * 获取角色的菜单权限
 * @param roleId 角色ID
 */
export function getRoleMenus(roleId: number) {
  return request.post('/role/getRoleMenus', { roleId })
}
