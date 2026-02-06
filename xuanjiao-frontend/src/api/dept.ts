/**
 * 部门API模块
 * <p>提供部门的树形结构、CRUD、编号生成等接口</p>
 *
 * @author system
 * @version 1.0
 */
import request from '@/utils/request'

/**
 * 获取部门列表（扁平）
 */
export function getDeptList() {
  return request.post('/dept/getList', {})
}

/**
 * 获取部门树
 */
export function getDeptTree() {
  return request.post('/dept/getTree', {})
}

/**
 * 获取部门详情
 * @param id 部门ID
 */
export function getDeptById(id: number) {
  return request.post('/dept/getDetail', { id })
}

/**
 * 创建部门
 * @param data 部门信息
 */
export function saveDept(data: any) {
  return request.post('/dept/create', data)
}

/**
 * 更新部门
 * @param data 部门信息
 */
export function updateDept(data: any) {
  return request.post('/dept/update', data)
}

/**
 * 删除部门
 * @param id 部门ID
 */
export function deleteDept(id: number) {
  return request.post('/dept/delete', { id })
}

/**
 * 生成部门编号
 */
export function generateDeptCode() {
  return request.get('/dept/generate-code')
}
