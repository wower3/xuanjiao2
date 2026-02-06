import request from '@/utils/request'

/**
 * 流经事项相关API
 */

/**
 * 获取流经事项列表
 */
export function getMyFlowItems(params: {
  pageNum?: number
  pageSize?: number
  businessType?: string
  status?: string
  keyword?: string
}) {
  return request.post('/approval/getMyFlowItems', params)
}
