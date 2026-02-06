/**
 * 流经事项API模块
 * <p>提供查询当前用户参与过的审批流程（作为发起人或审批人）</p>
 *
 * @author system
 * @version 1.0
 */
import request from '@/utils/request'

/**
 * 获取流经事项列表
 * @param params 查询参数
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
