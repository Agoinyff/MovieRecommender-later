import { createRouter, createWebHistory } from 'vue-router';
import pinia from '@/store';
import { useAuthStore } from '@/store/auth';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomePage.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/movies',
    name: 'Movies',
    component: () => import('@/views/MoviesPage.vue'),
    meta: { title: '电影库' }
  },
  {
    path: '/movies/:id',
    name: 'MovieDetail',
    component: () => import('@/views/MovieDetailPage.vue'),
    meta: { title: '电影详情', requiresAuth: true }
  },
  {
    path: '/recommendations',
    name: 'Recommendations',
    component: () => import('@/views/RecommendationsPage.vue'),
    meta: { title: '个性化推荐', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/UserProfilePage.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/AdminPage.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/metrics',
    name: 'Metrics',
    component: () => import('@/views/MetricsPage.vue'),
    meta: { title: '系统监控', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginPage.vue'),
    meta: { title: '登录', guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterPage.vue'),
    meta: { title: '注册', guestOnly: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  }
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia);
  await authStore.hydrate();

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return { name: 'Recommendations' };
  }
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'Login', query: { redirect: to.fullPath } };
  }
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { name: 'Recommendations' };
  }
  document.title = to.meta.title ? `${to.meta.title} - 电影推荐系统` : '电影推荐系统';
  return true;
});

export default router;
