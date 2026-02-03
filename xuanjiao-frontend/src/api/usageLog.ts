import request from '@/utils/request'

// 获取素材使用记录列表
export function getAssetUsageLogs(assetId: number, params?: { pageNum?: number; pageSize?: number }) {
  return request.post('/log/getAssetUsageLogs', { assetId, ...params })
}
