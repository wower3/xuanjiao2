import request from '@/utils/request'

export function getCurrentPermissions() {
  return request.get('/permission/current')
}

export function getCurrentPermissionCodes() {
  return request.get('/permission/codes')
}
