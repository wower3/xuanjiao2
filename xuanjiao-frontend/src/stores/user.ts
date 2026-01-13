import { defineStore } from 'pinia'
import { ref } from 'vue'
import { resetSessionTimeout, clearSessionTimeout } from '@/utils/sessionTimeout'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<any>(null)

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

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    // 清除会话超时计时器
    clearSessionTimeout()
  }

  return { token, userInfo, setToken, setUserInfo, logout }
})
