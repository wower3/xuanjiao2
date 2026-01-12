import request from '@/utils/request'

// 申请使用素材
export function applyUsage(data: any) {
  return request.post('/usage-apply/apply', data)
}

// 查询我的申请列表
export function getMyApplications(params: any) {
  return request.get('/usage-apply/my-applications', { params })
}

// 检查是否有权限使用素材
export function checkCanUse(assetId: number) {
  return request.get(`/usage-apply/can-use/${assetId}`)
}

// 下载素材
export function downloadAsset(assetId: number) {
  return request.get(`/asset/download/${assetId}`, {
    responseType: 'blob'
  })
}
