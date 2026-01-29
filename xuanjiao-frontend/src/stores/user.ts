import { defineStore } from 'pinia'
import { ref } from 'vue'
import { resetSessionTimeout, clearSessionTimeout } from '@/utils/sessionTimeout'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  // 立即从 localStorage 恢复用户信息（在 store 初始化时执行，早于组件渲染）
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = ref<any>(userInfoStr ? JSON.parse(userInfoStr) : null)

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
    // 同时存储到 localStorage，方便页面刷新时恢复
    if (info) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    // 清除会话超时计时器
    clearSessionTimeout()
  }

  return { token, userInfo, setToken, setUserInfo, logout }
})
