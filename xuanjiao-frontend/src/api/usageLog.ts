import request from '@/utils/request'

// 获取素材使用记录列表
export function getAssetUsageLogs(assetId: number, params?: { pageNum?: number; pageSize?: number }) {
  return request.get(`/log/asset/${assetId}/usage-logs`, { params })
}
