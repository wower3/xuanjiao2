import { useUserStore } from '@/stores/user'
import router from '@/router'

// 超时时间：15分钟（单位：毫秒）
const TIMEOUT = 15 * 60 * 1000
let timeoutId: ReturnType<typeof setTimeout> | null = null

/**
 * 重置会话超时计时器
 */
export function resetSessionTimeout() {
  // 清除之前的计时器
  if (timeoutId) {
    clearTimeout(timeoutId)
  }

  // 设置新的计时器
  timeoutId = setTimeout(() => {
    handleSessionTimeout()
  }, TIMEOUT)
}

/**
 * 处理会话超时
 */
function handleSessionTimeout() {
  const userStore = useUserStore()
  // 清除用户信息和token
  userStore.$reset?.() || userStore.setToken('')
  localStorage.removeItem('token')

  // 跳转到登录页
  router.push('/login')
}

/**
 * 清除会话超时计时器（用于登出时）
 */
export function clearSessionTimeout() {
  if (timeoutId) {
    clearTimeout(timeoutId)
    timeoutId = null
  }
}
