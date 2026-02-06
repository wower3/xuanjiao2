import request from '@/utils/request'

/**
 * 通知相关API
 */

/**
 * 获取通知列表
 */
export function getNotificationList(params: {
  pageNum?: number
  pageSize?: number
  notificationType?: string
  isRead?: number
  sourceType?: string
}) {
  return request.post('/notification/getMyNotifications', params)
}

/**
 * 获取知会事项列表（包含工单信息）
 */
export function getNotificationListWithWorkOrder(params: {
  pageNum?: number
  pageSize?: number
  notificationType?: string
  isRead?: number
  sourceType?: string
}) {
  return request.post('/notification/getMyNotificationsWithWorkOrder', params)
}

/**
 * 获取通知详情
 */
export function getNotificationDetail(id: number) {
  return request.post('/notification/getDetail', { id })
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount() {
  return request.post('/notification/getUnreadCount')
}

/**
 * 创建通知
 */
export function createNotification(data: {
  title: string
  content: string
  notificationType: string
  sourceType?: string
  sourceId?: number
  recipientIds: number[]
}) {
  return request.post('/notification/create', data)
}

/**
 * 批量创建通知
 */
export function batchCreateNotification(data: {
  title: string
  content: string
  notificationType: string
  sourceType?: string
  sourceId?: number
  recipientIds: number[]
}) {
  return request.post('/notification/batchCreate', data)
}

/**
 * 标记通知为已读
 */
export function markAsRead(id: number) {
  return request.post('/notification/markAsRead', { id })
}

/**
 * 批量标记通知为已读
 */
export function batchMarkAsRead(ids: number[]) {
  return request.post('/notification/batchMarkAsRead', { ids })
}

/**
 * 标记所有通知为已读
 */
export function markAllAsRead() {
  return request.post('/notification/markAllAsRead')
}

/**
 * 删除通知
 */
export function deleteNotification(id: number) {
  return request.post('/notification/delete', { id })
}

/**
 * 批量删除通知
 */
export function batchDeleteNotification(ids: number[]) {
  return request.post('/notification/batchDelete', { ids })
}

/**
 * 知会用户关于审批实例
 */
export function notifyUsers(data: {
  instanceId: number
  recipientIds: number[]
  message?: string
}) {
  return request.post('/notification/notifyUsers', data)
}

/**
 * 获取工单的知会记录
 */
export function getNotificationRecords(instanceId: number) {
  return request.post('/notification/getNotificationRecords', { instanceId })
}
