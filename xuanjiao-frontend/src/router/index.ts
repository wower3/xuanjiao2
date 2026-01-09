import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCurrentPermissionCodes } from '@/api/permission'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      redirect: '/asset',
      meta: { requiresAuth: true },
      children: [
        {
          path: 'asset',
          name: 'Asset',
          component: () => import('@/views/asset/index.vue')
        },
        {
          path: 'workflow',
          name: 'Workflow',
          component: () => import('@/views/workflow/index.vue')
        },
        {
          path: 'workflow/design/:id?',
          name: 'WorkflowDesign',
          component: () => import('@/views/workflow/design.vue')
        },
        {
          path: 'approval',
          name: 'Approval',
          component: () => import('@/views/approval/index.vue')
        },
        {
          path: 'log',
          name: 'Log',
          component: () => import('@/views/log/index.vue')
        },
        {
          path: 'system/dept',
          name: 'Dept',
          component: () => import('@/views/system/dept.vue')
        },
        {
          path: 'system/user',
          name: 'User',
          component: () => import('@/views/system/user.vue')
        },
        {
          path: 'system/role',
          name: 'Role',
          component: () => import('@/views/system/role.vue')
        }
      ]
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else if (token) {
    const userStore = useUserStore()
    // 如果还没有加载权限，则加载
    if (userStore.permissions.length === 0) {
      try {
        const res = await getCurrentPermissionCodes()
        userStore.setPermissions(res.data || [])
      } catch (e) {
        console.error('加载权限失败', e)
      }
    }
    next()
  } else {
    next()
  }
})

export default router
