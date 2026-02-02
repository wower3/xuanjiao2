import request from '@/utils/request'

export function getAssetList(params: any) {
  return request.post('/asset/list', params)
}

export function getAssetById(id: number) {
  return request.post('/asset/getDetail', { id })
}

export function deleteAsset(id: number) {
  return request.post('/asset/delete', { id })
}

export function uploadAsset(file: File, data: any) {
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
  return request.post('/asset/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function uploadCopyrightFile(file: File): Promise<{ filePath: string }> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/asset/upload-copyright', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 管理员彻底删除素材
export function adminDeleteAsset(id: number, reason: string) {
  return request.post('/asset/adminDelete', { id, reason })
}

// 管理员调整素材删除时间（测试功能）
export function adjustAssetDeleteTime(id: number) {
  return request.post('/asset/adjustDeleteTime', { id })
}

// 管理员手动触发定时任务（测试功能）
export function triggerCleanupTask() {
  return request.post('/asset/admin/trigger-cleanup')
}

// 查询用户已录入的素材（APPROVED状态）
export function getMyApprovedAssets(params: any) {
  return request.post('/asset/getMyApproved', params)
}
