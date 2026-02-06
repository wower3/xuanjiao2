/**
 * 菜单API模块
 * <p>提供菜单的树形结构、CRUD、角色菜单查询等接口</p>
 *
 * @author system
 * @version 1.0
 */
import request from '@/utils/request'

/**
 * 获取菜单树
 */
export function getMenuTree() {
  return request.post('/menu/getTree', {})
}

/**
 * 获取当前用户的菜单权限
 */
export function getCurrentMenus() {
  return request.post('/menu/getCurrent', {})
}

/**
 * 获取菜单详情
 * @param id 菜单ID
 */
export function getMenuById(id: number) {
  return request.post('/menu/getDetail', { id })
}

/**
 * 创建菜单
 * @param data 菜单信息
 */
export function saveMenu(data: any) {
  return request.post('/menu/create', data)
}

/**
 * 更新菜单
 * @param data 菜单信息
 */
export function updateMenu(data: any) {
  return request.post('/menu/update', data)
}

/**
 * 删除菜单
 * @param id 菜单ID
 */
export function deleteMenu(id: number) {
  return request.post('/menu/delete', { id })
}

/**
 * 获取角色的菜单权限
 * @param roleId 角色ID
 */
export function getRoleMenus(roleId: number) {
  return request.post('/menu/getRoleMenus', { roleId })
}
