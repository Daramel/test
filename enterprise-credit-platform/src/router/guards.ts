import type { Router } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { ElMessage } from 'element-plus'

export const setupRouterGuards = (router: Router) => {
  router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    if (to.meta.requiresAuth) {
      if (!userStore.isAuthenticated) {
        ElMessage.warning('请先登录')
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }

      if (to.meta.permission && typeof to.meta.permission === 'string') {
        const hasPermission = userStore.permissions.includes(to.meta.permission)
        if (!hasPermission) {
          ElMessage.error('您没有权限访问该页面')
          next({ name: 'Dashboard' })
          return
        }
      }
    }

    next()
  })

  router.afterEach((to) => {
    document.title = `${to.meta.title || '企业征信管理平台'} - 企业征信管理平台`
  })
}
