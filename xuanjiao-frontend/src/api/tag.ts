/**
 * 标签API模块
 * <p>提供标签的查询、创建、删除等接口</p>
 *
 * @author system
 * @version 1.0
 */
import request from '@/utils/request'

/**
 * 获取标签列表
 */
export function getTagList() {
  return request.post('/tag/getList', {})
}

/**
 * 根据分类获取标签列表
 * @param category 分类名称
 */
export function getTagsByCategory(category: string) {
  return request.post('/tag/getListByCategory', { category })
}

/**
 * 创建标签
 * @param data 标签信息
 */
export function createTag(data: any) {
  return request.post('/tag/create', null, {
    params: data
  })
}

/**
 * 删除标签
 * @param id 标签ID
 */
export function deleteTag(id: number) {
  return request.post('/tag/delete', { id })
}
