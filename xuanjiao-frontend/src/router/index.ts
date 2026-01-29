import { createRouter, createWebHistory } from 'vue-router'

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
      meta: { requiresAuth: true },
      children: [
        {
          path: 'asset',
          name: 'Asset',
          component: () => import('@/views/asset/index.vue')
        },
        {
          path: 'asset/material-list',
          name: 'MaterialList',
          component: () => import('@/views/asset/material-list.vue')
        },
        {
          path: 'asset/material-entry',
          name: 'MaterialEntry',
          component: () => import('@/views/asset/material-entry.vue')
        },
        {
          path: 'asset/usage-apply',
          name: 'UsageApply',
          component: () => import('@/views/asset/usage-apply.vue')
        },
        {
          path: 'asset/deletion',
          name: 'AssetDeletion',
          component: () => import('@/views/asset/deletion/index.vue')
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
          path: 'task/pending-approval',
          name: 'PendingApproval',
          component: () => import('@/views/task/pending-approval.vue')
        },
        {
          path: 'task/my-initiated',
          name: 'MyInitiated',
          component: () => import('@/views/task/my-initiated.vue')
        },
        {
          path: 'task/draft-box',
          name: 'DraftBox',
          component: () => import('@/views/task/draft-box.vue')
        },
        {
          path: 'task/in-progress',
          name: 'WorkflowInProgress',
          component: () => import('@/views/task/workflow-in-progress.vue')
        },
        {
          path: 'task/material-approval',
          name: 'MaterialApproval',
          component: () => import('@/views/task/material-approval.vue')
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
        },
        {
          path: 'system/menu',
          name: 'Menu',
          component: () => import('@/views/system/menu.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    // 登录后重定向到首页，不需要在这里处理，由 MainLayout 处理
    next()
  } else {
    next()
  }
})

export default router
