import request from '@/utils/request'

export function getLogList(params: any) {
  return request.post('/log/queryLogs', params)
}
