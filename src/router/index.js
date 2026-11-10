import { createRouter, createWebHistory } from 'vue-router'
import BackendLayout from '@/components/BackendLayout.vue'
import AuthLayout from '@/components/AuthLayout.vue'
import FrontendLayout from '@/components/FrontendLayout.vue'

// 路由配置
const backendroutes = [
  {
    path: '/back',
    redirect: '/back/dashboard',
    component: BackendLayout,
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard.vue'),
        meta: {
          title: '数据分析',
          icon: 'Search'
        }
      },
      {
        path: 'knowledge',
        component: () => import('@/views/knowledge.vue'),
        meta: {
          title: '知识文件',
          icon: 'Folder'
        }
      },
      {
        path: 'consultation',
        component: () => import('@/views/consultations.vue'),
        meta: {
          title: '咨询管理',
          icon: 'Message'
        }
      },
      {
         path: 'emotional',
        component: () => import('@/views/emotional.vue'),
        meta: {
          title: '情感日志',
          icon: 'User'
        }
      }
    ]
  },
  {
    path: '/auth',
    component: AuthLayout,
    children: [
      {
        path: 'login',
        component: () => import('@/views/login.vue'),
        meta: {
          title: '登录',
        }
      },
      {
        path: 'register',
        component: () => import('@/views/register.vue'),
        meta: {
          title: '注册',
        }
      }
    ]
  }

]

const frontendroutes = [
  {
    path: '/',
    component: FrontendLayout,
    children: [
      {
        path: '',
        component: () => import('@/views/home.vue'),
      },
      {
        path: 'consultation',
        component: () => import('@/views/consultation.vue'),
      },
      {
        path: 'emotional-diary',
        component: () => import('@/views/emotionalDiary.vue'),
      },
      {
        path: 'knowledge',
        component: () => import('@/views/frontendKnowledge.vue'),
      },
      {
        path: 'knowledge/article/:id',
        component: () => import('@/views/articleDetail.vue'),
        props: true
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: [...backendroutes, ...frontendroutes]
})

// 添加路由守卫
router.beforeEach((to, from, next) => { 
  const token = localStorage.getItem('token')
  // 判断是否登录
  if (token) { // 如果有token说明登录了
    const userInfo = JSON.parse(localStorage.getItem('userInfo'))
    if (userInfo.userType == 2) {  // 判断用户类型，为2说明是后台管理员用户
      if(to.path.startsWith('/back')) {
        next() 
      } else { 
        next('/back/dashboard')  // 后台管理员用户登录了，并且不是后台页面，则跳转到后台页面
      }
    } else if (userInfo.userType == 1) {   // 否则为前台普通用户
      // 判断用户类型，为1说明是前台普通用户，只能访问前台页面
      if(to.path.startsWith('/back') || to.path.startsWith('/auth')) { 
        next('/')  // 前台普通用户登录了，并且访问了后台或登录页面，则跳转到前台页面
      } else { 
        next()  // 否则正常访问
      }
    }  
  } else {
    if (to.path.startsWith('/back')) { // 没有登录的情况下， 如果当前是后台页面，则直接进入登录页面
      next('/auth/login')
    } else { 
      next()
    }
  }
})

export default router