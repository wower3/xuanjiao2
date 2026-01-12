import { defineStore } from 'pinia'
import { ref } from 'vue'
import { resetSessionTimeout, clearSessionTimeout } from '@/utils/sessionTimeout'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<any>(null)
  const permissions = ref<string[]>([])

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
    // 登录成功后启动会话超时计时器
    if (t) {
      resetSessionTimeout()
    }
  }

  function setUserInfo(info: any) {
    userInfo.value = info
  }

  function setPermissions(perms: string[]) {
    permissions.value = perms
  }

  function hasPermission(code: string): boolean {
    return permissions.value.includes(code)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
    // 清除会话超时计时器
    clearSessionTimeout()
  }

  return { token, userInfo, permissions, setToken, setUserInfo, setPermissions, hasPermission, logout }
})
