import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/user'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { getUserInfo, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])

  const isAuthenticated = computed(() => !!token.value)

  const setUserToken = (newToken: string) => {
    token.value = newToken
    setToken(newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    permissions.value = info.permissions || []
  }

  const fetchUserInfo = async () => {
    try {
      const info = await getUserInfo()
      setUserInfo(info)
      return info
    } catch (error) {
      console.error('Failed to fetch user info:', error)
      throw error
    }
  }

  const logout = async () => {
    try {
      await logoutApi()
    } catch (error) {
      console.error('Logout API failed:', error)
    } finally {
      token.value = null
      userInfo.value = null
      permissions.value = []
      removeToken()
    }
  }

  const clearAuth = () => {
    token.value = null
    userInfo.value = null
    permissions.value = []
    removeToken()
  }

  return {
    token,
    userInfo,
    permissions,
    isAuthenticated,
    setUserToken,
    setUserInfo,
    fetchUserInfo,
    logout,
    clearAuth
  }
})
