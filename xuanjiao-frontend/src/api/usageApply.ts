import request from '@/utils/request'

// ========== 新API（多素材支持） ==========

// 创建使用申请草稿
export function createUsageDraft(data: any) {
  return request.post('/usage-apply/draft', data)
}

// 更新使用申请草稿
export function updateUsageDraft(id: number, data: any) {
  return request.post('/usage-apply/update', { id, ...data })
}

// 提交使用申请
export function submitUsageApply(id: number, workflowId: number) {
  return request.post(`/usage-apply/${id}/submit`, null, {
    params: { workflowId }
  })
}

// 删除使用申请
export function deleteUsageApply(id: number) {
  return request.post('/usage-apply/delete', { id })
}

// 查询申请单详情
export function getUsageApplyById(id: number) {
  return request.post('/usage-apply/getDetail', { id })
}

// 查询草稿箱
export function getUsageDrafts(params: any) {
  return request.post('/usage-apply/getDrafts', params)
}

// 查询我的所有申请
export function getMyUsageApplies(params: any) {
  return request.post('/usage-apply/getMyApplications', params)
}

// 检查是否有权限使用素材
export function checkCanUseAsset(assetId: number) {
  return request.post('/usage-apply/canUseAsset', { assetId })
}

// 复制使用申请
export function copyApplication(id: number) {
  return request.post(`/usage-apply/${id}/copy`)
}

// ========== 旧API（保持兼容） ==========

// 申请使用素材（单素材）
export function applyUsage(data: any) {
  return request.post('/usage-apply/apply', data)
}

// 查询我的申请列表（旧API）
export function getMyApplications(params: any) {
  return request.get('/usage-apply/my-applications', { params })
}

// 下载素材
export function downloadAsset(assetId: number) {
  return request.get(`/asset/download/${assetId}`, {
    responseType: 'blob'
  })
}
