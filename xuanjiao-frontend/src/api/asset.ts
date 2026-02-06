/**
 * 素材API模块
 * <p>提供素材的上传、查询、删除、预览等接口</p>
 *
 * @author system
 * @version 1.0
 */
import request from '@/utils/request'

/**
 * 获取素材列表（分页筛选）
 * @param params 查询参数（名称、类型、状态、分页）
 */
export function getAssetList(params: any) {
  return request.post('/asset/list', params)
}

/**
 * 获取素材详情
 * @param id 素材ID
 */
export function getAssetById(id: number) {
  return request.post('/asset/getDetail', { id })
}

/**
 * 删除素材（标记删除状态）
 * @param id 素材ID
 */
export function deleteAsset(id: number) {
  return request.post('/asset/delete', { id })
}

/**
 * 上传素材
 * @param file 素材文件
 * @param data 素材信息
 * @param thumbnailFile 视频缩略图（可选）
 */
export function uploadAsset(file: File, data: any, thumbnailFile?: File) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('name', data.name)
  formData.append('type', data.type)
  if (data.workflowId) {
    formData.append('workflowId', data.workflowId)
  }
  // New fields for material entry
  if (data.applicationId) {
    formData.append('applicationId', data.applicationId)
  }
  if (data.tagIds && data.tagIds.length > 0) {
    data.tagIds.forEach((tagId: number) => {
      formData.append('tagIds', String(tagId))
    })
  }
  if (data.copyrightFilePath) {
    formData.append('copyrightFilePath', data.copyrightFilePath)
  }
  if (data.copyrightText) {
    formData.append('copyrightText', data.copyrightText)
  }
  if (data.description) {
    formData.append('description', data.description)
  }
  if (data.publishChannel) {
    formData.append('publishChannel', data.publishChannel)
  }
  // 视频缩略图
  if (thumbnailFile) {
    formData.append('thumbnailFile', thumbnailFile)
  }
  return request.post('/asset/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 上传版权附件文件
 * @param file 版权文件
 */
export function uploadCopyrightFile(file: File): Promise<{ filePath: string }> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/asset/upload-copyright', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 管理员彻底删除素材（软删除，deleted=1）
 * @param id 素材ID
 * @param reason 删除原因
 */
export function adminDeleteAsset(id: number, reason: string) {
  return request.post('/asset/adminDelete', { id, reason })
}

/**
 * 管理员调整素材删除时间（测试功能，将deletion_approve_time设为7天前）
 * @param id 素材ID
 */
export function adjustAssetDeleteTime(id: number) {
  return request.post('/asset/adjustDeleteTime', { id })
}

/**
 * 管理员手动触发定时清理任务（测试功能）
 */
export function triggerCleanupTask() {
  return request.post('/asset/admin/trigger-cleanup')
}

/**
 * 查询用户已录入的素材（APPROVED状态）
 * @param params 查询参数
 */
export function getMyApprovedAssets(params: any) {
  return request.post('/asset/getMyApproved', params)
}
