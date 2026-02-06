/**
 * 认证API模块
 * <p>提供用户登录认证相关接口</p>
 *
 * @author system
 * @version 1.0
 */
import request from '@/utils/request'

/**
 * 用户登录
 * @param data 登录参数，包含用户名和密码
 * @returns 登录结果，包含Token和用户信息
 */
export function login(data: { username: string; password: string }) {
  return request.post('/auth/login', data)
}

