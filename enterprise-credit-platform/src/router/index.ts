import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页仪表盘' }
      },
      {
        path: '/enterprise',
        name: 'Enterprise',
        component: () => import('@/views/EnterpriseInfo.vue'),
        meta: { title: '企业管理' }
      },
      {
        path: '/report',
        name: 'Report',
        component: () => import('@/views/ReportList.vue'),
        meta: { title: '报告管理' }
      },
      {
        path: '/order',
        name: 'Order',
        component: () => import('@/views/BuyReport.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: '/authorization',
        name: 'Authorization',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '授权管理' }
      },
      {
        path: '/system',
        name: 'System',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '系统管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = userStore.isAuthenticated

  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if ((to.name === 'Login' || to.name === 'Register') && isAuthenticated) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
