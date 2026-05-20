import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由配置
 * 包含公开路由(登录/注册)和需要认证的主布局路由
 * 主布局路由下包含所有功能页面子路由
 */
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { noAuth: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/Register.vue'),
      meta: { noAuth: true }
    },
    {
      path: '/',
      component: () => import('@/components/layout/AppLayout.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/home/Dashboard.vue') },
        { path: 'weight', name: 'Weight', component: () => import('@/views/weight/WeightPage.vue') },
        { path: 'plans', name: 'Plans', component: () => import('@/views/plan/PlanList.vue') },
        { path: 'plans/create', name: 'PlanCreate', component: () => import('@/views/plan/PlanEditor.vue') },
        { path: 'plans/:id/edit', name: 'PlanEdit', component: () => import('@/views/plan/PlanEditor.vue') },
        { path: 'plans/templates', name: 'PlanTemplates', component: () => import('@/views/plan/PlanTemplate.vue') },
        { path: 'diet', name: 'Diet', component: () => import('@/views/diet/DietPage.vue') },
        { path: 'diet/overview', name: 'DietOverview', component: () => import('@/views/diet/DietOverview.vue') },
        { path: 'foods', name: 'Foods', component: () => import('@/views/food-library/FoodList.vue') },
        { path: 'actions', name: 'Actions', component: () => import('@/views/action-library/ActionList.vue') },
        { path: 'training', name: 'Training', component: () => import('@/views/training/TrainingPage.vue') },
        { path: 'measurement', name: 'Measurement', component: () => import('@/views/measurement/MeasurementPage.vue') },
        { path: 'photos', name: 'Photos', component: () => import('@/views/photo/BodyPhotoPage.vue') },
        { path: 'water', name: 'Water', component: () => import('@/views/water/WaterPage.vue') },
        { path: 'profile', name: 'Profile', component: () => import('@/views/profile/ProfilePage.vue') },
        { path: 'admin/foods', name: 'AdminFoods', component: () => import('@/views/admin/AdminFood.vue'), meta: { admin: true } },
        { path: 'admin/actions', name: 'AdminActions', component: () => import('@/views/admin/AdminAction.vue'), meta: { admin: true } },
        { path: 'admin/templates', name: 'AdminTemplates', component: () => import('@/views/admin/AdminTemplate.vue'), meta: { admin: true } },
        { path: 'admin/users', name: 'AdminUsers', component: () => import('@/views/admin/AdminUsers.vue'), meta: { admin: true } },
        { path: 'admin/dict', name: 'AdminDict', component: () => import('@/views/admin/AdminDict.vue'), meta: { admin: true } }
      ]
    }
  ]
})

/**
 * 路由守卫：验证登录状态和权限
 * - 未登录用户只能访问标记了noAuth的页面(登录/注册)
 * - 管理员路由需要ADMIN角色才能访问
 * - 非管理员访问管理页面会被重定向到首页
 */
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('accessToken')
  // 未登录只能访问免登录页面
  if (!to.meta.noAuth && !token) {
    next('/login')
  } else if (to.meta.admin) {
    // 管理员权限检查
    const role = localStorage.getItem('role')
    if (role !== 'ADMIN') next('/dashboard')
    else next()
  } else {
    next()
  }
})

export default router
