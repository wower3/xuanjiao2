/**
 * 素材录入申请API模块
 * <p>提供素材录入申请的创建、更新、提交、删除等接口</p>
 *
 * @author system
 * @version 1.0
 */
import request from '@/utils/request'

/**
 * 创建素材录入申请
 * @param data 申请信息
 */
export function createMaterialApplication(data: any) {
  return request.post('/material-application/create', data)
}

/**
 * 更新素材录入申请
 * @param id 申请ID
 * @param data 更新数据
 */
export function updateMaterialApplication(id: number, data: any) {
  return request.post('/material-application/update', { id, ...data })
}

/**
 * 提交素材录入申请到审批
 * @param data 提交数据（包含申请ID和审批流程ID）
 */
export function submitMaterialApplication(data: { id: number; workflowId: number }) {
  return request.post('/material-application/submit', data)
}

/**
 * 删除素材录入申请
 * @param id 申请ID
 */
export function deleteMaterialApplication(id: number) {
  return request.post('/material-application/delete', { id })
}

/**
 * 获取素材录入申请详情
 * @param id 申请ID
 */
export function getMaterialApplicationById(id: number) {
  return request.post('/material-application/getDetail', { id })
}

/**
 * 获取草稿列表
 * @param params 查询参数
 */
export function getDraftApplications(params: any) {
  return request.post('/material-application/getDrafts', params)
}

/**
 * 获取我的申请列表
 * @param params 查询参数
 */
export function getMyApplications(params: any) {
  return request.post('/material-application/getMyApplications', params)
}

/**
 * 复制申请单（被驳回的申请）
 * @param id 申请ID
 */
export function copyApplication(id: number) {
  return request.post(`/material-application/${id}/copy`)
}
